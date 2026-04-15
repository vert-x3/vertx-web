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

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpResponseHead;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class CSRFHandlerTest extends WebTestBase {

  @AfterAll
  public static void oneTimeTearDown() throws IOException {
    cleanupFileUploadDir();
  }

  @Test
  public void testGetCookie() throws Exception {

    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.get().handler(rc -> rc.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    List<String> cookies = resp.headers().getAll("set-cookie");
    assertEquals(1, cookies.size());
    assertEquals(CSRFHandler.DEFAULT_COOKIE_NAME, cookies.get(0).substring(0, cookies.get(0).indexOf('=')));
  }

  @Test
  public void testPostWithoutHeader(VertxTestContext testContext) {
    router.route()
      .handler(BodyHandler.create())
      .handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());
    router.errorHandler(403, rc -> testContext.completeNow());

    testRequest(HttpMethod.POST, "/", 403, "Forbidden", null);
  }

  private final Map<String, String> cookieJar = Collections.synchronizedMap(new HashMap<>());

  private void storeCookies(HttpResponseHead resp) {
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

    HttpResponse<Buffer> resp = testRequest(webClient.get("/xsrf").send(), 200, "OK");
    storeCookies(resp);

    testRequest(webClient.post("/xsrf")
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .putHeader("Cookie", encodeCookies())
      .send(), 200, "OK");
  }

  @Test
  public void testPostWithExpiredCookie() throws Exception {
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra").setTimeout(1));
    router.route().handler(rc -> rc.response().end());

    testRequest(webClient.post("/")
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME,
        "4CYp9vQsr2VSQEsi/oVsMu35Ho9TlR0EovcYovlbiBw=.1437037602082.41jwU0FPl/n7ZNZAZEA07GyIUnpKSTKQ8Eju7Nicb34=")
      .send(), 403, "Forbidden");
  }

  @Test
  public void testPostWithFormAttribute() throws Exception {

    // since we are working with forms we need the body handler to be present
    router.route().handler(BodyHandler.create());
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    storeCookies(resp);

    // create an HTTP form
    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    Buffer buffer = Buffer.buffer();
    String str =
      "--" + boundary + "\r\n" +
      "Content-Disposition: form-data; name=\"" + CSRFHandler.DEFAULT_HEADER_NAME + "\"\r\n\r\n" + cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME) + "\r\n" +
        "--" + boundary + "--\r\n";
    buffer.appendString(str);
    testRequest(webClient.post("/")
      .putHeader("content-length", String.valueOf(buffer.length()))
      .putHeader("content-type", "multipart/form-data; boundary=" + boundary)
      .putHeader("Cookie", encodeCookies())
      .sendBuffer(buffer), 200, "OK");
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

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    cookieJar.put(CSRFHandler.DEFAULT_COOKIE_NAME, resp.bodyAsString());

    // There is always 1 cookie (csrf)
    assertEquals(1, resp.headers().getAll("set-cookie").size());

    // will fail as the cookie is always required to be validated!
    String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
    Buffer buffer = Buffer.buffer();
    String str =
      "--" + boundary + "\r\n" +
      "Content-Disposition: form-data; name=\"" + CSRFHandler.DEFAULT_HEADER_NAME + "\"\r\n\r\n" + cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME) + "\r\n" +
        "--" + boundary + "--\r\n";
    buffer.appendString(str);
    testRequest(webClient.post("/")
      .putHeader("content-length", String.valueOf(buffer.length()))
      .putHeader("content-type", "multipart/form-data; boundary=" + boundary)
      .sendBuffer(buffer), 403, "Forbidden");
  }

  @Test
  public void testPostWithCustomResponseBody() throws Exception {
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra").setTimeout(1));
    router.route().handler(rc -> rc.response().end());

    testRequest(webClient.post("/")
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME,
        "4CYp9vQsr2VSQEsi/oVsMu35Ho9TlR0EovcYovlbiBw=.1437037602082.41jwU0FPl/n7ZNZAZEA07GyIUnpKSTKQ8Eju7Nicb34=")
      .send(), 403, "Forbidden", "Forbidden");
  }

  @Test
  public void testGetCookieWithSession() throws Exception {

    router.route().handler(SessionHandler.create(SessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.get().handler(rc -> rc.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    storeCookies(resp);
    assertEquals(2, cookieJar.size());

    HttpResponse<Buffer> resp2 = testRequest(webClient.get("/")
      .putHeader("cookie", encodeCookies())
      .send(), 200, "OK");
    // session cookie is untouched, so not sent back
    assertTrue(resp2.headers().getAll("set-cookie").isEmpty());
  }

  @Test
  public void testGetCookieWithSessionReplay() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    storeCookies(resp);
    assertEquals(2, cookieJar.size());

    // POST shall be OK as the token is on the session
    HttpResponse<Buffer> resp2 = testRequest(webClient.post("/")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 200, "OK");
    List<String> cookies = resp2.headers().getAll("set-cookie");
    // as this request was fine, we must invalidate the old cookie
    assertEquals(1, cookies.size());

    // The token shouldn't be reusable as it's been renewed
    testRequest(webClient.post("/")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 403, "Forbidden");
  }

  @Test
  public void testGetCookieWithSessionMultipleGetSameToken() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    storeCookies(resp);
    assertEquals(2, cookieJar.size());

    // GET shall not have any impact on the token as they are on the session, so we can reuse it further on...
    HttpResponse<Buffer> resp2 = testRequest(webClient.get("/")
      .putHeader("cookie", encodeCookies())
      .send(), 200, "OK");
    List<String> cookies = resp2.headers().getAll("set-cookie");
    // as there is a session, the cookie jar should be untouched
    assertEquals(0, cookies.size());

    // POST shall be OK as the token is on the session
    testRequest(webClient.post("/")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 200, "OK");
  }

  @Test
  public void testPostWithHeaderAndOrigin() throws Exception {
    router.route().handler(StaticHandler.create());
    router.route("/xsrf").handler(CSRFHandler.create(vertx, "Abracadabra").setOrigin("http://myserver.com"));
    router.route("/xsrf").handler(rc -> rc.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/xsrf")
      .putHeader("Origin", "http://myserver.com")
      .send(), 200, "OK");
    storeCookies(resp);

    testRequest(webClient.post("/xsrf")
      .putHeader("Origin", "http://myserver.com")
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .putHeader("Cookie", encodeCookies())
      .send(), 200, "OK");
  }

  @Test
  public void testPostWithHeaderAndWrongOrigin() throws Exception {

    router.route().handler(StaticHandler.create());
    router.route("/xsrf").handler(CSRFHandler.create(vertx, "Abracadabra").setOrigin("http://myserver.com"));
    router.route("/xsrf").handler(rc -> rc.response().end());

    testRequest(webClient.get("/xsrf")
      .putHeader("Origin", "https://myserver.com")
      .send(), 403, "Forbidden");
    testRequest(webClient.get("/xsrf")
      .putHeader("Origin", "http://myserver.com/")
      .send(), 200, "OK");
    testRequest(webClient.get("/xsrf")
      .putHeader("Origin", "http://myserver.com:80")
      .send(), 200, "OK");
  }

  @Test
  public void testPostAfterPost() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    storeCookies(resp);
    assertEquals(2, cookieJar.size());

    // POST shall be OK as the token is on the session
    HttpResponse<Buffer> resp2 = testRequest(webClient.post("/")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 200, "OK");
    Map<String, String> oldState = new HashMap<>(cookieJar);
    cookieJar.clear();
    storeCookies(resp2);
    assertEquals(1, cookieJar.size());
    // CSRF cookies must be different now
    assertTrue(cookieJar.containsKey(CSRFHandler.DEFAULT_COOKIE_NAME));
    assertFalse(cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME).equals(oldState.get(CSRFHandler.DEFAULT_COOKIE_NAME)));
    // Now put back the session ID
    oldState.remove(CSRFHandler.DEFAULT_COOKIE_NAME);
    cookieJar.putAll(oldState);

    // second POST should be fine
    testRequest(webClient.post("/")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 200, "OK");
    // third POST should not be fine (as the token cannot be reused
    testRequest(webClient.post("/")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 403, "Forbidden");
  }

  @Test
  public void testPostAfterPostNoState() throws Exception {
    router.route().handler(BodyHandler.create());
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    storeCookies(resp);

    // POST shall be OK as the token is on the session
    HttpResponse<Buffer> resp2 = testRequest(webClient.post("/")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 200, "OK");
    Map<String, String> oldState = new HashMap<>(cookieJar);
    cookieJar.clear();
    storeCookies(resp2);
    assertEquals(1, cookieJar.size());
    // CSRF cookies must be different now
    assertTrue(cookieJar.containsKey(CSRFHandler.DEFAULT_COOKIE_NAME));
    assertFalse(cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME).equals(oldState.get(CSRFHandler.DEFAULT_COOKIE_NAME)));

    // second POST should be fine
    testRequest(webClient.post("/")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 200, "OK");
  }

  @Test
  public void testMultipleGetWithSessionSameToken() throws Exception {
    router.route().handler(BodyHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");
    storeCookies(resp);
    assertEquals(2, cookieJar.size());

    for (int i = 0; i < 5; i++) {
      HttpResponse<Buffer> loopResp = testRequest(webClient.get("/")
        .putHeader("cookie", encodeCookies())
        .send(), 200, "OK");
      List<String> cookies = loopResp.headers().getAll("set-cookie");
      // within the same session tokens are preserved across requests if not used
      assertEquals(0, cookies.size());
    }
  }

  @Test
  public void testPostWithNoResponse() throws Exception {
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route("/working").handler(rc -> rc.response().end());
    router.route("/broken").handler(rc -> rc.request().connection().close());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/working").send(), 200, "OK");
    storeCookies(resp);

    // POST shall be OK as the token is on the session
    HttpResponse<Buffer> resp2 = testRequest(webClient.post("/working")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 200, "OK");
    storeCookies(resp2);

    try {
      client.request(
        new RequestOptions().setMethod(HttpMethod.POST)
          .setHost("localhost").setPort(8080).setURI("/broken")
          .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
          .putHeader("Cookie", encodeCookies())
      ).await().send().await();
      fail("Should have thrown");
    } catch (Exception expected) {
      // connection closed by server
    }

    HttpResponse<Buffer> resp3 = testRequest(webClient.get("/working").send(), 200, "OK");
    storeCookies(resp3);

    // ensure valid token still works
    testRequest(webClient.post("/working")
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .putHeader("Cookie", encodeCookies())
      .send(), 200, "OK");
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
    HttpResponse<Buffer> resp = testRequest(webClient.get("/csrf/basic").send(), 200, "OK");
    storeCookies(resp);

    //Send the GET that will only resolve after a subsequent POST to /csrf/second
    Future<HttpResponse<Buffer>> getFuture = webClient.get("/csrf/first")
      .putHeader("Cookie", encodeCookies())
      .send();

    HttpResponse<Buffer> postResp = webClient.post("/csrf/second")
      .putHeader("Cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send()
      .await();

    Map<String, String> oldState = new HashMap<>(cookieJar);
    cookieJar.clear();
    storeCookies(postResp);
    assertEquals(1, cookieJar.size(), "Should only have one set-cookie");
    assertTrue(cookieJar.containsKey(CSRFHandler.DEFAULT_COOKIE_NAME), "Should be token cookie");

    // Get the session ID back in the cookie jar
    oldState.remove(CSRFHandler.DEFAULT_COOKIE_NAME);
    cookieJar.putAll(oldState);

    HttpResponse<Buffer> getResp = getFuture.await();
    Assertions.assertThat(getResp.headers().get("set-cookie")).isNull();

    // The above has confirmed that the GET did not send back a new cookie
    // Now to confirm the new token from the POST works
    testRequest(webClient.post("/csrf/basic")
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .putHeader("Cookie", encodeCookies())
      .send(), 200, "OK");
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
    HttpResponse<Buffer> resp = testRequest(webClient.get("/home").send(), 200, "OK");
    storeCookies(resp);

    HttpResponse<Buffer> resp2 = testRequest(webClient.get("/protected/initial")
      .putHeader("cookie", encodeCookies())
      .send(), 200, "OK");
    Map<String, String> oldState = new HashMap<>(cookieJar);
    cookieJar.clear();
    storeCookies(resp2);
    assertEquals(1, cookieJar.size()); // reroute loses session cookie
    // Add session cookie again
    cookieJar.forEach(oldState::remove);
    cookieJar.putAll(oldState);

    // POST shall be OK as the token and session align
    testRequest(webClient.post("/protected/rerouted")
      .putHeader("cookie", encodeCookies())
      .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, cookieJar.get(CSRFHandler.DEFAULT_COOKIE_NAME))
      .send(), 200, "OK");
  }
}
