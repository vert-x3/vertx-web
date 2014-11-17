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

import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookierParserTest extends ApexTestBase {

  @Test
  public void testSimpleCookie() throws Exception {
    router.route().handler(CookieParser.cookierParser());
    router.route().handler(rc -> {
      Set<ApexCookie> cookies = rc.getCookies();
      for (ApexCookie cookie: cookies) {
        assertEquals("foo", cookie.getName());
        assertEquals("bar", cookie.getValue());
      }
      rc.response().end();
    });
    testRequestWithCookies(HttpMethod.GET, "/", "foo=bar", 200, "OK");
  }
}
