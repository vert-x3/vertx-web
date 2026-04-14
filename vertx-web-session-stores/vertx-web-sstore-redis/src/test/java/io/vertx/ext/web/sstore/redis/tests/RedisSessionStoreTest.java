package io.vertx.ext.web.sstore.redis.tests;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.sstore.redis.RedisSessionStore;
import io.vertx.junit5.VertxTest;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import org.junit.jupiter.api.*;

import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import org.testcontainers.containers.GenericContainer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="https://github.com/llfbandit">Rémy Noël</a>
 */
@VertxTest
@Timeout(10)
public class RedisSessionStoreTest {

  static GenericContainer<?> container = new GenericContainer<>("redis:5.0")
    .withExposedPorts(6379);

  @BeforeAll
  public static void startContainer() {
    container.start();
  }

  @AfterAll
  public static void stopContainer() {
    container.stop();
  }

  private SessionStore store;

  @BeforeEach
  public void before(Vertx vertx) {
    store = RedisSessionStore.create(
      vertx,
      Redis.createClient(vertx, new RedisOptions()
        .setConnectionString("redis://" + container.getHost() + ":" + container.getMappedPort(6379))
        .setMaxPoolSize(2)
        .setMaxPoolWaiting(32)));
  }

  @AfterEach
  public void after() {
    store.clear().await();
  }

  @Test
  public void testPutSession() {
    Session session = store.createSession(30_000);
    store.put(session).await();
  }

  @Test
  public void testGetSession() {
    Session session = store.createSession(30_000);
    String value = session.value();

    store.put(session).await();
    Session sessionGet = store.get(value).await();
    assertEquals(value, sessionGet.value());
  }

  @Test
  public void testClearSession() {
    Session session = store.createSession(30_000);
    store.put(session).await();
    store.clear().await();
  }

  @Test
  public void testSizeSession() {
    Session session = store.createSession(30_000);

    store.put(session).await();
    int size = store.size().await();
    assertEquals(1, size);

    store.clear().await();
    size = store.size().await();
    assertEquals(0, size);
  }

  @Test
  public void testDeleteSession() {
    Session session = store.createSession(30_000);
    String value = session.value();

    store.put(session).await();
    int size = store.size().await();
    assertEquals(1, size);

    store.delete(value).await();
    size = store.size().await();
    assertEquals(0, size);
  }

  @Test
  public void testFloodConnection() {
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
    ).await();
  }
}
