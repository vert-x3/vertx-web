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

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class CSRFHandlerTest extends WebTestBase {

  @AfterClass
  public static void oneTimeTearDown() {
    Vertx vertx = Vertx.vertx();
    if (vertx.fileSystem().existsBlocking(BodyHandler.DEFAULT_UPLOADS_DIRECTORY)) {
      vertx.fileSystem().deleteRecursiveBlocking(BodyHandler.DEFAULT_UPLOADS_DIRECTORY, true);
    }
  }

  @Test
  public void testGetCookie() throws Exception {

    router.route().handler(CookieHandler.create());
    router.route().handler(CSRFHandler.create("Abracadabra"));
    router.get().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(1, cookies.size());
      assertEquals(CSRFHandler.DEFAULT_COOKIE_NAME, cookies.get(0).substring(0, cookies.get(0).indexOf('=')));
    }, 200, "OK", null);
  }

  @Test
  public void testPostWithoutHeader() throws Exception {

    router.route().handler(CookieHandler.create());
    router.route().handler(CSRFHandler.create("Abracadabra"));
    router.route().handler(rc -> rc.response().end());


    testRequest(HttpMethod.POST, "/", null, null, 403, "Forbidden", null);
  }

  String rawCookie;
  String tmpCookie;

  @Test
  public void testPostWithHeader() throws Exception {

    router.route().handler(StaticHandler.create());
    router.route("/xsrf").handler(CookieHandler.create());
    router.route("/xsrf").handler(CSRFHandler.create("Abracadabra"));
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
    router.route().handler(CookieHandler.create());
    router.route().handler(CSRFHandler.create("Abracadabra").setTimeout(1));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.POST, "/", req -> req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME,
        "4CYp9vQsr2VSQEsi/oVsMu35Ho9TlR0EovcYovlbiBw=.1437037602082.41jwU0FPl/n7ZNZAZEA07GyIUnpKSTKQ8Eju7Nicb34="), null, 403, "Forbidden", null);
  }

  @Test
  public void testPostWithFormAttribute() throws Exception {

    // since we are working with forms we need the body handler to be present
    router.route().handler(BodyHandler.create());
    router.route().handler(CookieHandler.create());
    router.route().handler(CSRFHandler.create("Abracadabra"));
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
    router.route().handler(CSRFHandler.create("Abracadabra"));
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

      assertEquals(0, cookies.size());
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
    final String expectedResponseBody = "Expected response body";

    router.route().handler(CookieHandler.create());
    router.route().handler(CSRFHandler.create("Abracadabra").setTimeout(1).setResponseBody(expectedResponseBody));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.POST, "/", req -> req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME,
      "4CYp9vQsr2VSQEsi/oVsMu35Ho9TlR0EovcYovlbiBw=.1437037602082.41jwU0FPl/n7ZNZAZEA07GyIUnpKSTKQ8Eju7Nicb34="), null, 403, "Forbidden", expectedResponseBody);
  }
}
