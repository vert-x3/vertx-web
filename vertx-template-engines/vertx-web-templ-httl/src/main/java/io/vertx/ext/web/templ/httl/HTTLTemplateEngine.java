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

package io.vertx.ext.web.templ.httl;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.httl.impl.HTTLTemplateEngineImpl;

/**
 * A template engine that uses the HTTL library.
 * The {@link #unwrap()} shall return an object of class {@link httl.Engine}
 *
 * @author <a href="mailto:victorqrsilva@gmail.com">Victor Quezado</a>
 */
@VertxGen
public interface HTTLTemplateEngine extends TemplateEngine {

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "httl";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static HTTLTemplateEngine create(Vertx vertx) {
    return new HTTLTemplateEngineImpl(vertx, DEFAULT_TEMPLATE_EXTENSION);
  }

  /**
   * Create a template engine using defaults and custom file extension
   *
   * @return  the engine
   */
  static HTTLTemplateEngine create(Vertx vertx, String extension) {
    return new HTTLTemplateEngineImpl(vertx, extension);
  }
}
