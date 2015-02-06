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
import io.vertx.ext.apex.handler.impl.TemplateHandlerImpl;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.templ.TemplateEngine;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface TemplateHandler extends Handler<RoutingContext> {

  static final String DEFAULT_TEMPLATE_DIRECTORY = "templates";
  static final String DEFAULT_CONTENT_TYPE = "text/html";

  static TemplateHandler create(TemplateEngine engine) {
    return new TemplateHandlerImpl(engine, DEFAULT_TEMPLATE_DIRECTORY, DEFAULT_CONTENT_TYPE);
  }

  static TemplateHandler create(TemplateEngine engine, String templateDirectory, String contentType) {
    return new TemplateHandlerImpl(engine, templateDirectory, contentType);
  }

  @Override
  void handle(RoutingContext context);

}
