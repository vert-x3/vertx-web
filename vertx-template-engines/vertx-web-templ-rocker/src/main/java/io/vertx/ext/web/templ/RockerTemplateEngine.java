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

package io.vertx.ext.web.templ;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.templ.impl.RockerTemplateEngineImpl;

/**
 * A template engine that uses the Rocker library.
 *
 * @author <a href="mailto:xianguang.zhou@outlook.com">Xianguang Zhou</a>
 */
@VertxGen
public interface RockerTemplateEngine extends TemplateEngine {

  /**
   * Default max number of templates to cache
   */
  int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "rocker.html";

  /**
   * Create a template engine using defaults
   *
   * @return the engine
   */
  static RockerTemplateEngine create() {
    return new RockerTemplateEngineImpl();
  }

  /**
   * Set the extension for the engine
   *
   * @param extension
   *          the extension
   * @return a reference to this for fluency
   */
  RockerTemplateEngine setExtension(String extension);

  /**
   * Set the max cache size for the engine
   *
   * @param maxCacheSize
   *          the maxCacheSize
   * @return a reference to this for fluency
   */
  RockerTemplateEngine setMaxCacheSize(int maxCacheSize);
}
