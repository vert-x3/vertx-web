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

package io.vertx.ext.web.templ.groovy;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.groovy.impl.GroovyTemplateEngineImpl;

/**
 * A template engine that uses the <a href="https://docs.groovy-lang.org/docs/next/html/documentation/template-engines.html">Groovy-Templates library</a>.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface GroovyTemplateEngine extends TemplateEngine {

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "templ";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static GroovyTemplateEngine create(Vertx vertx) {
    return new GroovyTemplateEngineImpl(vertx, DEFAULT_TEMPLATE_EXTENSION);
  }

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static GroovyTemplateEngine create(Vertx vertx, String extension) {
    return new GroovyTemplateEngineImpl(vertx, extension);
  }
}
