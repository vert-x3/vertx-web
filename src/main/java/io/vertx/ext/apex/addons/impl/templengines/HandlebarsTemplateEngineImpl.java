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

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.apex.addons.HandlebarsTemplateEngine;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.core.impl.Utils;

import java.io.IOException;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlebarsTemplateEngineImpl extends CachingTemplateEngine<Template> implements HandlebarsTemplateEngine {

  private final Handlebars handlebars;

  public HandlebarsTemplateEngineImpl(String resourcePrefix, String ext, int maxCacheSize) {
    super(resourcePrefix, ext, maxCacheSize);

    handlebars = new Handlebars(new TemplateLoader() {
      @Override
      public TemplateSource sourceAt(String location) throws IOException {

        final String loc = adjustLocation(location);

        String templ = Utils.readResourceToString(loc);

        if (templ == null) {
          throw new IllegalArgumentException("Cannot find resource " + loc);
        }

        long lastMod = System.currentTimeMillis();

        return new TemplateSource() {
          @Override
          public String content() throws IOException {
            // load from the file system
            return templ;
          }

          @Override
          public String filename() {
            return loc;
          }

          @Override
          public long lastModified() {
            return lastMod;
          }
        };
      }

      @Override
      public String resolve(String location) {
        return location;
      }

      @Override
      public String getPrefix() {
        return prefix;
      }

      @Override
      public String getSuffix() {
        return extension;
      }
    });
  }

  @Override
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    try {
      Template template = cache.get(templateFileName);
      if (template == null) {
        template = handlebars.compile(templateFileName);
        cache.put(templateFileName, template);
      }
      handler.handle(Future.succeededFuture(Buffer.buffer(template.apply(context.contextData()))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }


}
