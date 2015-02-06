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

package io.vertx.ext.apex.templ;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.apex.templ.impl.ThymeleafTemplateEngineImpl;

/**
 * A template engine that uses the Thymeleaf library.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface ThymeleafTemplateEngine extends TemplateEngine {

  static final String DEFAULT_TEMPLATE_MODE = "XHTML";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static ThymeleafTemplateEngine create() {
    return new ThymeleafTemplateEngineImpl(null, DEFAULT_TEMPLATE_MODE);
  }

  /**
   * Create a template engine
   *
   * @param resourcePrefix  the resource prefix
   * @param templateMode  the template mode - e.g. XHTML
   * @return  the engine
   */
  static ThymeleafTemplateEngine create(String resourcePrefix, String templateMode) {
    return new ThymeleafTemplateEngineImpl(resourcePrefix, templateMode);
  }
}
