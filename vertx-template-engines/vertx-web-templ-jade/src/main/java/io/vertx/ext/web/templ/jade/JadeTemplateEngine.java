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

package io.vertx.ext.web.templ.jade;

import de.neuland.jade4j.JadeConfiguration;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.jade.impl.JadeTemplateEngineImpl;

/**
 * A template engine that uses Jade.
 * The {@link #unwrap()} shall return an object of class {@link JadeConfiguration}
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface JadeTemplateEngine extends TemplateEngine {

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "jade";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static JadeTemplateEngine create(Vertx vertx) {
    return new JadeTemplateEngineImpl(vertx, DEFAULT_TEMPLATE_EXTENSION);
  }

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static JadeTemplateEngine create(Vertx vertx, String extension) {
    return new JadeTemplateEngineImpl(vertx, extension);
  }

  /**
   * @deprecated see {@link #unwrap()}
   * Get a reference to the internal JadeConfiguration object so it
   * can be configured.
   *
   * @return a reference to the internal JadeConfiguration instance.
   */
  @GenIgnore
  @Deprecated
  JadeConfiguration getJadeConfiguration();
}
