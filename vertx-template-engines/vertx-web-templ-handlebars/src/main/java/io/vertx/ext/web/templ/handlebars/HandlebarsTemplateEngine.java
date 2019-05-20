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

package io.vertx.ext.web.templ.handlebars;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.ValueResolver;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.handlebars.impl.HandlebarsTemplateEngineImpl;

/**
 * A template engine that uses the Handlebars library.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface HandlebarsTemplateEngine extends TemplateEngine {

  /**
   * Default max number of templates to cache
   */
  int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default template extension
   */
  @Deprecated
  String DEFAULT_TEMPLATE_EXTENSION = "hbs";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static HandlebarsTemplateEngine create(Vertx vertx) {
    return new HandlebarsTemplateEngineImpl(vertx);
  }

  /**
   * @deprecated as a user you should use filename with extensions on the render method instead of relying
   * on this method to suffix your filenames. Using this method is quite an opinionated API and has the side
   * effect that you cannot use files without extensions as templates.
   *
   * Set the extension for the engine
   *
   * @param extension  the extension
   * @return a reference to this for fluency
   */
  @Fluent
  @Deprecated
  HandlebarsTemplateEngine setExtension(String extension);

  /**
   * Set the max cache size for the engine
   *
   * @param maxCacheSize  the maxCacheSize
   * @return a reference to this for fluency
   */
  @Fluent
  HandlebarsTemplateEngine setMaxCacheSize(int maxCacheSize);

  /**
   * Get a reference to the internal Handlebars object so it
   * can be configured.
   *
   * @return a reference to the internal Handlebars instance.
   */
  @GenIgnore
  Handlebars getHandlebars();

  /**
   * Return the array of configured handlebars context value resolvers.
   * @return array of configured resolvers
   */
  @GenIgnore
  ValueResolver[] getResolvers();

  /**
   * Set the array of handlebars context value resolvers.
   *
   * @param resolvers the value resolvers to be used
   * @return a reference to the internal Handlebars instance.
   */
  @GenIgnore
  HandlebarsTemplateEngine setResolvers(ValueResolver... resolvers);
}
