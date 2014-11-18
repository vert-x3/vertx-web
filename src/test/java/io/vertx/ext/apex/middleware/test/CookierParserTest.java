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

package io.vertx.ext.apex.middleware.test;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.middleware.ApexCookie;
import io.vertx.ext.apex.middleware.CookieParser;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookierParserTest extends ApexTestBase {

  @Test
  public void testSimpleCookie() throws Exception {
    router.route().handler(CookieParser.cookierParser());
    router.route().handler(rc -> {
      Map<String, ApexCookie> cookies = CookieParser.cookies(rc);
      assertEquals(1, cookies.size());
      ApexCookie cookie = cookies.get("foo");
      assertNotNull(cookie);
      assertEquals("bar", cookie.getValue());
      rc.response().end();
    });
    testRequestWithCookies(HttpMethod.GET, "/", "foo=bar", 200, "OK");
  }

  @Test
  public void testCookiesReturned() throws Exception {
    router.route().handler(CookieParser.cookierParser());
    router.route().handler(rc -> {
      Map<String, ApexCookie> cookies = CookieParser.cookies(rc);
      assertEquals("bar", cookies.get("foo").getValue());
      assertEquals("blibble", cookies.get("wibble").getValue());
      assertEquals("flop", cookies.get("plop").getValue());
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().set("Cookie", "foo=bar; wibble=blibble; plop=flop");
    }, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(3, cookies.size());
      assertTrue(cookies.contains("foo=bar"));
      assertTrue(cookies.contains("wibble=blibble"));
      assertTrue(cookies.contains("plop=flop"));
    }, 200, "OK", null);
  }

  @Test
  public void testCookiesChangedInHandler() throws Exception {
    router.route().handler(CookieParser.cookierParser());
    router.route().handler(rc -> {
      Map<String, ApexCookie> cookies = CookieParser.cookies(rc);
      assertEquals("bar", cookies.get("foo").getValue());
      assertEquals("blibble", cookies.get("wibble").getValue());
      assertEquals("flop", cookies.get("plop").getValue());
      cookies.remove("plop");
      rc.next();
    });
    router.route().handler(rc -> {
      Map<String, ApexCookie> cookies = CookieParser.cookies(rc);
      assertEquals("bar", cookies.get("foo").getValue());
      assertEquals("blibble", cookies.get("wibble").getValue());
      assertFalse(cookies.containsKey("plop"));
      cookies.put("fleeb", ApexCookie.apexCookie("fleeb", "floob"));
      cookies.remove("flop");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().set("Cookie", "foo=bar; wibble=blibble; plop=flop");
    }, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(3, cookies.size());
      assertTrue(cookies.contains("foo=bar"));
      assertTrue(cookies.contains("wibble=blibble"));
      assertTrue(cookies.contains("fleeb=floob"));
    }, 200, "OK", null);
  }

  // TODO tests on all ApexCookie methods
}
