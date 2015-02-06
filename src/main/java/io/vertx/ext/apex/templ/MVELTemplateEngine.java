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
import io.vertx.ext.apex.templ.impl.MVELTemplateEngineImpl;

/**
 * A template engine that uses the Handlebars library.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface MVELTemplateEngine extends TemplateEngine {

  /**
   * Default max number of templates to cache
   */
  static final int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default template extension
   */
  static final String DEFAULT_TEMPLATE_EXTENSION = "templ";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static MVELTemplateEngine create() {
    return new MVELTemplateEngineImpl(null, DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
  }

  /**
   * Create a template engine
   *
   * @param resourcePrefix  the resource prefix
   * @param extension  the extension
   * @return  the engine
   */
  static MVELTemplateEngine create(String resourcePrefix, String extension) {
    return new MVELTemplateEngineImpl(resourcePrefix, extension, DEFAULT_MAX_CACHE_SIZE);
  }

  /**
   * Create a template engine
   *
   * @param resourcePrefix  the resource prefix
   * @param extension  the extension
   * @param maxCacheSize  the max cache size
   * @return  the engine
   */
  static MVELTemplateEngine create(String resourcePrefix, String extension, int maxCacheSize) {
    return new MVELTemplateEngineImpl(resourcePrefix, extension, maxCacheSize);
  }
}
