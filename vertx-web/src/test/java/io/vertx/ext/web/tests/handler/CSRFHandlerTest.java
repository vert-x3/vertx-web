/*
 * Copyright 2024 Red Hat, Inc.
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

package io.vertx.ext.web.tests.handler;

import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class CSRFHandlerTest extends WebTestBase {

  @AfterClass
  public static void oneTimeTearDown() throws IOException {
    cleanupFileUploadDir();
  }

  @Test
  public void testGetCookie() throws Exception {

    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.get().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(1, cookies.size());
      assertEquals(CSRFHandler.DEFAULT_COOKIE_NAME, cookies.get(0).substring(0, cookies.get(0).indexOf('=')));
    }, 200, "OK", null);
  }

  @Test
  public void testPostWithoutHeader() throws Exception {
    router.route()
      .handler(BodyHandler.create())
      .handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());
    router.errorHandler(403, rc -> testComplete());

    testRequest(HttpMethod.POST, "/", null, null, 403, "Forbidden", null);

    await();
  }

  private final Map<String, String> cookieJar = Collections.synchronizedMap(new HashMap<>());

  private void storeCookies(HttpClientResponse resp) {
    for (String value : resp.headers().getAll("set-cookie")) {
      int eq = value.indexOf('=');
      String cookieName = value.substring(0, eq);
      int sc = value.indexOf(';', eq + 1);
      String cookieValue = value.substring(eq + 1, sc > 0 ? sc : value.length());
      cookieJar.put(cookieName, cookieValue);
    }
  }

  private String encodeCookies() {
    return cookieJar.entrySet().stream()
      .map(cookie -> cookie.getKey() + "=" + cookie.getValue())
      .collect(joining(";"));
  }

  @Test
  public void testPostWithHeader() throws Exception {
    router.route().handler(StaticHandler.create());
    router.route("/xsrf").handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route("/xsrf").handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/xsrf", null, this::storeCookies, 200, "OK", null);

    testRequest(HttpMethod.POST, "/xsrf", req -> {
      req.headers()
        .set(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
        .set("Cookie", encodeCookies());
    }, null, 200, "OK", null);
  }

  @Test
  public void testPostWithExpiredCookie() throws Exception {
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra").setTimeout(1));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.POST, "/", req -> req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME,
      "4CYp9vQsr2VSQEsi/oVsMu35Ho9TlR0EovcYovlbiBw=.1437037602082.41jwU0FPl/n7ZNZAZEA07GyIUnpKSTKQ8Eju7Nicb34="), null, 403, "Forbidden", null);
  }

  @Test
  public void testPostWithFormAttribute() throws Exception {

    // since we are working with forms we need the body handler to be present
    router.route().handler(BodyHandler.create());
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, this::storeCookies, 200, "OK", null);

    testRequest(HttpMethod.POST, "/", req -> {
      // create an HTTP form
      String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
      Buffer buffer = Buffer.buffer();
      String str =
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + CSRFHandler.DEFAULT_HEADER_NAME + "\"\r\n\r\n" + cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME) + "\r\n" +
          "--" + boundary + "--\r\n";
      buffer.appendString(str);
      req.headers().set("content-length", String.valueOf(buffer.length()));
      req.headers().set("content-type", "multipart/form-data; boundary=" + boundary);
      req.putHeader("Cookie", encodeCookies());
      req.write(buffer);
    }, null, 200, "OK", null);
  }

  @Test
  public void testPostWithFormAttributeWithoutCookies() throws Exception {

    // since we are working with forms we need the body handler to be present
    router.route().handler(BodyHandler.create());
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> {
      String token = rc.get(CSRFHandler.DEFAULT_HEADER_NAME);
      if (token != null) {
        rc.response().end(token);
      } else {
        rc.response().end();
      }
    });

    // we need to wait parsing the response body
    CountDownLatch latch = new CountDownLatch(1);

    testRequest(HttpMethod.GET, "/", null, resp -> {
      resp.bodyHandler(buffer -> {
        cookieJar.put(CSRFHandler.DEFAULT_COOKIE_NAME, buffer.toString());
        latch.countDown();
      });

      // There is always 1 cookie (csrf)
      assertEquals(1, resp.headers().getAll("set-cookie").size());
    }, 200, "OK", null);

    // response body is known
    awaitLatch(latch);

    // will fail as the cookie is always required to be validated!
    testRequest(HttpMethod.POST, "/", req -> {
      // create an HTTP form
      String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
      Buffer buffer = Buffer.buffer();
      String str =
        "--" + boundary + "\r\n" +
        "Content-Disposition: form-data; name=\"" + CSRFHandler.DEFAULT_HEADER_NAME + "\"\r\n\r\n" + cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME) + "\r\n" +
          "--" + boundary + "--\r\n";
      buffer.appendString(str);
      req.headers().set("content-length", String.valueOf(buffer.length()));
      req.headers().set("content-type", "multipart/form-data; boundary=" + boundary);
      req.write(buffer);
    }, null, 403, "Forbidden", null);
  }

  @Test
  public void testPostWithCustomResponseBody() throws Exception {
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra").setTimeout(1));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.POST, "/", req -> req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME,
      "4CYp9vQsr2VSQEsi/oVsMu35Ho9TlR0EovcYovlbiBw=.1437037602082.41jwU0FPl/n7ZNZAZEA07GyIUnpKSTKQ8Eju7Nicb34="), null, 403, "Forbidden", "Forbidden");
  }

  @Test
  public void testGetCookieWithSession() throws Exception {

    router.route().handler(SessionHandler.create(SessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.get().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      storeCookies(resp);
      assertEquals(2, cookieJar.size());
    }, 200, "OK", null);

    testRequest(HttpMethod.GET, "/", req -> req.headers().set("cookie", encodeCookies()), resp -> {
      // session cookie is untouched, so not sent back
      assertTrue(resp.headers().getAll("set-cookie").isEmpty());
    }, 200, "OK", null);
  }

  @Test
  public void testGetCookieWithSessionReplay() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      storeCookies(resp);
      assertEquals(2, cookieJar.size());
    }, 200, "OK", null);

    // POST shall be OK as the token is on the session
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, res -> {
      List<String> cookies = res.headers().getAll("set-cookie");
      // as this request was fine, we must invalidate the old cookie
      assertEquals(1, cookies.size());
    }, 200, "OK", null);
    // The token shouldn't be reusable as it's been renewed
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, res -> {

    }, 403, "Forbidden", null);
  }

  @Test
  public void testGetCookieWithSessionMultipleGetSameToken() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      storeCookies(resp);
      assertEquals(2, cookieJar.size());
    }, 200, "OK", null);

    // GET shall not have any impact on the token as they are on the session, so we can reuse it further on...
    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("cookie", encodeCookies());
    }, res -> {
      List<String> cookies = res.headers().getAll("set-cookie");
      // as there is a session, the cookie jar should be untouched
      assertEquals(0, cookies.size());
    }, 200, "OK", null);
    // POST shall be OK as the token is on the session
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, null, 200, "OK", null);
  }

  @Test
  public void testPostWithHeaderAndOrigin() throws Exception {
    router.route().handler(StaticHandler.create());
    router.route("/xsrf").handler(CSRFHandler.create(vertx, "Abracadabra").setOrigin("http://myserver.com"));
    router.route("/xsrf").handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/xsrf", req -> {
      req.putHeader("Origin", "http://myserver.com");
    }, this::storeCookies, 200, "OK", null);

    testRequest(HttpMethod.POST, "/xsrf", req -> {
      req.putHeader("Origin", "http://myserver.com");
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
      req.putHeader("Cookie", encodeCookies());
    }, null, 200, "OK", null);
  }

  @Test
  public void testPostWithHeaderAndWrongOrigin() throws Exception {

    router.route().handler(StaticHandler.create());
    router.route("/xsrf").handler(CSRFHandler.create(vertx, "Abracadabra").setOrigin("http://myserver.com"));
    router.route("/xsrf").handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/xsrf", req -> req.putHeader("Origin", "https://myserver.com"), null, 403, "Forbidden", null);
    testRequest(HttpMethod.GET, "/xsrf", req -> req.putHeader("Origin", "http://myserver.com/"), null, 200, "OK", null);
    testRequest(HttpMethod.GET, "/xsrf", req -> req.putHeader("Origin", "http://myserver.com:80"), null, 200, "OK", null);
  }

  @Test
  public void testPostAfterPost() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      storeCookies(resp);
      assertEquals(2, cookieJar.size());
    }, 200, "OK", null);

    // POST shall be OK as the token is on the session
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, resp -> {
      Map<String, String> oldState = new HashMap<>(cookieJar);
      cookieJar.clear();
      storeCookies(resp);
      assertEquals(1, cookieJar.size());
      // CSRF cookies must be different now
      assertTrue(cookieJar.containsKey(CSRFHandler.DEFAULT_COOKIE_NAME));
      assertFalse(cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME).equals(oldState.get(CSRFHandler.DEFAULT_COOKIE_NAME)));
      // Now put back the session ID
      oldState.remove(CSRFHandler.DEFAULT_COOKIE_NAME);
      cookieJar.putAll(oldState);
    }, 200, "OK", null);
    // second POST should be fine
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, null, 200, "OK", null);
    // third POST should not be fine (as the token cannot be reused
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, null, 403, "Forbidden", null);
  }

  @Test
  public void testPostAfterPostNoState() throws Exception {
    router.route().handler(BodyHandler.create());
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, this::storeCookies, 200, "OK", null);

    // POST shall be OK as the token is on the session
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, resp -> {
      Map<String, String> oldState = new HashMap<>(cookieJar);
      cookieJar.clear();
      storeCookies(resp);
      assertEquals(1, cookieJar.size());
      // CSRF cookies must be different now
      assertTrue(cookieJar.containsKey(CSRFHandler.DEFAULT_COOKIE_NAME));
      assertFalse(cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME).equals(oldState.get(CSRFHandler.DEFAULT_COOKIE_NAME)));
    }, 200, "OK", null);
    // second POST should be fine
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, null, 200, "OK", null);
  }

  @Test
  public void testMultipleGetWithSessionSameToken() throws Exception {
    router.route().handler(BodyHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      storeCookies(resp);
      assertEquals(2, cookieJar.size());
    }, 200, "OK", null);

    for (int i = 0; i < 5; i++) {
      testRequest(
        HttpMethod.GET,
        "/",
        req -> {
          req.putHeader("cookie", encodeCookies());
        },
        resp -> {
          List<String> cookies = resp.headers().getAll("set-cookie");
          // within the same session tokens are preserved across requests if not used
          assertEquals(0, cookies.size());
        }, 200, "OK", null);
    }
  }

  @Test
  public void testPostWithNoResponse() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route("/working").handler(rc -> rc.response().end());
    router.route("/broken").handler(rc -> rc.request().connection().close());

    testRequest(HttpMethod.GET, "/working", null, this::storeCookies, 200, "OK", null);

    // POST shall be OK as the token is on the session
    testRequest(HttpMethod.POST, "/working", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, this::storeCookies, 200, "OK", null);

    CountDownLatch latch = new CountDownLatch(1);
    // this request will never return
    client.request(
      new RequestOptions().setMethod(HttpMethod.POST)
        .setHost("localhost").setPort(8080).setURI("/broken")
        .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
        .putHeader("Cookie", encodeCookies())
    ).onComplete(onSuccess(req -> {
      req.send().onComplete(onFailure(throwable -> {
        latch.countDown();
      }));
    }));

    awaitLatch(latch);

    testRequest(HttpMethod.GET, "/working", null, this::storeCookies, 200, "OK", null);

    // ensure valid token still works
    testRequest(HttpMethod.POST, "/working", req -> {
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
      req.putHeader("Cookie", encodeCookies());
    }, null, 200, "OK", null);
  }

  /**
   * When a POST happens during a GET, if the POST returned first (with a new CRSF token), when the GET completed it would
   * override the csrf token in the session. This meant a subsequent POST would fail as the CSRF token did not match the server value.
   */
  @Test
  public void simultaneousGetAndPostDoesNotOverrideTokenInSession() throws Exception {
    final SessionStore store = LocalSessionStore.create(vertx);
    final Promise<Void> delayedResponse = Promise.promise();

    router.route().handler(BodyHandler.create());
    router.route().handler(SessionHandler.create(store));
    router.route("/csrf/*").handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route("/csrf/basic").handler(rc -> rc.response().end());
    router.route("/csrf/first").handler(rc -> {
      delayedResponse.future().onComplete(v -> rc.response().end());
    });
    router.route("/csrf/second").handler(rc -> {
      rc.response().end();
      delayedResponse.complete();
    });

    // get a session
    testRequest(HttpMethod.GET, "/csrf/basic", null, this::storeCookies, 200, "OK", null);

    final CountDownLatch latch = new CountDownLatch(2);
    //Send the GET that will only resolve after a subsequent POST to /csrf/second
    client.request(
      new RequestOptions().setMethod(HttpMethod.GET)
        .putHeader("Cookie", encodeCookies())
        .setHost("localhost").setPort(8080).setURI("/csrf/first")
    ).compose(HttpClientRequest::send).onComplete(onSuccess(res -> {
      assertThat("Should not send set-cookie header", res.headers().get("set-cookie"), nullValue());
      latch.countDown();
    }));

    client.request(
      new RequestOptions().setMethod(HttpMethod.POST)
        .putHeader("Cookie", encodeCookies())
        .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
        .setHost("localhost").setPort(8080).setURI("/csrf/second")
    ).compose(HttpClientRequest::send).onComplete(onSuccess(res -> {
      Map<String, String> oldState = new HashMap<>(cookieJar);
      cookieJar.clear();

      storeCookies(res);
      assertEquals("Should only have one set-cookie", 1, cookieJar.size());
      assertTrue("Should be token cookie", cookieJar.containsKey(CSRFHandler.DEFAULT_COOKIE_NAME));

      // Get the session ID back in the cookie jar
      oldState.remove(CSRFHandler.DEFAULT_COOKIE_NAME);
      cookieJar.putAll(oldState);

      latch.countDown();
    }));

    awaitLatch(latch);

    // The above has confirmed that the GET did not send back a new cookie
    // Now to confirm the new token from the POST works
    testRequest(HttpMethod.POST, "/csrf/basic", req -> {
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
      req.putHeader("Cookie", encodeCookies());
    }, null, 200, "OK", null);
  }

  @Test
  public void testRerouteRequest() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route("/home").handler(rc -> rc.end("home"));
    router.route("/protected/*").handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route("/protected/initial").handler(rc -> rc.reroute("/protected/rerouted"));
    router.route("/protected/rerouted").handler(rc -> rc.end("done"));

    // get a session, if first request is rerouted we don't get a session because of seenHandler check in SessionHandlerImpl
    // and context cleaning in reroute function
    testRequest(HttpMethod.GET, "/home", null, this::storeCookies, 200, "OK", null);

    testRequest(HttpMethod.GET, "/protected/initial", req -> req.putHeader("cookie", encodeCookies()), resp -> {
      Map<String, String> oldState = new HashMap<>(cookieJar);
      cookieJar.clear();
      storeCookies(resp);
      assertEquals(1, cookieJar.size()); // reroute loses session cookie
      // Add session cookie again
      cookieJar.forEach(oldState::remove);
      cookieJar.putAll(oldState);
    }, 200, "OK", null);

    // POST shall be OK as the token and session align
    testRequest(HttpMethod.POST, "/protected/rerouted", req -> {
      req.putHeader("cookie", encodeCookies());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME));
    }, null, 200, "OK", null);
  }
}
