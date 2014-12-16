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

package io.vertx.ext.apex.core;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.core.impl.BodyHandlerImpl;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface BodyHandler extends Handler<RoutingContext> {

  static final long DEFAULT_BODY_LIMIT = -1;
  static final String DEFAULT_UPLOADS_DIRECTORY = "file-uploads";

  static BodyHandler bodyHandler() {
    return new BodyHandlerImpl();
  }

  static BodyHandler bodyHandler(long bodyLimit) {
    return new BodyHandlerImpl(bodyLimit);
  }

  static BodyHandler bodyHandler(String uploadsDirectory) {
    return new BodyHandlerImpl(uploadsDirectory);
  }

  static BodyHandler bodyHandler(long bodyLimit, String uploadsDirectory) {
    return new BodyHandlerImpl(bodyLimit, uploadsDirectory);
  }

  @Override
  void handle(RoutingContext context);

}
