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

package io.vertx.ext.web.templ.freemarker.impl;

import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.impl.TemplateHolder;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class FreeMarkerTemplateEngineImpl extends CachingTemplateEngine<Template> implements FreeMarkerTemplateEngine {

  private final Configuration config;

  public FreeMarkerTemplateEngineImpl(Vertx vertx, String extension) {
    super(vertx, extension);

    config = new Configuration(Configuration.VERSION_2_3_29);
    config.setObjectWrapper(new VertxWebObjectWrapper(config.getIncompatibleImprovements()));
    config.setTemplateLoader(new FreeMarkerTemplateLoader(vertx));
    config.setCacheStorage(new NullCacheStorage());
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
          template = new TemplateHolder<>(config.getTemplate(src));
        }
        putTemplate(src, template);
      }

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        template.template().process(context, new OutputStreamWriter(baos));
        handler.handle(Future.succeededFuture(Buffer.buffer(baos.toByteArray())));
      }

    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }
}
