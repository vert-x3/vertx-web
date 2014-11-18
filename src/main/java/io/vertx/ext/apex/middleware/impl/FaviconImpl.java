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

package io.vertx.ext.apex.middleware.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.middleware.Favicon;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class FaviconImpl implements Favicon {

  /**
   * ## Icon
   *
   * Represents a favicon.ico file and related headers
   */
  private class Icon {
    /**
     * Headers for the icon resource
     */
    private final MultiMap headers;

    /**
     * Binary content of the icon file
     */
    private final Buffer body;

    /**
     * Instantiate a new Icon
     *
     * @param buffer buffer containing the image data for this icon.
     */
    private Icon(Buffer buffer) {
      headers = new CaseInsensitiveHeaders();
      body = buffer;

      headers.add("content-type", "image/x-icon");
      headers.add("content-length", Integer.toString(buffer.length()));

      try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        headers.add("etag", "\"" + Base64.getEncoder().encodeToString(md.digest(buffer.getBytes())) + "\"");
      } catch (NoSuchAlgorithmException e) {
        // ignore
      }
      headers.add("cache-control", "public, max-age=" + (maxAge / 1000));
    }
  }

  /**
   * favicon cache
   */
  private Icon icon;

  /**
   * Location of the icon in the file system
   */
  private final String path;

  /**
   * Cache control for the resource
   */
  private final long maxAge;

  /**
   * Create a new Favicon instance using a file in the file system and customizable cache period
   *
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new Favicon("/icons/icon.ico", 1000));
   * </pre>
   *
   * @param path
   * @param maxAge
   */
  public FaviconImpl(String path, long maxAge) {
    this.path = path;
    this.maxAge = maxAge;
    if (maxAge < 0) {
      throw new IllegalArgumentException("maxAge must be > 0");
    }
  }

  /**
   * Create a new Favicon instance from the classpath and customizable cache period
   *
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new Favicon("/icons/icon.ico", 1000));
   * </pre>
   *
   * @param maxAge
   */
  public FaviconImpl(long maxAge) {
    this(null, maxAge);
  }

  /**
   * Create a new Favicon instance using a file in the file system and cache for 1 day.
   *
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new Favicon("/icons/icon.ico"));
   * </pre>
   *
   * @param path
   */
  public FaviconImpl(String path) {
    this(path, DEFAULT_MAX_AGE);
  }

  /**
   * Create a new Favicon instance using a the default icon and cache for 1 day.
   *
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new Favicon());
   * </pre>
   */
  public FaviconImpl() {
    this(null);
  }

  private void init(Vertx vertx) {
    try {
      if (path == null) {
        icon = new Icon(Utils.readResourceToBuffer("favicon.ico"));
      } else {
        icon = new Icon(vertx.fileSystem().readFileSync(path));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void handle(RoutingContext ctx) {
    if (icon == null) {
      init(ctx.vertx());
    }
    if ("/favicon.ico".equals(ctx.request().path())) {
      ctx.response().headers().addAll(icon.headers);
      ctx.response().end(icon.body);
    } else {
      ctx.next();
    }
  }
}
