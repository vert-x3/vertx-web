/*
 * Copyright 2016 Red Hat, Inc.
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

package io.vertx.ext.web.templ.httl.impl;

import httl.Engine;
import httl.Template;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.impl.TemplateHolder;
import io.vertx.ext.web.templ.httl.HTTLTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @author <a href="mailto:victorqrsilva@gmail.com">Victor Quezado</a>
 */
public class HTTLTemplateEngineImpl extends CachingTemplateEngine<Template> implements HTTLTemplateEngine {
  private final Engine engine;

  public HTTLTemplateEngineImpl(Vertx vertx, String extension) {
    super(vertx, extension);

    engine = Engine.getEngine();
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      String src = adjustLocation(templateFile);
      TemplateHolder<Template> template = getTemplate(src);
      if (template == null) {
        // real compile
        synchronized (this) {
          // Compile
          template = new TemplateHolder<>(engine.getTemplate(src));
        }
        putTemplate(src, template);
      }

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        template.template().render(context, baos);
        handler.handle(Future.succeededFuture(Buffer.buffer(baos.toByteArray())));
      }

    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }
}
