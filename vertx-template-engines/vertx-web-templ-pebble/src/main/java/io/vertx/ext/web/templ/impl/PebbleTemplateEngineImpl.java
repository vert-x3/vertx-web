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

package io.vertx.ext.web.templ.impl;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.PebbleTemplateEngine;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Dan Kristensen
 * @author Jens Klingsporn
 */
public class PebbleTemplateEngineImpl extends CachingTemplateEngine<PebbleTemplate> implements PebbleTemplateEngine {

  private final PebbleEngine pebbleEngine;

  public PebbleTemplateEngineImpl(Vertx vertx) {
    this(new PebbleEngine.Builder().loader(new PebbleVertxLoader(vertx)).cacheActive(false).build());
  }

  public PebbleTemplateEngineImpl(PebbleEngine engine) {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
    this.pebbleEngine = engine;
  }

  @Override
  public PebbleTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public PebbleTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void render(RoutingContext context, String templateDirectory, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    try {
      templateFileName = templateDirectory + templateFileName;
      PebbleTemplate template = isCachingEnabled() ? cache.get(templateFileName) : null;
      if (template == null) {
        // real compile
        final String loc = adjustLocation(templateFileName);
        template = pebbleEngine.getTemplate(loc);
        if (isCachingEnabled()) {
          cache.put(templateFileName, template);
        }
      }
      final Locale locale = context.acceptableLanguages()
              .stream()
              .findFirst()
                .map(LanguageHeader::value)
                .map(Locale::forLanguageTag)
              .orElseGet(Locale::getDefault);
      final Map<String, Object> variables = new HashMap<>(1);
      variables.put("context", context);
      final StringWriter stringWriter = new StringWriter();
      template.evaluate(stringWriter, variables, locale);
      handler.handle(Future.succeededFuture(Buffer.buffer(stringWriter.toString())));
    } catch (final Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

}
