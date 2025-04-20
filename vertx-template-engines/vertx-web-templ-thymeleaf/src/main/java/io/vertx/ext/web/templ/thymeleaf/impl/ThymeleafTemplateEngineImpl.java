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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.WebEnvironment;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;

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

  private final TemplateEngine templateEngine = new TemplateEngine();
  private final ResourceTemplateResolver templateResolver;

  public ThymeleafTemplateEngineImpl(Vertx vertx) {
    ResourceTemplateResolver templateResolver = new ResourceTemplateResolver(vertx);
    templateResolver.setCacheable(!WebEnvironment.development());
    templateResolver.setTemplateMode(ThymeleafTemplateEngine.DEFAULT_TEMPLATE_MODE);

    this.templateResolver = templateResolver;
    this.templateEngine.setTemplateResolver(templateResolver);
    // There's no servlet context in Vert.x, so we override default link builder
    // See https://github.com/vert-x3/vertx-web/issues/161
    this.templateEngine.setLinkBuilder(new StandardLinkBuilder() {
      @Override
      protected String computeContextPath(
        final IExpressionContext context, final String base, final Map<String, Object> parameters) {
        return "/";
      }
    });
  }

  @Override
  public TemplateEngine unwrap() {
    return templateEngine;
  }

  @Override
  public void clearCache() {
    templateEngine.clearTemplateCache();
  }

  @Override
  public Future<Buffer> render(Map<String, Object> context, String templateFile) {
    Buffer buffer = Buffer.buffer();

    try {
      synchronized (this) {
        templateEngine.process(templateFile, new WebIContext(context, (String) context.get("lang")), new Writer() {
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
      }

      return Future.succeededFuture(buffer);
    } catch (Exception ex) {
      return Future.failedFuture(ex);
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

    ResourceTemplateResolver(Vertx vertx) {
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
