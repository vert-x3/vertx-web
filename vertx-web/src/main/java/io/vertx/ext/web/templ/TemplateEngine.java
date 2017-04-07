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
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

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
   * The default content type header to be used in the response
   */
  String DEFAULT_CONTENT_TYPE = "text/html";

  /**
   * Render
   * @param context  the routing context
   * @param templateFileName  the template file name to use
   * @param handler  the handler that will be called with a result containing the buffer or a failure.
   */
  void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler);

  /**
   * Renders directly to the given context with the provided filename
   *
   * @param context  the routing context to render to
   * @param templateFileName  the template file to use
   */
  default void render(RoutingContext context, String templateFileName) {
    render(context, templateFileName, (res) -> {
      if (res.succeeded()) {
        context.response().putHeader(HttpHeaders.CONTENT_TYPE, getContentType()).end(res.result());
      } else {
        context.fail(res.cause());
      }
    });
  }

  /**
   * Gets the content type header to use when rendering, defaults to text/html
   *
   * @return the content type
   */
  String getContentType();

  /**
   * Sets the content type header to use when rendering
   * @param contentType the content type
   */
  void setContentType(String contentType);

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
