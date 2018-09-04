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

package io.vertx.ext.web.templ.thymeleaf.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://matty.io">Matty Southall</a>
 */
public class ThymeleafTemplateEngineImpl implements ThymeleafTemplateEngine {

  // should not be static, so at at creation time the value is evaluated
  private final boolean enableCache = !Boolean.getBoolean(CachingTemplateEngine.DISABLE_TEMPL_CACHING_PROP_NAME);

  private final TemplateEngine templateEngine = new TemplateEngine();
  private ResourceTemplateResolver templateResolver;

  public ThymeleafTemplateEngineImpl(Vertx vertx) {
    ResourceTemplateResolver templateResolver = new ResourceTemplateResolver(vertx);
    templateResolver.setCacheable(isCachingEnabled());
    templateResolver.setTemplateMode(ThymeleafTemplateEngine.DEFAULT_TEMPLATE_MODE);

    this.templateResolver = templateResolver;
    this.templateEngine.setTemplateResolver(templateResolver);
  }

  @Override
  public boolean isCachingEnabled() {
    return enableCache;
  }

  @Override
  public ThymeleafTemplateEngine setMode(TemplateMode mode) {
    templateResolver.setTemplateMode(mode);
    return this;
  }

  @Override
  public TemplateEngine getThymeleafTemplateEngine() {
    return this.templateEngine;
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    Buffer buffer = Buffer.buffer();

    try {
      synchronized (this) {
        templateEngine.process(templateFile, new WebIContext(context, (String) context.get("lang")), new Writer() {
          @Override
          public void write(char[] cbuf, int off, int len) throws IOException {
            buffer.appendString(new String(cbuf, off, len));
          }

          @Override
          public void flush() throws IOException {
          }

          @Override
          public void close() throws IOException {
          }
        });
      }

      handler.handle(Future.succeededFuture(buffer));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  private static class WebIContext implements IContext {
    private final Map<String, Object> data;
    private final Locale locale;

    private WebIContext(Map<String, Object> data, String lang) {
      this.data = data;
      if (lang == null) {
        this.locale = Locale.getDefault();
      } else {
        this.locale = Locale.forLanguageTag(lang);
      }
    }

    @Override
    public java.util.Locale getLocale() {
      return locale;
    }

    @Override
    public boolean containsVariable(String name) {
      return data.containsKey(name);
    }

    @Override
    public Set<String> getVariableNames() {
      return data.keySet();
    }

    @Override
    public Object getVariable(String name) {
      return data.get(name);
    }
  }

  private static class ResourceTemplateResolver extends StringTemplateResolver {
    private final Vertx vertx;

    public ResourceTemplateResolver(Vertx vertx) {
      super();
      this.vertx = vertx;
      setName("vertx/Thymeleaf3");
    }

    @Override
    protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration, String ownerTemplate, String template, Map<String, Object> templateResolutionAttributes) {
      return new StringTemplateResource(
        vertx.fileSystem()
          .readFileBlocking(template)
          .toString(Charset.defaultCharset()));
    }
  }
}
