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

package io.vertx.ext.web.templ.handlebars.impl;

import java.nio.charset.Charset;
import java.util.Map;

import io.vertx.ext.web.common.template.CachingTemplateEngine;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.ValueResolver;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlebarsTemplateEngineImpl extends CachingTemplateEngine<Template> implements HandlebarsTemplateEngine {

  private final Handlebars handlebars;
  private final Loader loader;

  private ValueResolver[] resolvers;

  public HandlebarsTemplateEngineImpl(Vertx vertx) {
    super(HandlebarsTemplateEngine.DEFAULT_TEMPLATE_EXTENSION, HandlebarsTemplateEngine.DEFAULT_MAX_CACHE_SIZE);
    loader = new Loader(vertx);
    // custom resolvers
    resolvers = new ValueResolver[ValueResolver.VALUE_RESOLVERS.length + 2];
    // custom resolvers for vertx json types
    resolvers[0] = JsonArrayValueResolver.INSTANCE;
    resolvers[1] = JsonObjectValueResolver.INSTANCE;
    // default resolvers
    System.arraycopy(ValueResolver.VALUE_RESOLVERS, 0, resolvers, 2, ValueResolver.VALUE_RESOLVERS.length);
    // create the engine
    handlebars = new Handlebars(loader);
  }

  @Override
  public HandlebarsTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public HandlebarsTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      int idx = templateFile.lastIndexOf('/');
      String prefix = "";
      String basename = templateFile;
      if (idx != -1) {
        prefix = templateFile.substring(0, idx);
        basename = templateFile.substring(idx + 1);
      }
      Template template = isCachingEnabled() ? cache.get(templateFile) : null;
      if (template == null) {
        synchronized (this) {
          loader.setPrefix(prefix);
          // Strip leading slash from Utils##normalizePath
          template = handlebars.compile(basename);
          if (isCachingEnabled()) {
            cache.put(templateFile, template);
          }
        }
      }
      Context engineContext = Context.newBuilder(context).resolver(getResolvers()).build();
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
    private final Vertx vertx;
    private String templateDirectory;
    private Charset charset = Charset.defaultCharset();

    Loader(Vertx vertx) {
      this.vertx = vertx;
    }

    @Override
    public TemplateSource sourceAt(String location) {
      final String loc = resolve(location);
      final Buffer templ;

      if (vertx.fileSystem().existsBlocking(loc)) {
        templ = vertx.fileSystem()
          .readFileBlocking(loc);
      } else {
        templ = null;
      }

      if (templ == null) {
        throw new IllegalArgumentException("Cannot find resource " + loc);
      }

      long lastMod = System.currentTimeMillis();

      return new TemplateSource() {
        @Override
        public String content(Charset charset) {
          return templ.toString(charset);
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
      return templateDirectory + "/" + adjustLocation(location);
    }

    @Override
    public String getPrefix() {
      return templateDirectory;
    }

    @Override
    public String getSuffix() {
      return extension;
    }

    @Override
    public void setPrefix(String prefix) {
      templateDirectory = prefix;
    }

    @Override
    public void setSuffix(String suffix) {
      extension = suffix;
    }

    @Override
    public void setCharset(Charset charset) {
      this.charset = charset;
    }

    @Override
    public Charset getCharset() {
      return charset;
    }
  }
}
