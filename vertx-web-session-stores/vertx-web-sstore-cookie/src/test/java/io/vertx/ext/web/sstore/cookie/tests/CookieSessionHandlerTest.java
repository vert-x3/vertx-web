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

package io.vertx.ext.web.sstore.cookie.tests;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.cookie.CookieSessionStore;
import io.vertx.ext.web.tests.handler.SessionHandlerTestBase;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class CookieSessionHandlerTest extends SessionHandlerTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    store = CookieSessionStore.create(vertx, "KeyboardCat!");
  }

  @Test
  public void testGetSession() throws Exception {
    Session session = store.createSession(30_000);
    String cookieId = session.id();

    store.get(session.value()).onComplete(get -> {
      if (get.failed()) {
        fail(get.cause());
      } else {
        assertNotNull(get.result());
        assertEquals(cookieId, get.result().id());
        testComplete();
      }
    });

    await();
  }

  @Test
  public void testParse() {
    Session session = store.createSession(30_000);
    String cookieValue = session.value();

    store.get(cookieValue).onComplete(get -> {
      if (get.failed()) {
        fail(get.cause());
      } else {
        assertNotNull(get.result());
        // the session id must be the same
        assertEquals(session.id(), get.result().id());
        // the session value will not be the same as IV is random
        assertFalse(cookieValue.equals(get.result().value()));
        testComplete();
      }
    });

    await();
  }

  /**
   * This test overrides the original as at the end there is no way to guarantee that the session cannot be
   * reused as Cookies do not preserve state across clients
   */
  @Test
  @Override
  public void testSessionFixation() throws Exception {

    final AtomicReference<String> session = new AtomicReference<>();
    final AtomicReference<String> sessionId = new AtomicReference<>();

    router.route().handler(SessionHandler.create(store));
    // call #0 anonymous a random id should be returned
    router.route("/0").handler(rc -> {
      sessionId.set(rc.session().id());
      // store the session value to be used in the next call
      session.set(rc.session().value());
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

    testRequest(HttpMethod.GET, "/1", req -> req.putHeader("cookie", "vertx-web.session=" + session.get() + "; Path=/"), resp -> {
      String setCookie = resp.headers().get("set-cookie");
      assertNotNull(setCookie);
      assertFalse(("vertx-web.session=" + session.get() + "; Path=/").equals(setCookie));
    }, 200, "OK", null);
  }

  @Test
  public void testInterStoreCommunication() throws Exception {
    CookieSessionStore store1 = CookieSessionStore.create(vertx, "KeyboardCat!");
    CookieSessionStore store2 = CookieSessionStore.create(vertx, "KeyboardCat!");

    Session session = store1.createSession(30_000);
    String cookieValue = session.value();

    store2.get(cookieValue).onComplete(get -> {
      if (get.failed()) {
        fail(get.cause());
      } else {
        assertNotNull(get.result());
        // the session id must be the same
        assertEquals(session.id(), get.result().id());
        // the session value will not be the same as IV is random
        assertFalse(cookieValue.equals(get.result().value()));
        testComplete();
      }
    });
    await();
  }

  /**
   * We explicitly ignore this test as there is no backend to assert that the cookie is removed.
   *
   * @throws Exception
   */
  @Test
  @Ignore
  @Override
  public void testSessionExpires() throws Exception {
  }

  /**
   * We explicitly ignore this test as there is value on signing a encrypted payload.
   *
   * @throws Exception
   */
  @Test
  @Ignore
  @Override
  public void testSessionCookieSigning() throws Exception {
  }

  @Test
  @Override
	public void testSessionFields() throws Exception {
		router.route().handler(SessionHandler.create(store));
		AtomicReference<String> rid = new AtomicReference<>();
		AtomicReference<String> rsession = new AtomicReference<>();
		router.route("/").handler(rc -> {
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
			int pos = setCookie.indexOf("; Path=" + SessionHandler.DEFAULT_SESSION_COOKIE_PATH);
			rsession.set(setCookie.substring(18, pos));
		}, 200, "OK", null);


      store.get(rsession.get()).onComplete(get -> {
        if (get.failed()) {
          fail(get.cause());
        } else {
          assertNotNull(get.result());
          assertEquals(rid.get(), get.result().id());
          testComplete();
        }
      });

      await();
  }
}
