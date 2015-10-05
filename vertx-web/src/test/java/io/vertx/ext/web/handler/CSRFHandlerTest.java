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

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.util.List;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class CSRFHandlerTest extends WebTestBase {

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

  String tmpCookie;

  @Test
  public void testPostWithHeader() throws Exception {

    router.route().handler(CookieHandler.create());
    router.route().handler(CSRFHandler.create("Abracadabra"));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.GET, "/", null, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      String cookie = cookies.get(0);
      tmpCookie = cookie.substring(cookie.indexOf('=') + 1);
      System.out.println(tmpCookie);
    }, 200, "OK", null);

    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME, tmpCookie);
    }, null, 200, "OK", null);
  }

  @Test
  public void testPostWithExpiredCookie() throws Exception {
    router.route().handler(CookieHandler.create());
    router.route().handler(CSRFHandler.create("Abracadabra").setTimeout(1));
    router.route().handler(rc -> rc.response().end());

    testRequest(HttpMethod.POST, "/", req -> {
      req.putHeader(CSRFHandler.DEFAULT_HEADER_NAME,
          "4CYp9vQsr2VSQEsi/oVsMu35Ho9TlR0EovcYovlbiBw=.1437037602082.41jwU0FPl/n7ZNZAZEA07GyIUnpKSTKQ8Eju7Nicb34=");
    }, null, 403, "Forbidden", null);
  }
}
