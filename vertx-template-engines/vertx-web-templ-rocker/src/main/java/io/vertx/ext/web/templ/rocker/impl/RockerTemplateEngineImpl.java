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

package io.vertx.ext.web.templ.rocker.impl;

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.Rocker;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.templ.rocker.RockerTemplateEngine;

import java.util.Map;

/**
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
public class RockerTemplateEngineImpl extends CachingTemplateEngine<Void> implements RockerTemplateEngine {

  public RockerTemplateEngineImpl() {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
  }

  @Override
  public RockerTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public RockerTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      String templatePath = adjustLocation(templateFile);

      BindableRockerModel model = Rocker.template(templatePath);
      // we need to exclude some keys as rocker requires explicit knowledge on the binded keys
      context.forEach((k, v) -> {
        // all keys starting with __ are considered private and will not be exposed
        if (!k.startsWith("__")) {
          model.bind(k, v);
        }
      });

      VertxBufferOutput output = model.render(VertxBufferOutput.FACTORY);

      handler.handle(Future.succeededFuture(output.getBuffer()));
    } catch (final Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

}
