/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.text.DateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class SessionHandlerTestBase extends WebTestBase {

  protected SessionStore store;

  @Test
  public void testSessionCookieName() throws Exception {
    router.route().handler(CookieHandler.create());
    String sessionCookieName = "acme.sillycookie";
    router.route().handler(SessionHandler.create(store).setSessionCookieName(sessionCookieName));
    router.route().handler(rc -> rc.response().end());
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertTrue(setCookie.startsWith(sessionCookieName + "="));
    }, 200, "OK", null);
  }

  @Test
  public void testSessionCookieHttpOnlyFlag() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store).setCookieHttpOnlyFlag(true));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");

      assertTrue(setCookie.contains("; HTTPOnly"));
    }, 200, "OK", null);
  }

  @Test
  public void testSessionCookieSecureFlag() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store).setCookieSecureFlag(true));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");

      assertTrue(setCookie.contains("; Secure"));
    }, 200, "OK", null);
  }

  @Test
  public void testSessionCookieSecureFlagAndHttpOnlyFlags() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store).setCookieSecureFlag(true).setCookieHttpOnlyFlag(true));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");

      assertTrue(setCookie.contains("; Secure"));
      assertTrue(setCookie.contains("; HTTPOnly"));
    }, 200, "OK", null);
  }

  @Test
  public void testSessionFields() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));
    AtomicReference<String> rid = new AtomicReference<>();
    router.route().handler(rc -> {
      Session sess = rc.session();
      assertNotNull(sess);
      assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
      assertNotNull(sess.id());
      rid.set(sess.id());
      assertFalse(sess.isDestroyed());
      assertEquals(SessionHandler.DEFAULT_SESSION_TIMEOUT, sess.timeout());
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertTrue(setCookie.startsWith(SessionHandler.DEFAULT_SESSION_COOKIE_NAME + "="));
      int pos = setCookie.indexOf("; Path=/");
      String sessID = setCookie.substring(18, pos);
      assertEquals(rid.get(), sessID);
    }, 200, "OK", null);
  }

  @Test
  public void testSession() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));
    AtomicReference<String> rid = new AtomicReference<>();
    AtomicInteger requestCount = new AtomicInteger();
    router.route().handler(rc -> {
      Session sess = rc.session();
      assertNotNull(sess);
      assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
      assertNotNull(sess.id());
      switch (requestCount.get()) {
        case 0:
          rid.set(sess.id());
          sess.put("foo", "bar");
          break;
        case 1:
          assertEquals(rid.get(), sess.id());
          assertEquals("bar", sess.get("foo"));
          sess.put("eek", "wibble");
          break;
        case 2:
          assertEquals(rid.get(), sess.id());
          assertEquals("bar", sess.get("foo"));
          assertEquals("wibble", sess.get("eek"));
      }
      requestCount.incrementAndGet();
      rc.response().end();
    });
    AtomicReference<String> rSetCookie = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      rSetCookie.set(setCookie);
    }, 200, "OK", null);
    Thread.sleep(1000);
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", rSetCookie.get()), null, 200, "OK", null);
    Thread.sleep(1000);
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", rSetCookie.get()), null, 200, "OK", null);
  }

  @Test
  public void testSessionExpires() throws Exception {
    router.route().handler(CookieHandler.create());
    long timeout = 1000;
    router.route().handler(SessionHandler.create(store).setSessionTimeout(timeout));
    AtomicReference<String> rid = new AtomicReference<>();
    AtomicInteger requestCount = new AtomicInteger();
    router.route().handler(rc -> {
      Session sess = rc.session();
      assertNotNull(sess);
      assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
      assertNotNull(sess.id());
      switch (requestCount.get()) {
        case 0:
          rid.set(sess.id());
          sess.put("foo", "bar");
          break;
        case 1:
          assertFalse(rid.get().equals(sess.id())); // New session
          assertNull(sess.get("foo"));
          break;
      }
      requestCount.incrementAndGet();
      rc.response().end();
    });
    AtomicReference<String> rSetCookie = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      rSetCookie.set(setCookie);
    }, 200, "OK", null);
    Thread.sleep(2 * (LocalSessionStore.DEFAULT_REAPER_INTERVAL + timeout));
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", rSetCookie.get()), null, 200, "OK", null);
    CountDownLatch latch1 = new CountDownLatch(1);
    Thread.sleep(500); // FIXME -Needed because session.destroy is async :(
    store.size(onSuccess(res -> {
      assertEquals(1, res.intValue());
      latch1.countDown();
    }));
    awaitLatch(latch1);
    Thread.sleep(2 * (LocalSessionStore.DEFAULT_REAPER_INTERVAL + timeout));
    CountDownLatch latch2 = new CountDownLatch(1);
    store.size(onSuccess(res -> {
      assertEquals(0, res.intValue());
      latch2.countDown();
    }));
    awaitLatch(latch2);
  }

  @Test
  public void testDestroySession() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));
    AtomicReference<String> rid = new AtomicReference<>();
    AtomicInteger requestCount = new AtomicInteger();
    router.route().handler(rc -> {
      Session sess = rc.session();
      assertNotNull(sess);
      assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
      assertNotNull(sess.id());
      switch (requestCount.get()) {
        case 0:
          rid.set(sess.id());
          sess.put("foo", "bar");
          sess.destroy();
          break;
        case 1:
          assertFalse(rid.get().equals(sess.id())); // New session
          assertNull(sess.get("foo"));
          sess.destroy();
          break;
      }
      requestCount.incrementAndGet();
      rc.response().end();
    });
    AtomicReference<String> rSetCookie = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      rSetCookie.set(setCookie);
    }, 200, "OK", null);
    testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", rSetCookie.get()), null, 200, "OK", null);
    CountDownLatch latch1 = new CountDownLatch(1);
    store.size(onSuccess(res -> {
      assertEquals(0, res.intValue());
      latch1.countDown();
    }));
    awaitLatch(latch1);
  }

  @Test
  public void testLastAccessed1() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));
    AtomicReference<Session> rid = new AtomicReference<>();
    long start = System.currentTimeMillis();
    router.route().handler(rc -> {
      rid.set(rc.session());
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    assertTrue(rid.get().lastAccessed() - start < 500);
    start = System.currentTimeMillis();
    Thread.sleep(1000);
    testRequest(HttpMethod.GET, "/", 200, "OK");
    assertTrue(rid.get().lastAccessed() - start >= 1000);
  }

  @Test
  public void testLastAccessed2() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));
    AtomicReference<Session> rid = new AtomicReference<>();
    router.route().handler(rc -> {
      rid.set(rc.session());
      rc.session().put("foo", "bar");
      vertx.setTimer(1000, tid -> rc.response().end());
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    // accessed() is called after request too
    assertTrue(rid.get().lastAccessed() - System.currentTimeMillis() < 500);
  }

  @Test
  public void testIssue172_setnull() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));
    AtomicReference<Session> rid = new AtomicReference<>();

    router.route().handler(rc -> {
      rid.set(rc.session());
      rc.session().put("foo", null);
      vertx.setTimer(1000, tid -> rc.response().end());
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testSessionCookieAttack() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));
    // faking that there was some auth error
    router.route().handler(rc -> rc.fail(401));

    testRequest(HttpMethod.GET, "/", null, resp -> assertNull(resp.headers().get("set-cookie")), 401, "Unauthorized", null);
  }

  private final DateFormat dateTimeFormatter = Utils.createRFC1123DateTimeFormatter();

  protected long doTestSessionRetryTimeout() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));
    AtomicReference<Session> rid = new AtomicReference<>();

    router.get("/0").handler(rc -> {
      rid.set(rc.session());
      rc.session().put("foo", "foo_value");
      rc.response().end();
    });
    router.get("/1").handler(rc -> {
      rid.set(rc.session());
      assertEquals("foo_value", rc.session().get("foo"));
      rc.session().destroy();
      rc.response().end();
    });
    router.get("/2").handler(rc -> {
      rid.set(rc.session());
      assertEquals(null, rc.session().<String>get("foo"));
      rc.response().end();
    });

    AtomicReference<String> sessionID = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/0", req -> {}, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      sessionID.set(setCookie);
    }, 200, "OK", null);
    testRequest(HttpMethod.GET, "/1", req -> req.putHeader("cookie", sessionID.get()), 200, "OK", null);
    long now = System.currentTimeMillis();
    testRequest(HttpMethod.GET, "/2", req -> req.putHeader("cookie", sessionID.get()), 200, "OK", null);
    return System.currentTimeMillis() - now;
  }

  @Test
  public void testSessionFixation() throws Exception {

    final AtomicReference<String> sessionId = new AtomicReference<>();

    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));
    // call #0 anonymous a random id should be returned
    router.route("/0").handler(rc -> {
      sessionId.set(rc.session().id());
      rc.response().end();
    });
    // call #1 fake auth security upgrade is done so session id must change
    router.route("/1").handler(rc -> {
      // previous id must match
      assertEquals(sessionId.get(), rc.session().id());
      rc.session().regenerateId();
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/0", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertNotNull(setCookie);
    }, 200, "OK", null);

    testRequest(HttpMethod.GET, "/1", req -> req.putHeader("cookie", "vertx-web.session=" + sessionId.get() + "; Path=/"), resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertNotNull(setCookie);
      assertFalse(("vertx-web.session=" + sessionId.get() + "; Path=/").equals(setCookie));
    }, 200, "OK", null);

    CountDownLatch latch = new CountDownLatch(1);
    // after the id is regenerated the old id must not be valid anymore
    store.get(sessionId.get(), get -> {
      assertTrue(get.succeeded());
      assertNull(get.result());
      latch.countDown();
    });

    awaitLatch(latch);
  }

  @Test
  public void testSessionIdLength() throws Exception {

    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(store));

    router.route("/1").handler(rc -> {
      // previous id must match
      assertFalse("abc".equals(rc.session().id()));
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/1", req -> req.putHeader("cookie", "vertx-web.session=abc; Path=/"), resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertNotNull(setCookie);
    }, 200, "OK", null);
  }
}
