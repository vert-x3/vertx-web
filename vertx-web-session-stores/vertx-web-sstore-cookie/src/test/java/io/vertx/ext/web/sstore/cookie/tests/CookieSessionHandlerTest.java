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

  @Override
  public void setUp() throws Exception {
    super.setUp();
    store = CookieSessionStore.create(vertx, "KeyboardCat!", Buffer.buffer("salt"));
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
    testRequest(HttpMethod.POST, "/authenticate",
      req -> req.putHeader("content-type", "application/json").send(myuser.toString()),
      resp -> {
        String setCookie = resp.headers().get("set-cookie");
        cookie.set(setCookie.substring(0, setCookie.indexOf(";")));
        resp.body().compose(body -> {
          assertEquals("Authenticated", body.toString());
          return Future.succeededFuture();
        }).onFailure(this::fail);
      }, 200, "OK", null);
    assertNotNull(cookie.get());

    testRequest(HttpMethod.GET, "/private/whoami", req -> {
      req.putHeader("cookie", cookie.get());
    }, resp -> {
      resp.body().compose(body -> {
        assertEquals(myuser, body.toJsonObject());
        return Future.succeededFuture();
      }).onFailure(this::fail);
    }, 200, "OK", null);
  }

  @Test
  public void testGetSession() throws Exception {
    Session session = store.createSession(30_000);
    String cookieValue = session.value();

    store.get(cookieValue).onComplete(get -> {
      if (get.failed()) {
        fail(get.cause());
      } else {
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
      assertEquals(cookieValue, get.result().value());
      testComplete();
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

    final AtomicReference<String> sessionId = new AtomicReference<>();

    router.route().handler(SessionHandler.create(store));
    // call #0 anonymous a random id should be returned
    router.route("/0").handler(rc -> {
      sessionId.set(rc.session().value());
      rc.response().end();
    });
    // call #1 fake auth security upgrade is done so session id must change
    router.route("/1").handler(rc -> {
      // previous id must match
      assertEquals(sessionId.get(), rc.session().value());
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
    super.testSessionExpires();
  }

}
