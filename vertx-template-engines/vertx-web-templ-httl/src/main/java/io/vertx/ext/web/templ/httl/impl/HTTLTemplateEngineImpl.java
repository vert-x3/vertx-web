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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.CachedTemplate;
import io.vertx.ext.web.templ.httl.HTTLTemplateEngine;

import java.io.Writer;
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
  public Engine unwrap() {
    return engine;
  }

  @Override
  public Future<Buffer> render(Map<String, Object> context, String templateFile) {
    try {
      String src = adjustLocation(templateFile);
      CachedTemplate<Template> template = getTemplate(src);
      if (template == null) {
        // real compile
        synchronized (this) {
          // Compile
          template = new CachedTemplate<>(engine.getTemplate(src));
        }
        putTemplate(src, template);
      }

      final Buffer buffer = Buffer.buffer();

      template.template().render(context, new Writer() {
        @Override
        public void write(char[] cbuf, int off, int len) {
          buffer.appendString(new String(cbuf, off, len));
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
      });

      return Future.succeededFuture(buffer);

    } catch (Exception ex) {
      return Future.failedFuture(ex);
    }
  }
}
