package io.vertx.ext.web.sstore.caffeine.tests;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.caffeine.CaffeineSessionStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:lazarbulic@gmail.com">Lazar Bulic</a>
 */
@RunWith(VertxUnitRunner.class)
public class CaffeineSessionStoreTest {

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  private SessionStore store;

  @Before
  public void before() {
    store = CaffeineSessionStore.create(rule.vertx());
  }

  @After
  public void after(TestContext should) {
    final Async test = should.async();
    store.clear().onComplete(clear -> {
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
    Future.all(
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
