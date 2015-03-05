/*
 * Copyright 2014 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 and Apache License v2.0 which accompanies this distribution. The
 * Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html The Apache License v2.0 is available
 * at http://www.opensource.org/licenses/apache2.0.php You may elect to redistribute this code under either of these
 * licenses.
 */

package io.vertx.ext.apex.templ.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.impl.Utils;
import io.vertx.ext.apex.templ.ThymeleafTemplateEngine;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Locale;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.VariablesMap;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ThymeleafTemplateEngineImpl implements ThymeleafTemplateEngine {

  private final TemplateEngine   engine   = new TemplateEngine();
  private final TemplateResolver templateResolver;
  private final ResourceResolver resolver = new ResourceResolver();

  public ThymeleafTemplateEngineImpl() {
    templateResolver = new TemplateResolver();
    templateResolver.setTemplateMode(ThymeleafTemplateEngine.DEFAULT_TEMPLATE_MODE);
    templateResolver.setResourceResolver(resolver);
    engine.setTemplateResolver(templateResolver);
  }

  @Override
  public ThymeleafTemplateEngine setMode(String mode) {
    templateResolver.setTemplateMode(ThymeleafTemplateEngine.DEFAULT_TEMPLATE_MODE);
    return this;
  }

  @Override
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    Buffer buffer = Buffer.buffer();
    try {
      // Not very happy making a copy here... and it seems Thymeleaf copies the data again internally as well...
      VariablesMap<String, Object> data = new VariablesMap<>();
      data.put("context", context);

      // Need to synchronized to make sure right Vert.x is used!
      synchronized (this) {
        resolver.setVertx(context.vertx());

        engine.process(templateFileName, new ApexIContext(data), new Writer() {
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

  /*
   * (non-Javadoc)
   * @see io.vertx.ext.apex.templ.ThymeleafTemplateEngine#setCacheable(boolean)
   */
  @Override
  public ThymeleafTemplateEngine setCacheable(boolean cacheable) {
    templateResolver.setCacheable(cacheable);
    return this;
  }

  /*
   * (non-Javadoc)
   * @see io.vertx.ext.apex.templ.ThymeleafTemplateEngine#setCacheTTLMs(java.lang.Long)
   */
  @Override
  public ThymeleafTemplateEngine setCacheTTLMs(Long cacheTTLMs) {
    templateResolver.setCacheTTLMs(cacheTTLMs);
    return this;
  }

  /*
   * We extend VariablesMap to avoid copying all context map data for each render We put the context data Map directly
   * into the variable map and we also provide variables called: _context - this is the routing context itself _request
   * - this is the HttpServerRequest object _response - this is the HttpServerResponse object
   */
  private static class ApexIContext implements IContext {

    private final VariablesMap<String, Object> data;

    private ApexIContext(VariablesMap<String, Object> data) {
      this.data = data;
    }

    @Override
    public VariablesMap<String, Object> getVariables() {
      return data;
    }

    @Override
    public Locale getLocale() {
      return Locale.getDefault();
    }

    @Override
    public void addContextExecutionInfo(String templateName) {
    }

  }

  private static class ResourceResolver implements IResourceResolver {

    private Vertx vertx;

    void setVertx(Vertx vertx) {
      this.vertx = vertx;
    }

    @Override
    public String getName() {
      return "Apex/Thymeleaf";
    }

    @Override
    public InputStream getResourceAsStream(TemplateProcessingParameters templateProcessingParameters,
        String resourceName) {
      String str = Utils.readFileToString(vertx, resourceName);
      try {
        ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes("UTF-8"));
        BufferedInputStream buis = new BufferedInputStream(bis);
        return buis;
      } catch (UnsupportedEncodingException e) {
        throw new VertxException(e);
      }
    }
  }

}
