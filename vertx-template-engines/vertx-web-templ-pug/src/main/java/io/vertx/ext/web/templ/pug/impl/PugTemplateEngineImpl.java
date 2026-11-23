/*
 * Copyright (c) 2011-2026 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.templ.pug.impl;

import de.neuland.pug4j.Pug4J;
import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.RenderContext;
import de.neuland.pug4j.template.PugTemplate;
import de.neuland.pug4j.template.TemplateLoader;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.common.template.CachingTemplateEngine;
import io.vertx.ext.web.common.template.CachedTemplate;
import io.vertx.ext.web.templ.pug.PugTemplateEngine;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

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
@SuppressWarnings("removal")
public class PugTemplateEngineImpl extends CachingTemplateEngine<PugTemplate> implements PugTemplateEngine {

  // Render-time defaults preserving the Vert.x 5 output contract for doctype-less templates:
  // the deprecated PugConfiguration defaults to Mode.HTML, while a RenderContext defaults to XHTML.
  private static final RenderContext LEGACY_RENDER_CONTEXT = RenderContext.builder().defaultMode(Pug4J.Mode.HTML).build();

  private final PugConfiguration config;     // non-null on the default (deprecated) path only
  private final PugEngine engine;            // non-null on the engine path only
  private final RenderContext renderContext; // engine path only, never null there
  private final Charset encoding;

  /**
   * Constructor that reads the template file with UTF-8 encoding.
   */
  public PugTemplateEngineImpl(Vertx vertx, String extension) {
    this(vertx, extension, StandardCharsets.UTF_8.name());
  }

  /**
   * Default path: renders through the deprecated pug4j 2.x configuration, using the Vert.x
   * template loader and the Vert.x cache only (pug4j caching disabled).
   */
  public PugTemplateEngineImpl(Vertx vertx, String extension, String encoding) {
    super(vertx, extension);
    this.config = new PugConfiguration();
    config.setTemplateLoader(new PugTemplateLoader(vertx));
    config.setCaching(false);
    this.engine = null;
    this.renderContext = null;
    this.encoding = Charset.forName(encoding);
  }

  /**
   * Engine path: renders through the given pug4j 3.x engine with the Vert.x legacy
   * {@link Pug4J.Mode#HTML} rendering.
   */
  public PugTemplateEngineImpl(Vertx vertx, String extension, String encoding, PugEngine engine) {
    this(vertx, extension, encoding, engine, null);
  }

  /**
   * Engine path: renders through the given pug4j 3.x engine and render context. The engine is
   * used as-is: its own template loader governs template resolution. A {@code null} render
   * context defaults to the Vert.x legacy {@link Pug4J.Mode#HTML} rendering.
   */
  public PugTemplateEngineImpl(Vertx vertx, String extension, String encoding, PugEngine engine, RenderContext renderContext) {
    super(vertx, extension);
    this.config = null;
    this.engine = Objects.requireNonNull(engine, "engine");
    this.renderContext = renderContext != null ? renderContext : LEGACY_RENDER_CONTEXT;
    this.encoding = Charset.forName(encoding);
  }

  @Override
  public PugEngine engine() {
    return engine != null ? engine : config.getOrCreateEngine();
  }

  @Override
  public PugConfiguration unwrap() {
    return config;
  }

  @Override
  public Future<Buffer> render(Map<String, Object> context, String templateFile) {
    try {
      String src = adjustLocation(templateFile);
      CachedTemplate<PugTemplate> template = getTemplate(src);

      if (template == null) {
        synchronized (this) {
          // Compile
          template = new CachedTemplate<>(engine != null ? engine.getTemplate(src) : config.getTemplate(src));
        }
        putTemplate(src, template);
      }
      String output = engine != null
        ? engine.render(template.template(), context, renderContext)
        : config.renderTemplate(template.template(), context);
      return Future.succeededFuture(Buffer.buffer(output));
    } catch (Exception ex) {
      return Future.failedFuture(ex);
    }
  }

  @Override
  public void clearCache() {
    super.clearCache();
    if (engine != null) {
      engine.clearCache();
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
