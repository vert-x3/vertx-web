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

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.MustacheTemplateEngine;

/**
 * @author <a href="mailto:j.milagroso@gmail.com">Jay Milagroso</a>
 */
public class MustacheTemplateEngineImpl extends CachingTemplateEngine<Template> implements MustacheTemplateEngine {

  public MustacheTemplateEngineImpl() {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
  }

  @Override
  public MustacheTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public MustacheTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void render(RoutingContext context, String templateDirectory, String templateFileName,
                     Handler<AsyncResult<Buffer>> handler) {
    try {
      String baseTemplateFileName = templateFileName;
      templateFileName = templateDirectory + templateFileName;
      Template template = isCachingEnabled() ? cache.get(templateFileName) : null;

      template = Mustache.compiler().escapeHTML(true).compile(context.data().toString());

      handler.handle(Future.succeededFuture(Buffer.buffer(template.execute(context))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }


}
