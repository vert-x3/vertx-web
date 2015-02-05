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

package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.addons.impl.PathTemplateHandlerImpl;
import io.vertx.ext.apex.core.RoutingContext;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface PathTemplateHandler extends Handler<RoutingContext> {

  static final String DEFAULT_TEMPLATE_DIRECTORY = "templates";
  static final String DEFAULT_CONTENT_TYPE = "text/html";

  static PathTemplateHandler templateHandler(TemplateEngine engine) {
    return new PathTemplateHandlerImpl(engine, DEFAULT_TEMPLATE_DIRECTORY, DEFAULT_CONTENT_TYPE);
  }

  static PathTemplateHandler templateHandler(TemplateEngine engine, String templateDirectory, String contentType) {
    return new PathTemplateHandlerImpl(engine, templateDirectory, contentType);
  }

  @Override
  void handle(RoutingContext context);

}
