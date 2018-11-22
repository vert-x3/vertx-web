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

package io.vertx.ext.web.templ;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * A template engine uses a specific template and the data in a routing context to render a resource into a buffer.
 * <p>
 * Concrete implementations exist for several well-known template engines.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
@Deprecated
public interface TemplateEngine extends io.vertx.ext.web.common.template.TemplateEngine {

  /**
   * Render the template
   *
   * @param context  the routing context
   * @param templateFileName  the template file name to use
   * @param handler  the handler that will be called with a result containing the buffer or a failure.
   *
   * @deprecated  use {@link #render(RoutingContext, String, String, Handler)}
   */
  @Deprecated
  default void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    render(new JsonObject(context.data()), templateFileName, handler);
  }

  /**
   * Render the template
   * <p>
   * <b>NOTE</b> if you call method directly (i.e. not using {@link io.vertx.ext.web.handler.TemplateHandler}) make sure
   * that <i>templateFileName</i> is sanitized via {@link io.vertx.ext.web.impl.Utils#normalizePath(String)}
   *
   * @param context  the routing context
   * @param templateDirectory  the template directory to use
   * @param templateFileName  the relative template file name to use
   * @param handler  the handler that will be called with a result containing the buffer or a failure.
   */
  @Deprecated
  default void render(RoutingContext context, String templateDirectory, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    render(new JsonObject(context.data()), templateDirectory + templateFileName, handler);
  }

  /**
   * Returns true if the template engine caches template files. If false, then template files are freshly loaded each
   * time they are used.
   *
   * @return True if template files are cached; otherwise, false.
   */
  default boolean isCachingEnabled() {
    return false;
  }
}
