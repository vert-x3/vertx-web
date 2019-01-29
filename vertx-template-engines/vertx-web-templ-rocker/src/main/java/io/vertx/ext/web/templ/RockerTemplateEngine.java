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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

/**
 * A template engine that uses the Rocker library.
 *
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 * @deprecated please use {@link io.vertx.ext.web.templ.rocker.RockerTemplateEngine} instead.
 */
@Deprecated
public interface RockerTemplateEngine extends io.vertx.ext.web.templ.rocker.RockerTemplateEngine {

  /**
   * Create a template engine using defaults
   *
   * @return the engine
   */
  static io.vertx.ext.web.templ.rocker.RockerTemplateEngine create() {
    return io.vertx.ext.web.templ.rocker.RockerTemplateEngine.create();
  }

  /**
   * Render the template. Template engines that support partials/fragments should extract the template base path from
   * the template filename up to the last file separator.
   *
   * Some engines support localization, for these engines, there is a predefined key "lang" to specify the language to
   * be used in the localization, the format should follow the standard locale formats e.g.: "en-gb", "pt-br", "en".
   *
   * @param context  the routing context
   * @param templateFileName  the template file name to use
   * @param handler  the handler that will be called with a result containing the buffer or a failure.
   */
  @Deprecated
  default void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    // restore the deprecated "context" top level key
    context.put("context", context.data());
    render(context.data(), templateFileName, handler);
  }
}
