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

package io.vertx.ext.apex.addons.impl;

import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.addons.Cookie;
import io.vertx.ext.apex.addons.Cookies;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookiesHelper {

  public static Cookie getCookie(String name) {
    Map<String, Cookie> cookies = cookies();
    if (cookies != null) {
      return cookies.get(name);
    } else {
      return null;
    }
  }

  public static void addCookie(Cookie cookie) {
    Map<String, Cookie> cookies = cookies();
    if (cookies == null) {
      cookies = new HashMap<>();
      RoutingContext.getContext().put(Cookies.COOKIES_ENTRY_NAME, cookies);
    }
    cookies.put(cookie.getName(), cookie);
  }

  public static Cookie removeCookie(String name) {
    Map<String, Cookie> cookies = cookies();
    if (cookies != null) {
      return cookies.remove(name);
    } else {
      return null;
    }
  }

  public static Set<String> cookiesNames() {
    Map<String, Cookie> cookies = cookies();
    if (cookies != null) {
      return cookies.keySet();
    } else {
      return Collections.emptySet();
    }
  }

  public static int cookieCount() {
    Map<String, Cookie> cookies = cookies();
    if (cookies != null) {
      return cookies.size();
    } else {
      return 0;
    }
  }

  private static Map<String, Cookie> cookies() {
    return RoutingContext.getContext().get(Cookies.COOKIES_ENTRY_NAME);
  }
}
