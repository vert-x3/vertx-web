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

package io.vertx.ext.web.templ.jade.impl;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.impl.TemplateHolder;
import io.vertx.ext.web.templ.jade.JadeTemplateEngine;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class JadeTemplateEngineImpl extends CachingTemplateEngine<JadeTemplate> implements JadeTemplateEngine {

  private final JadeConfiguration config = new JadeConfiguration();

  public JadeTemplateEngineImpl(Vertx vertx, String extension) {
    super(vertx, extension);
    config.setTemplateLoader(new JadeTemplateLoader(vertx));
    config.setCaching(false);
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      String src = adjustLocation(templateFile);
      TemplateHolder<JadeTemplate> template = getTemplate(src);

      if (template == null) {
        synchronized (this) {
          // Compile
          template = new TemplateHolder<>(config.getTemplate(src));
        }
        putTemplate(src, template);
      }
      handler.handle(Future.succeededFuture(Buffer.buffer(config.renderTemplate(template.template(), context))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  @Override
  public JadeConfiguration getJadeConfiguration() {
    return config;
  }

  private class JadeTemplateLoader implements TemplateLoader {

    private final Vertx vertx;

    JadeTemplateLoader(Vertx vertx) {
      this.vertx = vertx;
    }

    @Override
    public long getLastModified(String name) throws IOException {
      name = adjustLocation(name);
      try {
        if (vertx.fileSystem().existsBlocking(name)) {
          return vertx.fileSystem().propsBlocking(name).lastModifiedTime();
        } else {
          throw new IOException("Cannot find resource " + name);
        }
      } catch (RuntimeException e) {
        throw new IOException("Unexpected exception", e);
      }
    }

    @Override
    public String getExtension() {
      return "jade";
    }

    @Override
    public Reader getReader(String name) throws IOException {
      // the internal loader will always resolve with .jade extension
      name = adjustLocation(name);
      String templ = null;

      if (vertx.fileSystem().existsBlocking(name)) {
        templ = vertx.fileSystem()
          .readFileBlocking(name)
          .toString(Charset.defaultCharset());
      }

      if (templ == null) {
        throw new IOException("Cannot find resource " + name);
      }
      return new StringReader(templ);
    }
  }
}
