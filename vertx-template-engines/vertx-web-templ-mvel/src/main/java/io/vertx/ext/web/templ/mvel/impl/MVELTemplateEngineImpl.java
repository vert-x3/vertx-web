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

package io.vertx.ext.web.templ.mvel.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import org.mvel2.integration.impl.ImmutableDefaultFactory;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.mvel2.util.StringAppender;

import io.vertx.ext.web.templ.mvel.MVELTemplateEngine;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class MVELTemplateEngineImpl extends CachingTemplateEngine<CompiledTemplate> implements MVELTemplateEngine {

  private final Vertx vertx;

  public MVELTemplateEngineImpl(Vertx vertx) {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
    this.vertx = vertx;
  }

  @Override
  public MVELTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public MVELTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      int idx = templateFile.lastIndexOf('/');
      String prefix = "";
      if (idx != -1) {
        prefix = templateFile.substring(0, idx);
      }

      CompiledTemplate template = isCachingEnabled() ? cache.get(templateFile) : null;
      if (template == null) {
        // real compile
        String loc = adjustLocation(templateFile);

        String templ = null;

        if (vertx.fileSystem().existsBlocking(loc)) {
          templ = vertx.fileSystem()
            .readFileBlocking(loc)
            .toString(Charset.defaultCharset());
        }

        if (templ == null) {
          handler.handle(Future.failedFuture("Cannot find template " + loc));
          return;
        }

        template = TemplateCompiler.compileTemplate(templ);
        if (isCachingEnabled()) {
          cache.put(templateFile, template);
        }
      }

      handler.handle(Future.succeededFuture(
        Buffer.buffer(
          (String) new TemplateRuntime(template.getTemplate(), null, template.getRoot(), prefix)
            .execute(new StringAppender(), context, new ImmutableDefaultFactory())
        )
      ));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

}
