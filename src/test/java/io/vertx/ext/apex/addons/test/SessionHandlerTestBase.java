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

package io.vertx.ext.apex.addons.test;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.addons.impl.LocalSessionStore;
import io.vertx.ext.apex.addons.impl.SessionHandler;
import io.vertx.ext.apex.core.CookieHandler;
import io.vertx.ext.apex.core.Session;
import io.vertx.ext.apex.core.SessionStore;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class SessionHandlerTestBase extends ApexTestBase {

  protected SessionStore store;

  @Test
  public void testSessionCookieDefaultExpires() throws Exception {
    router.route().handler(CookieHandler.cookieHandler());
    router.route().handler(SessionHandler.sessionHandler(store));
    testSessionCookieExpires(SessionHandler.DEFAULT_COOKIE_MAX_AGE);
  }

  @Test
  public void testSessionCookieExpires() throws Exception {
    router.route().handler(CookieHandler.cookieHandler());
    long expires = 123456;
    router.route().handler(SessionHandler.sessionHandler(SessionHandler.DEFAULT_SESSION_COOKIE_NAME, expires,
                           SessionHandler.DEFAULT_SESSION_TIMEOUT, store));
    testSessionCookieExpires(expires);
  }

  private void testSessionCookieExpires(long expires) throws Exception {
    router.route().handler(rc -> {
      rc.response().end();
    });
    long now = System.currentTimeMillis();
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertTrue(setCookie.startsWith(SessionHandler.DEFAULT_SESSION_COOKIE_NAME + "="));
      int pos = setCookie.indexOf("; Expires=");
      assertTrue(pos != -1);
      String expiresString = setCookie.substring(pos + 10);
      try {
        Date date = DATE_TIME_FORMATTER.parse(expiresString);
        long diff = date.getTime() - now - expires * 1000;
        assertTrue(diff < 1000);
      } catch (ParseException e) {
        fail(e.getMessage());
      }

    }, 200, "OK", null);
  }

  @Test
  public void testSessionCookieName() throws Exception {
    router.route().handler(CookieHandler.cookieHandler());
    String sessionCookieName = "acme.sillycookie";
    router.route().handler(SessionHandler.sessionHandler(sessionCookieName, SessionHandler.DEFAULT_COOKIE_MAX_AGE,
                                                         SessionHandler.DEFAULT_SESSION_TIMEOUT, store));
    router.route().handler(rc -> {
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertTrue(setCookie.startsWith(sessionCookieName + "="));
    }, 200, "OK", null);
  }

  @Test
  public void testSessionFields() throws Exception {
    router.route().handler(CookieHandler.cookieHandler());
    router.route().handler(SessionHandler.sessionHandler(store));
    AtomicReference<String> rid = new AtomicReference<>();
    router.route().handler(rc -> {
      Session sess = rc.session();
      assertNotNull(sess);
      assertTrue(System.currentTimeMillis() - sess.lastAccessed() < 500);
      assertNotNull(sess.id());
      rid.set(sess.id());
      assertFalse(sess.isDestroyed());
      assertSame(store, sess.sessionStore());
      assertEquals(SessionHandler.DEFAULT_SESSION_TIMEOUT, sess.timeout());
      JsonObject data = sess.data();
      assertNotNull(data);
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertTrue(setCookie.startsWith(SessionHandler.DEFAULT_SESSION_COOKIE_NAME + "="));
      int pos = setCookie.indexOf("; Expires=");
      String sessID = setCookie.substring(13, pos);
      assertEquals(rid.get(), sessID);
    }, 200, "OK", null);
  }

  @Test
  public void testSession() throws Exception {
    router.route().handler(CookieHandler.cookieHandler());
    router.route().handler(SessionHandler.sessionHandler(store));
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
          sess.data().put("foo", "bar");
          break;
        case 1:
          assertEquals(rid.get(), sess.id());
          assertEquals("bar", sess.data().getString("foo"));
          sess.data().put("eek", "wibble");
          break;
        case 2:
          assertEquals(rid.get(), sess.id());
          assertEquals("bar", sess.data().getString("foo"));
          assertEquals("wibble", sess.data().getString("eek"));
      }
      requestCount.incrementAndGet();
      rc.response().end();
    });
    AtomicReference<String> rSetCookie = new AtomicReference<>();
    testRequest(HttpMethod.GET, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      rSetCookie.set(setCookie);
    }, 200, "OK", null);
    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("cookie", rSetCookie.get());
    }, null, 200, "OK", null);
    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("cookie", rSetCookie.get());
    }, null, 200, "OK", null);
  }

  @Test
  public void testSessionExpires() throws Exception {
    router.route().handler(CookieHandler.cookieHandler());
    long timeout = 1;
    router.route().handler(SessionHandler.sessionHandler(SessionHandler.DEFAULT_SESSION_COOKIE_NAME, SessionHandler.DEFAULT_COOKIE_MAX_AGE,
      timeout, store));
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
          sess.data().put("foo", "bar");
          break;
        case 1:
          assertFalse(rid.get().equals(sess.id())); // New session
          assertNull(sess.data().getString("foo"));
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
    Thread.sleep(2 * (LocalSessionStore.DEFAULT_REAPER_PERIOD + timeout * 1000));
    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("cookie", rSetCookie.get());
    }, null, 200, "OK", null);
    CountDownLatch latch1 = new CountDownLatch(1);
    Thread.sleep(500); // FIXME -Needed because session.destroy is async :(
    store.size(onSuccess(res -> {
      assertEquals(1, res.intValue());
      latch1.countDown();
    }));
    awaitLatch(latch1);
    Thread.sleep(2 * (LocalSessionStore.DEFAULT_REAPER_PERIOD + timeout * 1000));
    CountDownLatch latch2 = new CountDownLatch(1);
    store.size(onSuccess(res -> {
      assertEquals(0, res.intValue());
      latch2.countDown();
    }));
    awaitLatch(latch2);
  }

  @Test
  public void testDestroySession() throws Exception {
    router.route().handler(CookieHandler.cookieHandler());
    router.route().handler(SessionHandler.sessionHandler(store));
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
          sess.data().put("foo", "bar");
          sess.destroy();
          break;
        case 1:
          assertFalse(rid.get().equals(sess.id())); // New session
          assertNull(sess.data().getString("foo"));
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
    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("cookie", rSetCookie.get());
    }, null, 200, "OK", null);
    CountDownLatch latch1 = new CountDownLatch(1);
    store.size(onSuccess(res -> {
      assertEquals(0, res.intValue());
      latch1.countDown();
    }));
    awaitLatch(latch1);
  }

  @Test
  public void testLastAccessed1() throws Exception {
    router.route().handler(CookieHandler.cookieHandler());
    router.route().handler(SessionHandler.sessionHandler(store));
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
    router.route().handler(CookieHandler.cookieHandler());
    router.route().handler(SessionHandler.sessionHandler(store));
    AtomicReference<Session> rid = new AtomicReference<>();
    router.route().handler(rc -> {
      rid.set(rc.session());
      rc.session().data().put("foo", "bar");
      vertx.setTimer(1000, tid -> rc.response().end());
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    // accessed() is called after request too
    assertTrue(rid.get().lastAccessed() - System.currentTimeMillis() < 500);
  }

  private final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
  {
    DATE_TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

}
