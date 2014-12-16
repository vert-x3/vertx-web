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

package io.vertx.ext.apex.addons.impl.templengines;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.apex.addons.JadeTemplateEngine;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.core.impl.Utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class JadeTemplateEngineImpl extends CachingTemplateEngine<JadeTemplate> implements JadeTemplateEngine {

  private final JadeConfiguration config = new JadeConfiguration();

  public JadeTemplateEngineImpl(String resourcePrefix, String ext, int maxCacheSize) {
    super(resourcePrefix, ext, maxCacheSize);

    long lastMod = System.currentTimeMillis();

    config.setTemplateLoader(new TemplateLoader() {
      @Override
      public long getLastModified(String name) throws IOException {
        return lastMod;
      }

      @Override
      public Reader getReader(String name) throws IOException {
        name = adjustLocation(name);
        String templ = Utils.readResourceToString(name);
        if (templ == null) {
          throw new IllegalArgumentException("Cannot find resource " + name);
        }
        return new StringReader(templ);
      }
    });
  }

  @Override
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    try {
      JadeTemplate template = cache.get(templateFileName);

      if (template == null) {
        // real compile
        template = config.getTemplate(templateFileName);
        cache.put(templateFileName, template);
      }
      handler.handle(Future.succeededFuture(Buffer.buffer(config.renderTemplate(template, context.contextData()))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

}
