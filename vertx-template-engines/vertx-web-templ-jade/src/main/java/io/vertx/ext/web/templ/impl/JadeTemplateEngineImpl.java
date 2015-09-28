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

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.templ.JadeTemplateEngine;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class JadeTemplateEngineImpl extends CachingTemplateEngine<JadeTemplate> implements JadeTemplateEngine {

  private final JadeConfiguration config = new JadeConfiguration();
  private final JadeTemplateLoader loader = new JadeTemplateLoader();

  public JadeTemplateEngineImpl() {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
    config.setTemplateLoader(loader);
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
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    try {
      JadeTemplate template = cache.get(templateFileName);

      if (template == null) {
        synchronized (this) {
          loader.setVertx(context.vertx());
          // Compile
          template = config.getTemplate(templateFileName);
        }
        cache.put(templateFileName, template);
      }
      Map<String, Object> variables = new HashMap<>(1);
      variables.put("context", context);
      handler.handle(Future.succeededFuture(Buffer.buffer(config.renderTemplate(template, variables))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  @Override
  public JadeConfiguration getJadeConfiguration() {
    return config;
  }

  private class JadeTemplateLoader implements TemplateLoader {

    private Vertx vertx;
    private long lastMod = System.currentTimeMillis();

    void setVertx(Vertx vertx) {
      this.vertx = vertx;
    }

    @Override
    public long getLastModified(String name) throws IOException {
      return lastMod;
    }

    @Override
    public Reader getReader(String name) throws IOException {
      name = adjustLocation(name);
      String templ = Utils.readFileToString(vertx, name);
      if (templ == null) {
        throw new IllegalArgumentException("Cannot find resource " + name);
      }
      return new StringReader(templ);
    }
  }


}
