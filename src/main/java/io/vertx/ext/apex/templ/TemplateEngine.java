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

package io.vertx.ext.apex.templ;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.apex.RoutingContext;

/**
 * A template engine uses a specific template and the data in a routing context to render a resource into a buffer.
 * <p>
 * Concrete implementations exist for several well-known template engines.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface TemplateEngine {

  /**
   * Render
   * @param context  the routing context
   * @param templateFileName  the template file name to use
   * @param handler  the handler that will be called with a result containing the buffer or a failure.
   */
  void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler);
}
