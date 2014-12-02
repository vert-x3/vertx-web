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

package io.vertx.ext.apex.addons.test;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.addons.Cookie;
import io.vertx.ext.apex.addons.Cookies;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookiesTest extends ApexTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    router.route().handler(Cookies.cookies());
  }


  @Test
  public void testSimpleCookie() throws Exception {
    router.route().handler(rc -> {
      assertEquals(1, Cookies.cookieCount());
      Cookie cookie = Cookies.getCookie("foo");
      assertNotNull(cookie);
      assertEquals("bar", cookie.getValue());
      rc.response().end();
    });
    testRequestWithCookies(HttpMethod.GET, "/", "foo=bar", 200, "OK");
  }

  @Test
  public void testCookiesReturned() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, Cookies.cookieCount());
      assertEquals("bar", Cookies.getCookie("foo").getValue());
      assertEquals("blibble", Cookies.getCookie("wibble").getValue());
      assertEquals("flop", Cookies.getCookie("plop").getValue());
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
  public void testCookiesNames() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, Cookies.cookieCount());
      Set<String> names = Cookies.cookiesNames();
      assertTrue(names.contains("foo"));
      assertTrue(names.contains("wibble"));
      assertTrue(names.contains("plop"));
      Cookies.removeCookie("foo");
      assertFalse(names.contains("foo"));
      assertTrue(names.contains("wibble"));
      assertTrue(names.contains("plop"));
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> {
      req.headers().set("Cookie", "foo=bar; wibble=blibble; plop=flop");
    }, resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(2, cookies.size());
      assertTrue(cookies.contains("wibble=blibble"));
      assertTrue(cookies.contains("plop=flop"));
    }, 200, "OK", null);
  }

  @Test
  public void testCookiesChangedInHandler() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, Cookies.cookieCount());
      assertEquals("bar", Cookies.getCookie("foo").getValue());
      assertEquals("blibble", Cookies.getCookie("wibble").getValue());
      assertEquals("flop", Cookies.getCookie("plop").getValue());
      Cookies.removeCookie("plop");
      assertEquals(2, Cookies.cookieCount());
      rc.next();
    });
    router.route().handler(rc -> {
      assertEquals(2, Cookies.cookieCount());
      assertEquals("bar", Cookies.getCookie("foo").getValue());
      assertEquals("blibble", Cookies.getCookie("wibble").getValue());
      assertNull(Cookies.getCookie("plop"));
      Cookies.addCookie(Cookie.cookie("fleeb", "floob"));
      assertEquals(3, Cookies.cookieCount());
      assertNull(Cookies.removeCookie("blarb"));
      assertEquals(3, Cookies.cookieCount());
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

  // TODO tests on all Cookie methods
}
