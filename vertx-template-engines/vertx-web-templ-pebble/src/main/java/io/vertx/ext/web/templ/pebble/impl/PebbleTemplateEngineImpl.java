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

package io.vertx.ext.web.templ.pebble.impl;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.impl.TemplateHolder;
import io.vertx.ext.web.templ.pebble.PebbleTemplateEngine;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * @author Dan Kristensen
 * @author Jens Klingsporn
 */
public class PebbleTemplateEngineImpl extends CachingTemplateEngine<PebbleTemplate> implements PebbleTemplateEngine {

  private final PebbleEngine pebbleEngine;

  public PebbleTemplateEngineImpl(Vertx vertx, String extension) {
    this(vertx,
      extension,
      new PebbleEngine.Builder()
        .loader(new PebbleVertxLoader(vertx))
        .extension(new PebbleVertxExtension())
        .cacheActive(false).build());
  }

  public PebbleTemplateEngineImpl(Vertx vertx, String extension, PebbleEngine engine) {
    super(vertx, extension);
    this.pebbleEngine = engine;
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      String src = adjustLocation(templateFile);
      TemplateHolder<PebbleTemplate> template = getTemplate(src);
      if (template == null) {
        // real compile
        synchronized (this) {
          template = new TemplateHolder<>(pebbleEngine.getTemplate(adjustLocation(src)));
        }
        putTemplate(src, template);
      }

      // special key for lang selection
      final String lang = (String) context.get("lang");
      // rendering
      final StringWriter stringWriter = new StringWriter();
      template.template().evaluate(stringWriter, context, lang == null ? Locale.getDefault() : Locale.forLanguageTag(lang));
      handler.handle(Future.succeededFuture(Buffer.buffer(stringWriter.toString())));
    } catch (final Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

}
