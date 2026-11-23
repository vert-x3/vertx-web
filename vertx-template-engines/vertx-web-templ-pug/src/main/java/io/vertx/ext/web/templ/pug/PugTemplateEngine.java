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

package io.vertx.ext.web.templ.pug;

import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.RenderContext;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.pug.impl.PugTemplateEngineImpl;

import java.nio.charset.StandardCharsets;

/**
 * A template engine that uses Pug.
 * <p>
 * An engine created with {@link #create(Vertx, PugEngine)} (or one of its overloads) renders through
 * the given {@link PugEngine}: the engine's own template loader governs template resolution,
 * {@link #engine()} returns that instance and the deprecated {@link #unwrap()} returns {@code null}.
 * <p>
 * An engine created with the remaining factories renders through the deprecated {@link PugConfiguration}, still exposed by {@link #unwrap()}.
 *
 * <p>Thís interface has been copied from <a href="https://github.com/vert-x3/vertx-web/blob/4.0.0/vertx-template-engines/vertx-web-templ-jade/src/main/java/io/vertx/ext/web/templ/jade/JadeTemplateEngine.java">
 * JadeTemplateEngine.java</a>.
 * Authors of JadeTemplateEngine.java are <a href="http://tfox.org">Tim Fox</a>, Paulo Lopes (pmlopes), Julien Viet (vietj),
 * Roman Novikov (mystdeim), nEJC (mrnejc), Yunyu Lin, Kevin Macksamie (k-mack), Geoffrey Clements (baldmountain).
 *
 * <p>For authors of this file see git history.
 */
@VertxGen
public interface PugTemplateEngine extends TemplateEngine {

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "pug";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static PugTemplateEngine create(Vertx vertx) {
    return new PugTemplateEngineImpl(vertx, DEFAULT_TEMPLATE_EXTENSION);
  }

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static PugTemplateEngine create(Vertx vertx, String extension) {
    return new PugTemplateEngineImpl(vertx, extension);
  }

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static PugTemplateEngine create(Vertx vertx, String extension, String encoding) {
    return new PugTemplateEngineImpl(vertx, extension, encoding);
  }

  /**
   * Create a template engine using the given engine, e.g. if you want to use custom
   * filters, a custom expression handler, or caching tuned by yourself.
   *
   * <p>The given engine is used as-is: its own template loader governs template resolution.
   * Rendering uses a {@code null} render context, which defaults to the Vert.x legacy
   * {@link de.neuland.pug4j.Pug4J.Mode#HTML} rendering.
   *
   * @return the engine
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static PugTemplateEngine create(Vertx vertx, PugEngine engine) {
    return create(vertx, DEFAULT_TEMPLATE_EXTENSION, StandardCharsets.UTF_8.name(), engine);
  }

  /**
   * Create a template engine using the given engine and render context, e.g. if you want
   * to use custom filters, a custom expression handler, caching tuned by yourself, or a
   * specific rendering mode.
   *
   * <p>The given engine is used as-is: its own template loader governs template resolution.
   * A {@code null} render context defaults to the Vert.x legacy
   * {@link de.neuland.pug4j.Pug4J.Mode#HTML} rendering.
   *
   * @return the engine
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static PugTemplateEngine create(Vertx vertx, PugEngine engine, RenderContext renderContext) {
    return create(vertx, DEFAULT_TEMPLATE_EXTENSION, StandardCharsets.UTF_8.name(), engine, renderContext);
  }

  /**
   * Create a template engine using the given engine, template extension and encoding,
   * e.g. if you want to use custom filters, a custom expression handler, or caching
   * tuned by yourself.
   *
   * <p>The given engine is used as-is: its own template loader governs template resolution.
   * Rendering uses a {@code null} render context, which defaults to the Vert.x legacy
   * {@link de.neuland.pug4j.Pug4J.Mode#HTML} rendering.
   *
   * @return the engine
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static PugTemplateEngine create(Vertx vertx, String extension, String encoding, PugEngine engine) {
    return new PugTemplateEngineImpl(vertx, extension, encoding, engine);
  }

  /**
   * Create a template engine using the given engine, template extension, encoding and
   * render context, e.g. if you want to use custom filters, a custom expression handler,
   * caching tuned by yourself, or a specific rendering mode.
   *
   * <p>The given engine is used as-is: its own template loader governs template resolution.
   * A {@code null} render context defaults to the Vert.x legacy
   * {@link de.neuland.pug4j.Pug4J.Mode#HTML} rendering.
   *
   * @return the engine
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static PugTemplateEngine create(Vertx vertx, String extension, String encoding, PugEngine engine, RenderContext renderContext) {
    return new PugTemplateEngineImpl(vertx, extension, encoding, engine, renderContext);
  }

  /**
   * Returns the underlying {@link PugEngine}.
   *
   * <p>This is the engine passed at creation time for engines created with
   * {@link #create(Vertx, PugEngine)}, or the engine backing the {@link PugConfiguration} returned by {@link #unwrap()}.
   *
   * @return the engine instance.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  PugEngine engine();

  /**
   * Returns the underlying {@link PugConfiguration}.
   *
   * <p>Configuration applied to the returned object must happen before the first render:
   * {@code mode}, {@code prettyPrint} and {@code sharedVariables} may be changed at any time,
   * while changes to the filters, template loader, expression handler or caching are ignored
   * once the underlying {@link PugEngine} has been created.
   *
   * @return the engine instance, or {@code null} when this engine was created from a {@link PugEngine}.
   * @deprecated instead build a {@link PugEngine} and pass it to
   * {@link #create(io.vertx.core.Vertx, PugEngine)} or use {@link #engine()}.
   * {@code PugConfiguration} is deprecated for removal in pug4j 4.0.0, this method
   * returns {@code null} for engines created from a {@link PugEngine} and will be
   * removed in the next major version of Vert.x.
   */
  @Deprecated
  @SuppressWarnings("removal")
  @Override
  PugConfiguration unwrap();
}
