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

  public JadeTemplateEngineImpl(Vertx vertx) {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
    config.setTemplateLoader(new JadeTemplateLoader(vertx));
    config.setCaching(false);
  }

  @Override
  public JadeTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public JadeTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      JadeTemplate template = isCachingEnabled() ? cache.get(templateFile) : null;

      if (template == null) {
        synchronized (this) {
          // Compile
          template = config.getTemplate(templateFile);
        }
        if (isCachingEnabled()) {
          cache.put(templateFile, template);
        }
      }
      handler.handle(Future.succeededFuture(Buffer.buffer(config.renderTemplate(template, context))));
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
    private long lastMod = System.currentTimeMillis();

    JadeTemplateLoader(Vertx vertx) {
      this.vertx = vertx;
    }

    @Override
    public long getLastModified(String name) {
      return lastMod;
    }

    @Override
    public String getExtension() {
      return "jade";
    }

    @Override
    public Reader getReader(String name) throws IOException {
      // the internal loader will always resolve with .jade extension
      name = adjustLocation(name.endsWith(".jade") ? name.substring(0, name.length() - 5) : name);
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
