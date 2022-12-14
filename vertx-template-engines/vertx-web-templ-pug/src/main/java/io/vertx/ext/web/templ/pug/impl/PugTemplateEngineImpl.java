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

package io.vertx.ext.web.templ.pug.impl;

import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.template.PugTemplate;
import de.neuland.pug4j.template.TemplateLoader;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.impl.TemplateHolder;
import io.vertx.ext.web.templ.pug.PugTemplateEngine;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This implementation has been copied from
 * <a href="https://github.com/vert-x3/vertx-web/blob/4.0.0/vertx-template-engines/vertx-web-templ-jade/src/main/java/io/vertx/ext/web/templ/jade/impl/JadeTemplateEngineImpl.java">
 * JadeTemplateEngineImpl.java</a>.
 * Authors of JadeTemplateEngineImpl.java are <a href="http://pmlopes@gmail.com">Paulo Lopes</a>,
 * <a href="http://tfox.org">Tim Fox</a>, Julien Viet (vietj), Roman Novikov (mystdeim), nEJC (mrnejc), Yunyu Lin,
 * Kevin Macksamie (k-mack), Clement Escoffier (cescoffier), Geoffrey Clements (baldmountain).
 *
 * <p>For authors of this file see git history.
 */
public class PugTemplateEngineImpl extends CachingTemplateEngine<PugTemplate> implements PugTemplateEngine {

  private final PugConfiguration config = new PugConfiguration();
  private final Charset encoding;

  /**
   * Constructor that reads the template file with UTF-8 encoding.
   */
  public PugTemplateEngineImpl(Vertx vertx, String extension) {
    this(vertx, extension, StandardCharsets.UTF_8.name());
  }

  public PugTemplateEngineImpl(Vertx vertx, String extension, String encoding) {
    super(vertx, extension);
    config.setTemplateLoader(new PugTemplateLoader(vertx));
    config.setCaching(false);
    this.encoding = Charset.forName(encoding);
  }

  @Override
  public <T> T unwrap() {
    return (T) config;
  }

  @Override
  public void render(Map<String, Object> context, String templateFile, Handler<AsyncResult<Buffer>> handler) {
    try {
      String src = adjustLocation(templateFile);
      TemplateHolder<PugTemplate> template = getTemplate(src);

      if (template == null) {
        synchronized (this) {
          // Compile
          template = new TemplateHolder<>(config.getTemplate(src));
        }
        putTemplate(src, template);
      }
      handler.handle(Future.succeededFuture(Buffer.buffer(config.renderTemplate(template.template(), context))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }

  private class PugTemplateLoader implements TemplateLoader {

    private final Vertx vertx;

    PugTemplateLoader(Vertx vertx) {
      this.vertx = vertx;
    }

    @Override
    public long getLastModified(String name) throws IOException {
      name = adjustLocation(name);
      try {
        if (vertx.fileSystem().existsBlocking(name)) {
          return vertx.fileSystem().propsBlocking(name).lastModifiedTime();
        } else {
          throw new IOException("Cannot find resource " + name);
        }
      } catch (RuntimeException e) {
        throw new IOException("Unexpected exception", e);
      }
    }

    @Override
    public String getExtension() {
      return extension;
    }

    @Override
    public Reader getReader(String name) throws IOException {
      // the internal loader will always resolve with .pug extension
      name = adjustLocation(name);
      String templ = null;

      if (vertx.fileSystem().existsBlocking(name)) {
        templ = vertx.fileSystem()
          .readFileBlocking(name)
          .toString(encoding);
      }

      if (templ == null) {
        throw new IOException("Cannot find resource " + name);
      }
      return new StringReader(templ);
    }

    @Override
    public String getBase() {
      return "";
    }
  }
}
