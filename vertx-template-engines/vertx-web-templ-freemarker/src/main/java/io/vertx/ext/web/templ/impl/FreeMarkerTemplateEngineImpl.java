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

package io.vertx.ext.web.templ.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class FreeMarkerTemplateEngineImpl extends CachingTemplateEngine<Template> implements FreeMarkerTemplateEngine {

  private final Configuration config;
  private final FreeMarkerTemplateLoader loader;

  public FreeMarkerTemplateEngineImpl() {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);

    loader = new FreeMarkerTemplateLoader();
    config = new Configuration(Configuration.VERSION_2_3_23);
    config.setObjectWrapper(new VertxWebObjectWrapper(config.getIncompatibleImprovements()));
    config.setTemplateLoader(loader);
  }

  @Override
  public FreeMarkerTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public FreeMarkerTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Deprecated
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    this.render(context.vertx(), context, templateFileName, handler);
  }

  @Override
  public <T> void render(Vertx vertx, T context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    try {
      Template template = cache.get(templateFileName);
      if (template == null) {
        // real compile
        synchronized (this) {
          loader.setVertx(vertx);
          // Compile
          template = config.getTemplate(adjustLocation(templateFileName));
        }
        cache.put(templateFileName, template);
      }

      Map<String, T> variables = new HashMap<>(1);
      variables.put("context", context);

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        template.process(variables, new OutputStreamWriter(baos));
        handler.handle(Future.succeededFuture(Buffer.buffer(baos.toByteArray())));
      }

    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }
}
