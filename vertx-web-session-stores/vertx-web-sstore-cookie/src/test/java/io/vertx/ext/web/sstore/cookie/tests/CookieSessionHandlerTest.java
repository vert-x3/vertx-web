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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;
import io.vertx.ext.web.sstore.cookie.CookieSessionStore;
import io.vertx.ext.web.tests.handler.SessionHandlerTestBase;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class CookieSessionHandlerTest extends SessionHandlerTestBase {

  public CookieSessionHandlerTest() {
    super(ReportMode.STATELESS);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    store = CookieSessionStore.create(vertx, "KeyboardCat!");
  }

  @Test
  public void testSessionAndUser() throws Exception {
    router.route().handler(BodyHandler.create());
    router.route().handler(SessionHandler.create(store));
    router.route("/authenticate").handler(SimpleAuthenticationHandler.create().authenticate(rc -> {
      JsonObject body = rc.body().asJsonObject();
      if (body.getString("username").equals("myuser")) {
        return Future.succeededFuture(User.create(body));
      } else {
        return Future.failedFuture(new HttpException(401));
      }
    })).handler(rc -> {
      if (rc.normalizedPath().equals("/authenticate")) {
        rc.response().end("Authenticated");
      } else {
        rc.next();
      }
    });
    router.route("/private/*").handler(rc -> {
      if (rc.user() == null) {
        rc.response().setStatusCode(401).end();
      } else {
        rc.next();
      }
    });
    router.get("/private/whoami").handler(rc -> rc.end(rc.user().principal().encode()));

    testRequest(HttpMethod.GET, "/private/whoami", HttpResponseStatus.UNAUTHORIZED);

    JsonObject myuser = new JsonObject().put("username", "myuser").put("password", System.currentTimeMillis());
    AtomicReference<String> cookie = new AtomicReference<>();
    HttpResponse<Buffer> resp = testRequest(webClient.post("/authenticate")
      .putHeader("content-type", "application/json")
      .sendBuffer(Buffer.buffer(myuser.toString())), 200, "OK");
    String setCookie = resp.headers().get("set-cookie");
    cookie.set(setCookie.substring(0, setCookie.indexOf(";")));
    assertEquals("Authenticated", resp.bodyAsString());
    assertNotNull(cookie.get());

    HttpResponse<Buffer> resp2 = testRequest(webClient.get("/private/whoami")
      .putHeader("cookie", cookie.get())
      .send(), 200, "OK");
    assertEquals(myuser, resp2.bodyAsJsonObject());
  }

  @Test
  public void testGetSession() throws Exception {
    Session session = store.createSession(30_000);
    String cookieId = session.id();

    store
      .get(session.value())
      .onComplete(onSuccess(c -> {
        assertNotNull(c);
        assertEquals(cookieId, c.id());
        testComplete();
    }));

    await();
  }

  @Test
  public void testParse() {
    Session session = store.createSession(30_000);
    String cookieValue = session.value();

    store
      .get(cookieValue)
      .onComplete(onSuccess(c -> {
        assertNotNull(c);
        // the session id must be the same
        assertEquals(session.id(), c.id());
        // the session value will not be the same as IV is random
        assertFalse(cookieValue.equals(c.value()));
        testComplete();
    }));

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

    HttpResponse<Buffer> resp = testRequest(webClient.get("/0").send(), 200, "OK");
    String setCookie = resp.headers().get("set-cookie");
    assertNotNull(setCookie);

    HttpResponse<Buffer> resp2 = testRequest(webClient.get("/1")
      .putHeader("cookie", "vertx-web.session=" + session.get() + "; Path=/")
      .send(), 200, "OK");
    String setCookie2 = resp2.headers().get("set-cookie");
    assertNotNull(setCookie2);
    assertFalse(("vertx-web.session=" + session.get() + "; Path=/").equals(setCookie2));
  }

  @Test
  public void testInterStoreCommunication() throws Exception {
    CookieSessionStore store1 = CookieSessionStore.create(vertx, "KeyboardCat!");
    CookieSessionStore store2 = CookieSessionStore.create(vertx, "KeyboardCat!");

    Session session = store1.createSession(30_000);
    String cookieValue = session.value();

    store2
      .get(cookieValue)
      .onComplete(onSuccess(c -> {
        assertNotNull(c);
        // the session id must be the same
        assertEquals(session.id(), c.id());
        // the session value will not be the same as IV is random
        assertFalse(cookieValue.equals(c.value()));
        testComplete();
    }));
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
   * We explicitly ignore this test as there is no value on signing an encrypted payload.
   * The cookie is signed with AES/GCM which has built-in integrity checks.
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
		HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
		String setCookie = resp.headers().get("set-cookie");
		assertTrue(setCookie.startsWith(SessionHandler.DEFAULT_SESSION_COOKIE_NAME + "="));
		int pos = setCookie.indexOf("; Path=" + SessionHandler.DEFAULT_SESSION_COOKIE_PATH);
		rsession.set(setCookie.substring(18, pos));


      store
        .get(rsession.get())
        .onComplete(onSuccess(c -> {
          assertNotNull(c);
          assertEquals(rid.get(), c.id());
          testComplete();
      }));

      await();
  }
}
