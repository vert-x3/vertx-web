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

package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.apex.addons.impl.templengines.JadeTemplateEngineImpl;

/**
 * Thread-safe but best performance is to be achieved by not sharing between contexts
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface JadeTemplateEngine extends TemplateEngine {

  static final int DEFAULT_MAX_CACHE_SIZE = 10000;
  static final String DEFAULT_TEMPLATE_EXTENSION = "jade";

  static JadeTemplateEngine create() {
    return new JadeTemplateEngineImpl(null, DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
  }

  static JadeTemplateEngine create(String resourcePrefix, String extension) {
    return new JadeTemplateEngineImpl(resourcePrefix, extension, DEFAULT_MAX_CACHE_SIZE);
  }

  static JadeTemplateEngine create(String resourcePrefix, String extension, int maxCacheSize) {
    return new JadeTemplateEngineImpl(resourcePrefix, extension, maxCacheSize);
  }
}
