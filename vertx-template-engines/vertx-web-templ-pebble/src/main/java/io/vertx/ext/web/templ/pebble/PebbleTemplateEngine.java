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

package io.vertx.ext.web.templ.pebble;

import com.mitchellbosecke.pebble.PebbleEngine;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.pebble.impl.PebbleTemplateEngineImpl;

/**
 * A template engine that uses the Pebble library.
 *
 * @author Dan Kristensen
 */
@VertxGen
public interface PebbleTemplateEngine extends TemplateEngine {

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "peb";

  /**
   * Create a template engine using defaults
   *
   * @return the engine
   */
  static PebbleTemplateEngine create(Vertx vertx) {
    return new PebbleTemplateEngineImpl(vertx, DEFAULT_TEMPLATE_EXTENSION);
  }

  /**
   * Create a template engine using defaults
   *
   * @return the engine
   */
  static PebbleTemplateEngine create(Vertx vertx, String extension) {
    return new PebbleTemplateEngineImpl(vertx, extension);
  }

  /**
   * Create a template engine using a custom Builder, e.g. if
   * you want use custom Filters or Functions.
   *
   * @return the engine
   */
  @GenIgnore
  static PebbleTemplateEngine create(Vertx vertx, PebbleEngine engine) {
    return new PebbleTemplateEngineImpl(vertx, DEFAULT_TEMPLATE_EXTENSION, engine);
  }

  /**
   * Create a template engine using a custom Builder, e.g. if
   * you want use custom Filters or Functions.
   *
   * @return the engine
   */
  @GenIgnore
  static PebbleTemplateEngine create(Vertx vertx, String extension, PebbleEngine engine) {
    return new PebbleTemplateEngineImpl(vertx, extension, engine);
  }
}
