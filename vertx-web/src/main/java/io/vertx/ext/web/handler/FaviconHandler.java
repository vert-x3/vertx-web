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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.FaviconHandlerImpl;

/**
 * A handler that serves favicons.
 * <p>
 * If no file system path is specified it will attempt to serve a resource called `favicon.ico` from the classpath.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface FaviconHandler extends Handler<RoutingContext> {

  /**
   * The default max age in seconds as set in the cache-control header
   */
  long DEFAULT_MAX_AGE_SECONDS = 86400;

  /**
   * Create a handler with defaults
   *
   * @return the handler
   */
  static FaviconHandler create() {
    return new FaviconHandlerImpl();
  }

  /**
   * Create a handler attempting to load favicon file from the specified path
   *
   * @param path  the path
   * @return the handler
   */
  static FaviconHandler create(String path) {
    return new FaviconHandlerImpl(path);
  }

  /**
   * Create a handler attempting to load favicon file from the specified path, and with the specified max cache time
   *
   * @param path  the path
   * @param maxAgeSeconds max how long the file will be cached by browser, in seconds
   * @return the handler
   */
  static FaviconHandler create(String path, long maxAgeSeconds) {
    return new FaviconHandlerImpl(path, maxAgeSeconds);
  }

  /**
   * Create a handler with the specified max cache time
   *
   * @param maxAgeSeconds max how long the file will be cached by browser, in seconds
   * @return the handler
   */
  static FaviconHandler create(long maxAgeSeconds) {
    return new FaviconHandlerImpl(maxAgeSeconds);
  }

}
