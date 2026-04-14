package io.vertx.ext.web.sstore.caffeine.tests;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.caffeine.CaffeineSessionStore;
import io.vertx.junit5.VertxTest;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:lazarbulic@gmail.com">Lazar Bulic</a>
 */
@VertxTest
@Timeout(10)
public class CaffeineSessionStoreTest {

  private SessionStore store;

  @BeforeEach
  public void before(Vertx vertx) {
    store = CaffeineSessionStore.create(vertx);
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
