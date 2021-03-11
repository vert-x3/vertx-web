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

package io.vertx.ext.web.templ.jte.impl;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.templ.jte.JteTemplateEngine;

import java.util.Map;


/**
 * @author <a href="mailto:andy@mazebert.com">Andreas Hager</a>
 */
public class JteTemplateEngineImpl implements JteTemplateEngine {

  private final TemplateEngine templateEngine;
  private final VertxDirectoryCodeResolver codeResolver;

  /**
   * Creates a vert.x template engine for the given jte template engine.
   * <p>
   * Use this method if you want full control over the used engine.
   * <p>
   * For instance, it is recommended to use the jte-maven-plugin to precompile all jte templates
   * during maven build. If you do so, you can pass a precompiled engine when running in production.
   *
   * @param engine a configured engine instance
   */
  public JteTemplateEngineImpl(gg.jte.TemplateEngine engine) {
    templateEngine = engine;
    codeResolver = null;
  }

  public JteTemplateEngineImpl(Vertx vertx, String templateRootDirectory) {
    codeResolver = new VertxDirectoryCodeResolver(vertx, templateRootDirectory);
    templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
    templateEngine.setBinaryStaticContent(true);
  }

  public JteTemplateEngineImpl() {
    codeResolver = null;
    templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      Utf8ByteOutput output = new Utf8ByteOutput();
      templateEngine.render(templateFile, context, output);
      handler.handle(Future.succeededFuture(JteBufferUtil.toBuffer(output)));
    } catch (RuntimeException ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  @Override
  public void clearCache() {
    if (codeResolver != null) {
      codeResolver.clear();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T unwrap() throws ClassCastException {
    return (T) templateEngine;
  }
}
