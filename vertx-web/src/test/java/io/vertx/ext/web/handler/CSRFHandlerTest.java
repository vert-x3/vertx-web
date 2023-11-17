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

import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;

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

    router.route()
      .handler(BodyHandler.create())
      .handler(CSRFHandler.create(vertx, "Abracadabra"));
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

  @Test
  public void testPostAfterPost() throws Exception {

    final AtomicReference<String> cookieJar = new AtomicReference<>();

    //router.route().handler(BodyHandler.create());
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
    }, resp -> {
      // re-extract the new cookie
      List<String> cookies = resp.headers().getAll("set-cookie");
      // only the XSRF cookie is updated
      assertEquals(1, cookies.size());
      String encodedCookie = "";
      // save the cookie
      for (String cookie : cookieJar.get().split(";")) {
        cookie = cookie.trim();
        if ("".equals(cookie)) {
          continue;
        }
        if (cookie.startsWith(CSRFHandler.DEFAULT_COOKIE_NAME)) {
          // replace with new one
          tmpCookie = cookies.get(0).substring(cookies.get(0).indexOf('=') + 1, cookies.get(0).indexOf(';'));
          encodedCookie += CSRFHandler.DEFAULT_COOKIE_NAME + "=" + tmpCookie;
        } else {
          encodedCookie += cookie;
        }
        encodedCookie += "; ";
      }
      // cookies must be different now
      assertFalse(cookieJar.get().equals(encodedCookie));
      cookieJar.set(encodedCookie);
    }, 200, "OK", null);
    // second POST should be fine
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", cookieJar.get());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
    }, null, 200, "OK", null);
    // third POST should not be fine (as the token cannot be reused
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", cookieJar.get());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
    }, null, 403, "Forbidden", null);
  }

  @Test
  public void testPostAfterPostNoState() throws Exception {

    final AtomicReference<String> cookieJar = new AtomicReference<>();

    router.route().handler(BodyHandler.create());
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(1, cookies.size());
      String encodedCookie;
      // save the cookie
      encodedCookie = cookies.get(0).substring(0, cookies.get(0).indexOf(';'));
      tmpCookie = cookies.get(0).substring(cookies.get(0).indexOf('=') + 1, cookies.get(0).indexOf(';'));
      cookieJar.set(encodedCookie);
    }, 200, "OK", null);

    // POST shall be OK as the token is on the session
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", cookieJar.get());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
    }, resp -> {
      // re-extract the new cookie
      List<String> cookies = resp.headers().getAll("set-cookie");
      // only the XSRF cookie is updated
      assertEquals(1, cookies.size());
      String encodedCookie = "";
      // save the cookie
      for (String cookie : cookieJar.get().split(";")) {
        cookie = cookie.trim();
        if ("".equals(cookie)) {
          continue;
        }
        if (cookie.startsWith(CSRFHandler.DEFAULT_COOKIE_NAME)) {
          // replace with new one
          tmpCookie = cookies.get(0).substring(cookies.get(0).indexOf('=') + 1, cookies.get(0).indexOf(';'));
          encodedCookie += CSRFHandler.DEFAULT_COOKIE_NAME + "=" + tmpCookie;
        } else {
          encodedCookie += cookie;
        }
        encodedCookie += "; ";
      }
      // cookies must be different now
      assertFalse(cookieJar.get().equals(encodedCookie));
      cookieJar.set(encodedCookie);
    }, 200, "OK", null);
    // second POST should be fine
    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader("cookie", cookieJar.get());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
    }, null, 200, "OK", null);
  }

  @Test
  public void testMultipleGetWithSessionSameToken() throws Exception {

    final AtomicReference<String> cookieJar = new AtomicReference<>();

    router.route().handler(BodyHandler.create());
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

    for (int i = 0; i < 5; i++) {
      testRequest(
        HttpMethod.GET,
        "/",
        req -> {
          req.putHeader("cookie", cookieJar.get());
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
    final AtomicReference<String> cookieJar = new AtomicReference<>();

    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(CSRFHandler.create(vertx, "Abracadabra"));
    router.route("/working").handler(rc -> rc.response().end());
    router.route("/broken").handler(rc -> {/*do nothing (no response)*/});

    testRequest(HttpMethod.GET, "/working", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(2, cookies.size());
      StringBuilder encodedCookie = new StringBuilder();
      // save the cookies
      for (String cookie : cookies) {
        encodedCookie.append(cookie, 0, cookie.indexOf(';'));
        encodedCookie.append("; ");
        if (cookie.startsWith(CSRFHandler.DEFAULT_COOKIE_NAME)) {
          tmpCookie = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
        }
      }
      cookieJar.set(encodedCookie.toString());
    }, 200, "OK", null);

    // POST shall be OK as the token is on the session
    testRequest(HttpMethod.POST, "/working", req -> {
      req.putHeader("cookie", cookieJar.get());
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
    }, resp -> {
      // re-extract the new cookie
      List<String> cookies = resp.headers().getAll("set-cookie");
      StringBuilder encodedCookie = new StringBuilder();
      // save the cookie
      for (String cookie : cookieJar.get().split(";")) {
        cookie = cookie.trim();
        if ("".equals(cookie)) {
          continue;
        }
        if (cookie.startsWith(CSRFHandler.DEFAULT_COOKIE_NAME)) {
          // replace with new one
          tmpCookie = cookies.get(0).substring(cookies.get(0).indexOf('=') + 1, cookies.get(0).indexOf(';'));
          encodedCookie.append(CSRFHandler.DEFAULT_COOKIE_NAME + "=").append(tmpCookie);
        } else {
          encodedCookie.append(cookie);
        }
        encodedCookie.append("; ");
      }
      cookieJar.set(encodedCookie.toString());
    }, 200, "OK", null);

    CountDownLatch latch = new CountDownLatch(1);
    // this request will never return but may incorrectly consume a valid token
    client.request(
      new RequestOptions().setMethod(HttpMethod.POST)
        .setHost("localhost").setPort(getServerPort()).setURI("/broken")
        .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie)
        .putHeader("Cookie", cookieJar.get())
    ).onComplete(onSuccess(req -> {
      req.end();
      // delay to ensure token is potentially consumed
      vertx.setTimer(500, v -> latch.countDown());
    }));

    awaitLatch(latch);

    // ensure valid token still works
    testRequest(HttpMethod.POST, "/working", req -> {
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
      req.putHeader("Cookie", cookieJar.get());
    }, null, 200, "OK", null);
  }

  /**
   * When a POST happens during a GET, if the POST returned first (with a new CRSF token), when the GET completed it would
   * override the csrf token in the session. This meant a subsequent POST would fail as the CSRF token did not match the server value.
   */
  @Test
  public void simultaneousGetAndPostDoesNotOverrideTokenInSession() throws Exception {

    final AtomicReference<String> cookieJar = new AtomicReference<>();
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
    testRequest(HttpMethod.GET, "/csrf/basic", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      //Session + CSRF
      assertEquals(2, cookies.size());
      StringBuilder encodedCookie = new StringBuilder();
      // save the cookies
      for (String cookie : cookies) {
        encodedCookie.append(cookie, 0, cookie.indexOf(';'));
        encodedCookie.append("; ");
        if (cookie.startsWith(CSRFHandler.DEFAULT_COOKIE_NAME)) {
          tmpCookie = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
        }
      }
      cookieJar.set(encodedCookie.toString());
    }, 200, "OK", null);


    final CountDownLatch latch = new CountDownLatch(2);
    //Send the GET that will only resolve after a subsequent POST to /csrf/second
    client.request(
      new RequestOptions().setMethod(HttpMethod.GET)
        .putHeader("Cookie", cookieJar.get())
        .setHost("localhost").setPort(getServerPort()).setURI("/csrf/first")
    ).compose(HttpClientRequest::send).onComplete(onSuccess(res -> {
      assertThat("Should not send set-cookie header", res.headers().get("set-cookie"), nullValue());
      latch.countDown();
    }));

    client.request(
      new RequestOptions().setMethod(HttpMethod.POST)
        .putHeader("Cookie", cookieJar.get())
        .putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie)
        .setHost("localhost").setPort(getServerPort()).setURI("/csrf/second")
    ).compose(HttpClientRequest::send).onComplete(onSuccess(res -> {
      assertEquals("Should only have one set-cookie", 1, res.headers().getAll("set-cookie").size());
      final String cookie = res.headers().get("set-cookie");
      assertThat("Should be token cookie", cookie, startsWith(CSRFHandler.DEFAULT_COOKIE_NAME));

      //Store the new token in the cookie jar + header value
      final String cookieValue = cookie.substring(0, cookie.indexOf(';'));
      tmpCookie = cookieValue.substring(cookie.indexOf('=') + 1);
      final String currentCookie = cookieJar.get();
      final int start = currentCookie.indexOf(CSRFHandler.DEFAULT_COOKIE_NAME);
      final int end = currentCookie.indexOf(";", start);
      final String newCookie = currentCookie.replace(currentCookie.substring(start, end + 1), "") + cookieValue + ";";
      cookieJar.set(newCookie);

      latch.countDown();
    }));

    awaitLatch(latch);

    // The above has confirmed that the GET did not send back a new cookie
    // Now to confirm the new token from the POST works
    testRequest(HttpMethod.POST, "/csrf/basic", req -> {
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
      req.putHeader("Cookie", cookieJar.get());
    }, null, 200, "OK", null);
  }
}
