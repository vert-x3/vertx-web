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

package io.vertx.groovy.ext.apex.templ;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
/**
 * A template engine that uses the Thymeleaf library.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class ThymeleafTemplateEngine extends TemplateEngine {
  final def io.vertx.ext.apex.templ.ThymeleafTemplateEngine delegate;
  public ThymeleafTemplateEngine(io.vertx.ext.apex.templ.ThymeleafTemplateEngine delegate) {
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
  public static ThymeleafTemplateEngine create() {
    def ret= ThymeleafTemplateEngine.FACTORY.apply(io.vertx.ext.apex.templ.ThymeleafTemplateEngine.create());
    return ret;
  }
  /**
   * Set the mode for the engine
   *
   * @param mode  the mode
   * @return a reference to this for fluency
   */
  public ThymeleafTemplateEngine setMode(String mode) {
    def ret= ThymeleafTemplateEngine.FACTORY.apply(this.delegate.setMode(mode));
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.templ.ThymeleafTemplateEngine, ThymeleafTemplateEngine> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.templ.ThymeleafTemplateEngine arg -> new ThymeleafTemplateEngine(arg);
  };
}
