package io.vertx.ext.web.sstore.redis;

import org.junit.Test;

import io.vertx.core.CompositeFuture;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.redis.client.RedisOptions;
import io.vertx.test.core.VertxTestBase;

/**
 * @author <a href="https://github.com/llfbandit">Rémy Noël</a>
 */
public class RedisSessionStoreTest extends VertxTestBase {
  private SessionStore store;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    store = RedisSessionStore.create(vertx, new RedisOptions());
  }

  @Test
  public void testPutSession() {
    Session session = store.createSession(30_000);

    store.put(session).setHandler(res -> {
      if (res.failed()) {
        fail(res.cause());
      } else {
        testComplete();
      }
    });

    await();
  }

  @Test
  public void testGetSession() {
    Session session = store.createSession(30_000);
    String value = session.value();

    store.put(session)
      .compose(aVoid -> store.get(value))
      .map(sessionGet -> {
        assertEquals(value, sessionGet.value());
        return null;
      })
      .setHandler(res -> {
        if (res.failed()) {
          fail(res.cause());
        } else {
          testComplete();
        }
      });

    await();
  }

  @Test
  public void testClearSession() {
    Session session = store.createSession(30_000);

    store.put(session)
      .compose(aVoid -> store.clear())
      .setHandler(res -> {
        if (res.failed()) {
          fail(res.cause());
        } else {
          testComplete();
        }
      });

    await();
  }

  @Test
  public void testSizeSession() {
    Session session = store.createSession(30_000);

    store.put(session)
      .compose(atrVoid -> store.size())
      .map(size -> {
        assertTrue(1 == size);
        return size;
      })
      .compose(size -> store.clear())
      .compose(atrVoid -> store.size())
      .setHandler(res -> {
        if (res.failed()) {
          fail(res.cause());
        } else {
          assertTrue(0 == res.result());
          testComplete();
        }
      });

    await();
  }

  @Test
  public void testDeleteSession() {
    Session session = store.createSession(30_000);
    String value = session.value();

    store.put(session)
      .compose(atrVoid -> store.size())
      .map(size -> {
        assertTrue(1 == size);
        return size;
      })
      .compose(size -> store.delete(value))
      .compose(atrVoid -> store.size())
      .setHandler(res -> {
        if (res.failed()) {
          fail(res.cause());
        } else {
          assertTrue(0 == res.result());
          testComplete();
        }
      });

    await();
  }

  @Test
  public void testFloodConnection() {
    Session session = store.createSession(30_000);

    CompositeFuture.all(
      store.put(session),
      store.put(session),
      store.put(session),
      store.put(session),
      store.put(session),
      store.put(session)
    ).setHandler(res -> {
      if (res.failed()) {
        fail(res.cause());
      } else {
        testComplete();
      }
    });

    await();
  }

  @Override
  protected void testComplete() {
    store.clear().setHandler(res -> super.testComplete());
  }
}
