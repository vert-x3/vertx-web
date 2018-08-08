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

package io.vertx.ext.web.sstore.cookie;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.SessionHandlerTestBase;
import io.vertx.ext.web.sstore.SessionStore;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
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
    String cookieValue = session.value();

    store.get(cookieValue, get -> {
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

    store.get(cookieValue, get -> {
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

    router.route().handler(CookieHandler.create());
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
