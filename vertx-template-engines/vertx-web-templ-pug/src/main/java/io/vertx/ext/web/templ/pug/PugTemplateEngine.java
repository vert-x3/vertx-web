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

package io.vertx.ext.web.templ.pug;

import de.neuland.pug4j.PugConfiguration;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.pug.impl.PugTemplateEngineImpl;

/**
 * A template engine that uses Pug.
 * The {@link #unwrap()} shall return an object of class {@link PugConfiguration}
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

  @Override
  PugConfiguration unwrap();
}
