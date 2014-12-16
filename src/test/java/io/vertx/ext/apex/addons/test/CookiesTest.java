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
import io.vertx.ext.apex.core.Cookie;
import io.vertx.ext.apex.core.CookieHandler;
import io.vertx.ext.apex.core.impl.LookupCookie;
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
    router.route().handler(CookieHandler.cookieHandler());
  }


  @Test
  public void testSimpleCookie() throws Exception {
    router.route().handler(rc -> {
      assertEquals(1, rc.cookieCount());
      Cookie cookie = rc.getCookie("foo");
      assertNotNull(cookie);
      assertEquals("bar", cookie.getValue());
      rc.response().end();
    });
    testRequestWithCookies(HttpMethod.GET, "/", "foo=bar", 200, "OK");
  }

  @Test
  public void testCookiesReturned() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, rc.cookieCount());
      assertEquals("bar", rc.getCookie("foo").getValue());
      assertEquals("blibble", rc.getCookie("wibble").getValue());
      assertEquals("flop", rc.getCookie("plop").getValue());
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
  public void testGetCookies() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, rc.cookieCount());
      Set<Cookie> cookies = rc.cookies();
      assertTrue(cookies.contains(new LookupCookie("foo")));
      assertTrue(cookies.contains(new LookupCookie("wibble")));
      assertTrue(cookies.contains(new LookupCookie("plop")));
      rc.removeCookie("foo");
      assertFalse(cookies.contains(new LookupCookie("foo")));
      assertTrue(cookies.contains(new LookupCookie("wibble")));
      assertTrue(cookies.contains(new LookupCookie("plop")));
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
      assertEquals(3, rc.cookieCount());
      assertEquals("bar", rc.getCookie("foo").getValue());
      assertEquals("blibble", rc.getCookie("wibble").getValue());
      assertEquals("flop", rc.getCookie("plop").getValue());
      rc.removeCookie("plop");
      assertEquals(2, rc.cookieCount());
      rc.next();
    });
    router.route().handler(rc -> {
      assertEquals(2, rc.cookieCount());
      assertEquals("bar", rc.getCookie("foo").getValue());
      assertEquals("blibble", rc.getCookie("wibble").getValue());
      assertNull(rc.getCookie("plop"));
      rc.addCookie(Cookie.cookie("fleeb", "floob"));
      assertEquals(3, rc.cookieCount());
      assertNull(rc.removeCookie("blarb"));
      assertEquals(3, rc.cookieCount());
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
