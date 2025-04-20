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
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.CachedTemplate;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;
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
  public Configuration unwrap() {
    return config;
  }

  @Override
  public Future<Buffer> render(Map<String, Object> context, String templateFile) {
    try {
      // respect the locale if present
      Locale locale = context.containsKey("lang") ?
        Locale.forLanguageTag((String) context.get("lang")) :
        Locale.getDefault();
      String src = adjustLocation(templateFile);
      String key = src + "_" + locale.toLanguageTag();
      CachedTemplate<Template> template = getTemplate(key);
      if (template == null) {
        // real compile
        synchronized (this) {
          // Compile
          template = new CachedTemplate<>(config.getTemplate(src, locale));
        }
        putTemplate(key, template);
      }

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        template.template().process(context, new OutputStreamWriter(baos));
        return Future.succeededFuture(Buffer.buffer(baos.toByteArray()));
      }

    } catch (Exception ex) {
      return Future.failedFuture(ex);
    }
  }
}
