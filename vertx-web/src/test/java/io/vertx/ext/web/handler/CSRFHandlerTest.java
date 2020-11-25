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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

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

    // we need to wait getting failure Throwable
    CountDownLatch latch = new CountDownLatch(1);

    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());
    router.errorHandler(403, rc -> latch.countDown());

    testRequest(HttpMethod.POST, "/", null, null, 403, "Forbidden", null);

    latch.await();
  }

  String rawCookie;
  String tmpCookie;

  @Test
  public void testPostWithHeader() throws Exception {

    router.route().handler(StaticHandler.create());
    router.route("/xsrf").handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route("/xsrf").handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/xsrf", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      String cookie = cookies.get(0);
      rawCookie = cookie;
      tmpCookie = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
    }, 200, "OK", null);

    testRequest(HttpMethod.POST, "/xsrf", req -> {
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
      req.putHeader("Cookie", rawCookie);
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

    testRequest(HttpMethod.GET, "/", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      String cookie = cookies.get(0);
      rawCookie = cookie;
      tmpCookie = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
    }, 200, "OK", null);

    testRequest(HttpMethod.POST, "/", req -> {
      // create a HTTP form
      String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
      Buffer buffer = Buffer.buffer();
      String str =
          "--" + boundary + "\r\n" +
          "Content-Disposition: form-data; name=\"" + CSRFHandler.DEFAULT_HEADER_NAME + "\"\r\n\r\n" + tmpCookie + "\r\n" +
          "--" + boundary + "--\r\n";
      buffer.appendString(str);
      req.headers().set("content-length", String.valueOf(buffer.length()));
      req.headers().set("content-type", "multipart/form-data; boundary=" + boundary);
      req.putHeader("Cookie", rawCookie);
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
        tmpCookie = buffer.toString();
        latch.countDown();
      });

      List<String> cookies = resp.headers().getAll("set-cookie");
      // there is always at least 1 cookie (csrf)
      assertTrue(cookies.size() > 0);
    }, 200, "OK", null);

    // response body is known
    latch.await();

    // will fail as the cookie is always required to be validated!
    testRequest(HttpMethod.POST, "/", req -> {
      // create a HTTP form
      String boundary = "dLV9Wyq26L_-JQxk6ferf-RT153LhOO";
      Buffer buffer = Buffer.buffer();
      String str =
          "--" + boundary + "\r\n" +
              "Content-Disposition: form-data; name=\"" + CSRFHandler.DEFAULT_HEADER_NAME + "\"\r\n\r\n" + tmpCookie + "\r\n" +
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

    AtomicReference<String> sessionID = new AtomicReference<>();

    testRequest(HttpMethod.GET, "/", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(2, cookies.size());
      // save the cookie
      for (String cookie : cookies) {
        if (cookie.startsWith("vertx-web.session=")) {
          sessionID.set(cookie);
        }
      }
    }, 200, "OK", null);

    testRequest(HttpMethod.GET, "/", req -> req.putHeader("cookie", sessionID.get()), resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      // session cookie is untouched, so not sent back
      assertEquals(0, cookies.size());
    }, 200, "OK", null);
  }

  @Test
  public void testGetCookieWithSessionReplay() throws Exception {

    final AtomicReference<String> cookieJar = new AtomicReference<>();

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(2, cookies.size());
      String encodedCookie = "";
      // save the cookies
      for (String cookie : cookies) {
        encodedCookie += cookie.substring(0, cookie.indexOf(';'));
        encodedCookie += "; ";
        if (cookie.startsWith(CSRFHandler.DEFAULT_COOKIE_NAME)) {
          tmpCookie = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
        }
      }
      cookieJar.set(encodedCookie);
    }, 200, "OK", null);

    // POST shall be OK as the token is on the session
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", cookieJar.get());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
    }, res -> {
      List<String> cookies = res.headers().getAll("set-cookie");
      // as this request was fine, we must invalidate the old cookie
      assertEquals(1, cookies.size());
    }, 200, "OK", null);
    // POST shall be Forbidded as the token is now removed from the session (can only be used once)
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", cookieJar.get());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
    }, res -> {

    }, 403, "Forbidden", null);
  }

  @Test
  public void testGetCookieWithSessionMultipleGetSameToken() throws Exception {

    final AtomicReference<String> cookieJar = new AtomicReference<>();

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(2, cookies.size());
      String encodedCookie = "";
      // save the cookies
      for (String cookie : cookies) {
        encodedCookie += cookie.substring(0, cookie.indexOf(';'));
        encodedCookie += "; ";
        if (cookie.startsWith(CSRFHandler.DEFAULT_COOKIE_NAME)) {
          tmpCookie = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
        }
      }
      cookieJar.set(encodedCookie);
    }, 200, "OK", null);

    // GET shall not have any impact on the token as they are on the session, so we can reuse it further on...
    testRequest(HttpMethod.GET, "/", req -> {
      req.putHeader("cookie", cookieJar.get());
    }, res -> {
      List<String> cookies = res.headers().getAll("set-cookie");
      // as there is a session, the cookie jar should be untouched
      assertEquals(0, cookies.size());
    }, 200, "OK", null);
    // POST shall be OK as the token is on the session
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", cookieJar.get());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
    }, null, 200, "OK", null);
  }

  @Test
  public void testPostWithHeaderAndOrigin() throws Exception {

    router.route().handler(StaticHandler.create());
    router.route("/xsrf").handler(CSRFHandler.create(vertx, "Abracadabra").setOrigin("http://myserver.com"));
    router.route("/xsrf").handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/xsrf", req -> {
      req.putHeader("Origin", "http://myserver.com");
    }, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      String cookie = cookies.get(0);
      rawCookie = cookie;
      tmpCookie = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
    }, 200, "OK", null);

    testRequest(HttpMethod.POST, "/xsrf", req -> {
      req.putHeader("Origin", "http://myserver.com");
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
      req.putHeader("Cookie", rawCookie);
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
}
