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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.core.impl.Utils;
import io.vertx.ext.apex.addons.Favicon;
import io.vertx.ext.apex.test.ApexTestBase;
import org.junit.Test;

import java.security.MessageDigest;
import java.util.Base64;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class FaviconTest extends ApexTestBase {

  @Test
  public void testFaviconClasspath() throws Exception {
    testFaviconPath(Favicon.favicon(), Favicon.DEFAULT_MAX_AGE);
  }

  @Test
  public void testFaviconPath() throws Exception {
    String path = "src/test/resources/favicon.ico";
    testFaviconPath(Favicon.favicon(path), Favicon.DEFAULT_MAX_AGE);
  }

  @Test
  public void testFaviconPathMaxAge() throws Exception {
    String path = "src/test/resources/favicon.ico";
    long maxAge = Favicon.DEFAULT_MAX_AGE * 2;
    testFaviconPath(Favicon.favicon(path, maxAge), maxAge);
  }

  @Test
  public void testFaviconMaxAge() throws Exception {
    long maxAge = Favicon.DEFAULT_MAX_AGE * 2;
    testFaviconPath(Favicon.favicon(maxAge), maxAge);
  }

  private void testFaviconPath(Favicon favicon, long maxAge) throws Exception {
    MessageDigest md = MessageDigest.getInstance("MD5");
    router.route().handler(favicon);
    router.route().handler(rc -> rc.response().end());
    Buffer icon = Utils.readResourceToBuffer("favicon.ico");
    testRequestBuffer(HttpMethod.GET, "/favicon.ico", null, resp -> {
      assertEquals("image/x-icon", resp.headers().get("content-type"));
      assertEquals(icon.length(), Integer.valueOf(resp.headers().get("content-length")).intValue());
      assertEquals("\"" + Base64.getEncoder().encodeToString(md.digest(icon.getBytes())) + "\"", resp.headers().get("etag"));
      assertEquals("public, max-age=" + (maxAge / 1000), resp.headers().get("cache-control"));
    }, 200, "OK", icon);
  }



}
