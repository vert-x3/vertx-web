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

package io.vertx.ext.apex.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.impl.BodyHandlerImpl;

/**
 * A handler which gathers the entire request body and sets it on the {@link io.vertx.ext.apex.RoutingContext}.
 * <p>
 * It also handles HTTP file uploads and can be used to limit body sizes.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface BodyHandler extends Handler<RoutingContext> {

  /**
   * Default max size for a request body. -1 means unlimited
   */
  static final long DEFAULT_BODY_LIMIT = -1;

  /**
   * Default uploads directory on server for file uploads
   */
  static final String DEFAULT_UPLOADS_DIRECTORY = "file-uploads";

  /**
   * Create a body handler with defaults
   *
   * @return the body handler
   */
  static BodyHandler create() {
    return new BodyHandlerImpl();
  }

  /**
   * Create a body handler specifying max body size
   *
   * @param bodyLimit - the max body size in bytes
   * @return the body handler
   */
  static BodyHandler create(long bodyLimit) {
    return new BodyHandlerImpl(bodyLimit);
  }

  /**
   * Create a body handler specifying uploads directory
   *
   * @param uploadsDirectory - the uploads directory
   * @return the body handler
   */
  static BodyHandler create(String uploadsDirectory) {
    return new BodyHandlerImpl(uploadsDirectory);
  }

  /**
   * Create a body handler specifying max body size and uploads directory
   *
   * @param bodyLimit - the max body size in bytes
   * @param uploadsDirectory - the uploads directory
   * @return the body handler
   */
  static BodyHandler create(long bodyLimit, String uploadsDirectory) {
    return new BodyHandlerImpl(bodyLimit, uploadsDirectory);
  }

  @Override
  void handle(RoutingContext context);

}
