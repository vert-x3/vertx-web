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
import io.vertx.ext.web.handler.impl.BodyHandlerImpl;

/**
 * A handler which gathers the entire request body and sets it on the {@link RoutingContext}.
 * <p>
 * It also handles HTTP file uploads and can be used to limit body sizes.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface BodyHandler extends Handler<RoutingContext> {

  /**
   * Create a body handler with default {@link BodyHandlerOptions}.
   *
   * @return the body handler
   */
  static BodyHandler create() {
    return new BodyHandlerImpl(new BodyHandlerOptions());
  }

  /**
   * Like {@link #create()}, with the give {@link BodyHandlerOptions}.
   */
  static BodyHandler create(BodyHandlerOptions options) {
    return new BodyHandlerImpl(options);
  }
}
