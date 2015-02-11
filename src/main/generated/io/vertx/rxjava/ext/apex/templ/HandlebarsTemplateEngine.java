/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.rxjava.ext.apex.templ;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;

/**
 * A template engine that uses the Handlebars library.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 * NOTE: This class has been automatically generated from the original non RX-ified interface using Vert.x codegen.
 */

public class HandlebarsTemplateEngine extends TemplateEngine {

  final io.vertx.ext.apex.templ.HandlebarsTemplateEngine delegate;

  public HandlebarsTemplateEngine(io.vertx.ext.apex.templ.HandlebarsTemplateEngine delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  public static HandlebarsTemplateEngine create() {
    HandlebarsTemplateEngine ret= HandlebarsTemplateEngine.newInstance(io.vertx.ext.apex.templ.HandlebarsTemplateEngine.create());
    return ret;
  }

  /**
   * Create a template engine
   *
   * @param resourcePrefix  the resource prefix
   * @param extension  the extension
   * @return  the engine
   */
  public static HandlebarsTemplateEngine create(String resourcePrefix, String extension) {
    HandlebarsTemplateEngine ret= HandlebarsTemplateEngine.newInstance(io.vertx.ext.apex.templ.HandlebarsTemplateEngine.create(resourcePrefix, extension));
    return ret;
  }

  /**
   * Create a template engine
   *
   * @param resourcePrefix  the resource prefix
   * @param extension  the extension
   * @param maxCacheSize  the max cache size
   * @return  the engine
   */
  public static HandlebarsTemplateEngine create(String resourcePrefix, String extension, int maxCacheSize) {
    HandlebarsTemplateEngine ret= HandlebarsTemplateEngine.newInstance(io.vertx.ext.apex.templ.HandlebarsTemplateEngine.create(resourcePrefix, extension, maxCacheSize));
    return ret;
  }


  public static HandlebarsTemplateEngine newInstance(io.vertx.ext.apex.templ.HandlebarsTemplateEngine arg) {
    return new HandlebarsTemplateEngine(arg);
  }
}
