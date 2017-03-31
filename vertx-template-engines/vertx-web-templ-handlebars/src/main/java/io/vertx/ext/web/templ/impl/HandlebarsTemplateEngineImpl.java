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

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.ValueResolver;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlebarsTemplateEngineImpl extends CachingTemplateEngine<Template> implements HandlebarsTemplateEngine {

  private final Handlebars handlebars;
  private final Loader loader = new Loader();
  private final ValueResolver[] DEFAULT_VERTX_RESOLVERS = {
    JsonArrayValueResolver.INSTANCE,
    JsonObjectValueResolver.INSTANCE
  };

  private ValueResolver[] resolvers = ArrayUtils.addAll(DEFAULT_VERTX_RESOLVERS, ValueResolver.VALUE_RESOLVERS);

  public HandlebarsTemplateEngineImpl() {
    super(HandlebarsTemplateEngine.DEFAULT_TEMPLATE_EXTENSION,
            HandlebarsTemplateEngine.DEFAULT_TEMPLATE_PATH,
            HandlebarsTemplateEngine.DEFAULT_MAX_CACHE_SIZE);
    handlebars = new Handlebars(loader);
  }

  @Override
  public HandlebarsTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public HandlebarsTemplateEngine setPath(String path) {
    doSetPath(path);
    return this;
  }

  @Override
  public HandlebarsTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    try {
      Template template = cache.get(templateFileName);
      if (template == null) {
        synchronized (this) {
          loader.setVertx(context.vertx());
          template = handlebars.compile(templateFileName);
          cache.put(templateFileName, template);
        }
      }
      Context engineContext = Context.newBuilder(context.data()).resolver(getResolvers()).build();
      handler.handle(Future.succeededFuture(Buffer.buffer(template.apply(engineContext))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  @Override
  public Handlebars getHandlebars() {
    return handlebars;
  }

  @Override
  public ValueResolver[] getResolvers() {
    return resolvers;
  }

  @Override
  public HandlebarsTemplateEngine setResolvers(ValueResolver... resolvers) {
    this.resolvers = resolvers;
    return this;
  }

  private class Loader implements TemplateLoader {

    private Vertx vertx;

    void setVertx(Vertx vertx) {
      this.vertx = vertx;
    }

    @Override
    public TemplateSource sourceAt(String location) throws IOException {

      String loc = adjustLocation(location);
      String templ = Utils.readFileToString(vertx, loc);

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
      return path;
    }

    @Override
    public String getSuffix() {
      return extension;
    }

    @Override
    public void setPrefix(String prefix) {
      path = prefix;
    }

    @Override
    public void setSuffix(String suffix) {
      extension = suffix;
    }
  }


}
