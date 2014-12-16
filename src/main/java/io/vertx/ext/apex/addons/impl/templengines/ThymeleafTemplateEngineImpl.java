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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.apex.addons.AbstractTemplateEngine;
import io.vertx.ext.apex.addons.ThymeleafTemplateEngine;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.core.impl.Utils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.VariablesMap;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Locale;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ThymeleafTemplateEngineImpl extends AbstractTemplateEngine implements ThymeleafTemplateEngine {

  private final TemplateEngine engine = new TemplateEngine();

  public ThymeleafTemplateEngineImpl() {
    this(null, "XHTML");
  }

  public ThymeleafTemplateEngineImpl(String resourcePrefix, String templateMode) {

    if (resourcePrefix != null && !resourcePrefix.endsWith("/")) {
      resourcePrefix += "/";
    }

    TemplateResolver templateResolver = new TemplateResolver();

    // XHTML is the default mode, but we will set it anyway for better understanding of code
    templateResolver.setTemplateMode(templateMode);
    if (resourcePrefix != null) {
      templateResolver.setPrefix(resourcePrefix);
    }

    templateResolver.setResourceResolver(new IResourceResolver() {

      @Override
      public String getName() {
        return "Apex/Thymeleaf";
      }

      @Override
      public InputStream getResourceAsStream(TemplateProcessingParameters templateProcessingParameters, String resourceName) {
        return Utils.getClassLoader().getResourceAsStream(resourceName);
      }
    });

    engine.setTemplateResolver(templateResolver);
  }

  @Override
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    Buffer buffer = Buffer.buffer();
    try {
      // Not very happy making a copy here... and it seems Thymeleaf copies the data again internally as well...
      VariablesMap<String, Object> data = new VariablesMap<>(context.contextData());
      data.put("_context", context);
      data.put("_request", context.request());
      data.put("_response", context.response());

      engine.process(templateFileName, new ApexIContext(data), new Writer() {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
          buffer.appendString(new String(cbuf, off, len));
        }

        @Override
        public void flush() throws IOException {}

        @Override
        public void close() throws IOException {
        }
      });
      handler.handle(Future.succeededFuture(buffer));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  /*
   We extend VariablesMap to avoid copying all context map data for each render
   We put the context data Map directly into the variable map and we also provide
   variables called:
   _context - this is the routing context itself
   _request - this is the HttpServerRequest object
   _response - this is the HttpServerResponse object
    */
  private static class ApexIContext implements IContext  {

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


}
