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
package io.vertx.ext.web.handler.impl;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.templ.TemplateEngine;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TemplateHandlerImpl implements TemplateHandler {

  private final TemplateEngine engine;
  private final String templateDirectory;
  private final String contentType;

  public TemplateHandlerImpl(TemplateEngine engine, String templateDirectory, String contentType) {
    this.engine = engine;
    this.templateDirectory = templateDirectory;
    this.contentType = contentType;
  }

  @Override
  public void handle(RoutingContext context) {
    String file = templateDirectory + Utils.pathOffset(context.normalisedPath(), context);
    engine.render(context, file, res -> {
      if (res.succeeded()) {
        context.response().putHeader(HttpHeaders.CONTENT_TYPE, contentType).end(res.result());
      } else {
        context.fail(res.cause());
      }
    });
  }
}
