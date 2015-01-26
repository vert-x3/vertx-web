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

package io.vertx.ext.apex.addons.impl;

import io.vertx.ext.apex.addons.PathTemplateHandler;
import io.vertx.ext.apex.addons.TemplateEngine;
import io.vertx.ext.apex.core.RoutingContext;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class PathTemplateHandlerImpl implements PathTemplateHandler {

  private final TemplateEngine engine;
  private final String templateDirectory;
  private final String contentType;

  public PathTemplateHandlerImpl(TemplateEngine engine, String templateDirectory, String contentType) {
    this.engine = engine;
    this.templateDirectory = templateDirectory;
    this.contentType = contentType;
  }

  @Override
  public void handle(RoutingContext context) {
    String file = templateDirectory + context.pathFromMountPoint();
    engine.renderResponse(context, file, contentType);
  }
}
