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

package io.vertx.ext.web.handler.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class FaviconHandlerImpl implements FaviconHandler {

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

      headers.add(CONTENT_TYPE, "image/x-icon");
      headers.add(CONTENT_LENGTH, Integer.toString(buffer.length()));
      headers.add(CACHE_CONTROL, "public, max-age=" + maxAgeSeconds);
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
  private final long maxAgeSeconds;

  /**
   * Create a new Favicon instance using a file in the file system and customizable cache period
   *
   * <pre>
   * Router router = Router.router(vertx);
   * router.route().handler(FaviconHandler.create("/icons/icon.ico", 1000));
   * </pre>
   *
   * @param path file path to icon
   * @param maxAgeSeconds max allowed time to be cached in seconds
   */
  public FaviconHandlerImpl(String path, long maxAgeSeconds) {
    this.path = path;
    this.maxAgeSeconds = maxAgeSeconds;
    if (maxAgeSeconds < 0) {
      throw new IllegalArgumentException("maxAgeSeconds must be > 0");
    }
  }

  /**
   * Create a new Favicon instance from the classpath and customizable cache period
   *
   * <pre>
   * Router router = Router.router(vertx);
   * router.route().handler(FaviconHandler.create(1000));
   * </pre>
   *
   * @param maxAgeSeconds max allowed time to be cached in seconds
   */
  public FaviconHandlerImpl(long maxAgeSeconds) {
    this(null, maxAgeSeconds);
  }

  /**
   * Create a new Favicon instance using a file in the file system and cache for 1 day.
   *
   * <pre>
   * Router router = Router.router(vertx);
   * router.route().handler(FaviconHandler.create("/icons/icon.ico"));
   * </pre>
   *
   * @param path file path to icon
   */
  public FaviconHandlerImpl(String path) {
    this(path, DEFAULT_MAX_AGE_SECONDS);
  }

  /**
   * Create a new Favicon instance using a the default icon and cache for 1 day.
   *
   * <pre>
   * Router router = Router.router(vertx);
   * router.route().handler(FaviconHandler.create());
   * </pre>
   */
  public FaviconHandlerImpl() {
    this(null);
  }

  private void init(Vertx vertx) {
    try {
      if (path == null) {
        icon = new Icon(Utils.readResourceToBuffer("favicon.ico"));
      } else {
        icon = new Icon(vertx.fileSystem().readFileBlocking(path));
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
