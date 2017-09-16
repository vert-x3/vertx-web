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
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.text.DateFormat;
import java.util.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookieHandlerTest extends WebTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    router.route().handler(CookieHandler.create());
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
  public void testGetCookies() throws Exception {
    router.route().handler(rc -> {
      assertEquals(3, rc.cookieCount());
      Set<Cookie> cookies = rc.cookies();
      assertTrue(contains(cookies, "foo"));
      assertTrue(contains(cookies, "wibble"));
      assertTrue(contains(cookies, "plop"));
      rc.removeCookie("foo");
      cookies = rc.cookies();
      assertFalse(contains(cookies, "foo"));
      assertTrue(contains(cookies, "wibble"));
      assertTrue(contains(cookies, "plop"));
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> req.headers().set("Cookie", "foo=bar; wibble=blibble; plop=flop"), resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(0, cookies.size());
    }, 200, "OK", null);
  }

  private boolean contains(Set<Cookie> cookies, String name) {
    for (Cookie cookie: cookies) {
      if (cookie.getName().equals(name)) {
        return true;
      }
    }
    return false;
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
      Cookie foo = rc.getCookie("foo");
      foo.setValue("blah");
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", req -> req.headers().set("Cookie", "foo=bar; wibble=blibble; plop=flop"), resp -> {
      List<String> cookies = resp.headers().getAll("set-cookie");
      assertEquals(2, cookies.size());
      assertTrue(cookies.contains("foo=blah"));
      assertTrue(cookies.contains("fleeb=floob"));
    }, 200, "OK", null);
  }

  @Test
  public void testCookieFields() throws Exception {
    Cookie cookie = Cookie.cookie("foo", "bar");
    assertEquals("foo", cookie.getName());
    assertEquals("bar", cookie.getValue());
    assertEquals("foo=bar", cookie.encode());
    assertNull(cookie.getPath());
    cookie.setPath("/somepath");
    assertEquals("/somepath", cookie.getPath());
    assertEquals("foo=bar; Path=/somepath", cookie.encode());
    assertNull(cookie.getDomain());
    cookie.setDomain("foo.com");
    assertEquals("foo.com", cookie.getDomain());
    assertEquals("foo=bar; Path=/somepath; Domain=foo.com", cookie.encode());
    long maxAge = 30 * 60;
    cookie.setMaxAge(maxAge);


    long now = System.currentTimeMillis();
    String encoded = cookie.encode();
    int startPos = encoded.indexOf("Expires=");
    int endPos = encoded.indexOf(';', startPos);
    String expiresDate = encoded.substring(startPos + 8, endPos);
    Date d = dateTimeFormat.parse(expiresDate);
    assertTrue(d.getTime() - now >= maxAge);

    cookie.setMaxAge(Long.MIN_VALUE);
    cookie.setSecure(true);
    assertEquals("foo=bar; Path=/somepath; Domain=foo.com; Secure", cookie.encode());
    cookie.setHttpOnly(true);
    assertEquals("foo=bar; Path=/somepath; Domain=foo.com; Secure; HTTPOnly", cookie.encode());
  }

  private final DateFormat dateTimeFormat = Utils.createRFC1123DateTimeFormatter();
}
