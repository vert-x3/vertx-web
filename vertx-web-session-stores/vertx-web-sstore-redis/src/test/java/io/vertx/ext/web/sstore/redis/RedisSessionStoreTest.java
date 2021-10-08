package io.vertx.ext.web.sstore.redis;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import org.junit.*;

import io.vertx.core.CompositeFuture;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

/**
 * @author <a href="https://github.com/llfbandit">Rémy Noël</a>
 */
@RunWith(VertxUnitRunner.class)
public class RedisSessionStoreTest {

  @ClassRule
  public static GenericContainer<?> container = new GenericContainer<>("redis:6.2")
    .withExposedPorts(6379);

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  private SessionStore store;

  @Before
  public void before() {
    store = RedisSessionStore.create(
      // get the vertx instance
      rule.vertx(),
      // provide a client
      Redis.createClient(rule.vertx(), new RedisOptions()
        .setConnectionString("redis://" + container.getContainerIpAddress() + ":" + container.getMappedPort(6379))
        // how many connections are we willing to open to redis?
        .setMaxPoolSize(2)
        // how many waiting connections are we allowing to queue?
        .setMaxPoolWaiting(32)));
  }

  @After
  public void after(TestContext should) {
    final Async test = should.async();
    store.clear(clear -> {
      should.assertTrue(clear.succeeded());
      test.complete();
    });
  }

  @Test(timeout = 10_000)
  public void testPutSession(TestContext should) {
    final Async test = should.async();

    Session session = store.createSession(30_000);

    store.put(session).onComplete(res -> {
      should.assertTrue(res.succeeded());
      test.complete();
    });
  }

  @Test(timeout = 10_000)
  public void testGetSession(TestContext should) {
    final Async test = should.async();

    Session session = store.createSession(30_000);
    String value = session.value();

    store.put(session)
      .compose(aVoid -> store.get(value))
      .map(sessionGet -> {
        should.assertEquals(value, sessionGet.value());
        return null;
      })
      .onComplete(res -> {
        should.assertTrue(res.succeeded());
        test.complete();
      });
  }

  @Test(timeout = 10_000)
  public void testClearSession(TestContext should) {
    final Async test = should.async();

    Session session = store.createSession(30_000);

    store.put(session)
      .compose(aVoid -> store.clear())
      .onComplete(res -> {
        should.assertTrue(res.succeeded());
        test.complete();
      });
  }

  @Test(timeout = 10_000)
  public void testSizeSession(TestContext should) {
    final Async test = should.async();

    Session session = store.createSession(30_000);

    store.put(session)
      .compose(atrVoid -> store.size())
      .map(size -> {
        should.assertEquals(1, size);
        return size;
      })
      .compose(size -> store.clear())
      .compose(atrVoid -> store.size())
      .onComplete(res -> {
        should.assertTrue(res.succeeded());
        should.assertEquals(0, res.result());
        test.complete();
      });
  }

  @Test(timeout = 10_000)
  public void testDeleteSession(TestContext should) {
    final Async test = should.async();

    Session session = store.createSession(30_000);
    String value = session.value();

    store.put(session)
      .compose(atrVoid -> store.size())
      .map(size -> {
        should.assertEquals(1, size);
        return size;
      })
      .compose(size -> store.delete(value))
      .compose(atrVoid -> store.size())
      .onComplete(res -> {
        should.assertTrue(res.succeeded());
        should.assertEquals(0, res.result());
        test.complete();
      });
  }

  @Test(timeout = 10_000)
  public void testFloodConnection(TestContext should) {
    final Async test = should.async();

    Session session = store.createSession(30_000);
    // even though we only allow 2 connections we configured
    // the system to queue up to 32 so this should not be a problem
    CompositeFuture.all(
      store.put(session),
      store.put(session),
      store.put(session),
      store.put(session),
      store.put(session),
      store.put(session)
    ).onComplete(res -> {
      should.assertTrue(res.succeeded());
      test.complete();
    });
  }
}
