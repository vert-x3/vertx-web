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

package io.vertx.ext.web.tests.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.tests.WebTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookieHandlerTest extends WebTestBase {

  public CookieHandlerTest() {
    super(ReportMode.FORBIDDEN);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testSimpleCookie() throws Exception {
    router.route().handler(rc -> {
      Assert.assertEquals(1, rc.request().cookieCount());
      Cookie cookie = rc.request().getCookie("foo");
      Assert.assertNotNull(cookie);
      Assert.assertEquals("bar", cookie.getValue());
      rc.response().end();
    });
    testRequestWithCookies(HttpMethod.GET, "/", "foo=bar", 200, "OK");
  }

  @Test
  public void testGetCookies() throws Exception {
    router.route().handler(rc -> {
      Assert.assertEquals(3, rc.request().cookieCount());
      Assert.assertNotNull(rc.request().getCookie("foo"));
      Assert.assertNotNull(rc.request().getCookie("wibble"));
      Assert.assertNotNull(rc.request().getCookie("plop"));
      Cookie removed = rc.response().removeCookie("foo");
      // removed cookies, need to be sent back with an expiration date
      Assert.assertNotNull(rc.request().getCookie("foo"));
      Assert.assertNotNull(rc.request().getCookie("wibble"));
      Assert.assertNotNull(rc.request().getCookie("plop"));
      rc.response().end();
    });
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("Cookie", "foo=bar; wibble=blibble; plop=flop").send(), 200, "OK");
    List<String> cookies = resp.headers().getAll("set-cookie");
    // the expired cookie must be sent back
    Assert.assertEquals(1, cookies.size());
    Assert.assertTrue(cookies.get(0).contains("Max-Age=0"));
    Assert.assertTrue(cookies.get(0).contains("Expires="));
  }

  @Test
  public void testCookiesChangedInHandler() throws Exception {
    router.route().handler(rc -> {
      Assert.assertEquals(3, rc.request().cookieCount());
      Assert.assertEquals("bar", rc.request().getCookie("foo").getValue());
      Assert.assertEquals("blibble", rc.request().getCookie("wibble").getValue());
      Assert.assertEquals("flop", rc.request().getCookie("plop").getValue());
      rc.response().removeCookie("plop");
      // the expected number of elements should remain the same as we're sending an invalidate cookie back
      Assert.assertEquals(3, rc.request().cookieCount());
      rc.next();
    });
    router.route().handler(rc -> {
      Assert.assertEquals("bar", rc.request().getCookie("foo").getValue());
      Assert.assertEquals("blibble", rc.request().getCookie("wibble").getValue());
      Assert.assertNotNull(rc.request().getCookie("plop"));
      rc.response().addCookie(Cookie.cookie("fleeb", "floob"));
      Assert.assertEquals(4, rc.request().cookieCount());
      Assert.assertNull(rc.response().removeCookie("blarb"));
      Assert.assertEquals(4, rc.request().cookieCount());
      Cookie foo = rc.request().getCookie("foo");
      foo.setValue("blah");
      rc.response().end();
    });
    HttpResponse<Buffer> resp = testRequest(webClient.get("/").putHeader("Cookie", "foo=bar; wibble=blibble; plop=flop").send(), 200, "OK");
    List<String> cookies = resp.headers().getAll("set-cookie");
    Assert.assertEquals(3, cookies.size());
    Assert.assertTrue(cookies.contains("foo=blah"));
    Assert.assertTrue(cookies.contains("fleeb=floob"));
    boolean found = false;
    for (String s : cookies) {
      if (s.startsWith("plop")) {
        found = true;
        Assert.assertTrue(s.contains("Max-Age=0"));
        Assert.assertTrue(s.contains("Expires="));
        break;
      }
    }
    Assert.assertTrue(found);
  }

  @Test
  public void testCookieFields() throws Exception {
    Cookie cookie = Cookie.cookie("foo", "bar");
    Assert.assertEquals("foo", cookie.getName());
    Assert.assertEquals("bar", cookie.getValue());
    Assert.assertEquals("foo=bar", cookie.encode());
    Assert.assertNull(cookie.getPath());
    cookie.setPath("/somepath");
    Assert.assertEquals("/somepath", cookie.getPath());
    Assert.assertEquals("foo=bar; Path=/somepath", cookie.encode());
    Assert.assertNull(cookie.getDomain());
    cookie.setDomain("foo.com");
    Assert.assertEquals("foo.com", cookie.getDomain());
    Assert.assertEquals("foo=bar; Path=/somepath; Domain=foo.com", cookie.encode());
    long maxAge = 30 * 60;
    cookie.setMaxAge(maxAge);


    long now = System.currentTimeMillis();
    String encoded = cookie.encode();
    int startPos = encoded.indexOf("Expires=");
    int endPos = encoded.indexOf(';', startPos);
    String expiresDate = encoded.substring(startPos + 8, endPos);
    Date d = new Date(Utils.parseRFC1123DateTime(expiresDate));
    Assert.assertTrue(d.getTime() - now >= maxAge);

    cookie.setMaxAge(Long.MIN_VALUE);
    cookie.setSecure(true);
    Assert.assertEquals("foo=bar; Path=/somepath; Domain=foo.com; Secure", cookie.encode());
    cookie.setHttpOnly(true);
    Assert.assertEquals("foo=bar; Path=/somepath; Domain=foo.com; Secure; HTTPOnly", cookie.encode());
  }
}
