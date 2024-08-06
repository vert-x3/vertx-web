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
import io.vertx.ext.web.handler.impl.StaticHandlerImpl;

/**
 * A handler for serving static resources from the file system or classpath.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface StaticHandler extends Handler<RoutingContext> {

  /**
   * Default value of the web-root, where files are served from.
   */
  String DEFAULT_WEB_ROOT = "webroot";

  /**
   * Like {@link #create(String, StaticHandlerOptions)} but using the default web root path and default options.
   */
  static StaticHandler create() {
    return create(DEFAULT_WEB_ROOT);
  }

  /**
   * Like {@link #create()} but using the given web root path and default options.
   */
  static StaticHandler create(String webRoot) {
    return new StaticHandlerImpl(false, webRoot, new StaticHandlerOptions());
  }

  /**
   * Create a new static files handler.
   * <p>
   * Access to files is relative to the application's working directory, including the Java class path.
   *
   * @param webRoot the web root path
   * @param options the options
   * @return the static files handler.
   */
  static StaticHandler create(String webRoot, StaticHandlerOptions options) {
    return new StaticHandlerImpl(false, webRoot, options);
  }

  /**
   * Create a new static files handler.
   * <p>
   * Access to files is the full file system, starting at "/", limited by the operating system permissions for the user running the app.
   * <p>
   * <strong>Use with care</strong>.
   *
   * @param webRoot the web root path
   * @param options the handler options
   * @return the static files handler
   */
  static StaticHandler createWithRootFileSystemAccess(String webRoot, StaticHandlerOptions options) {
    return new StaticHandlerImpl(true, webRoot, options);
  }
}
