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

package io.vertx.groovy.ext.web.templ;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
/**
 * A template engine that uses Jade.
*/
@CompileStatic
public class JadeTemplateEngine extends TemplateEngine {
  private final def io.vertx.ext.web.templ.JadeTemplateEngine delegate;
  public JadeTemplateEngine(Object delegate) {
    super((io.vertx.ext.web.templ.JadeTemplateEngine) delegate);
    this.delegate = (io.vertx.ext.web.templ.JadeTemplateEngine) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a template engine using defaults
   * @return the engine
   */
  public static JadeTemplateEngine create() {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.templ.JadeTemplateEngine.create(), io.vertx.groovy.ext.web.templ.JadeTemplateEngine.class);
    return ret;
  }
  /**
   * Set the extension for the engine
   * @param extension the extension
   * @return a reference to this for fluency
   */
  public JadeTemplateEngine setExtension(String extension) {
    def ret = InternalHelper.safeCreate(delegate.setExtension(extension), io.vertx.groovy.ext.web.templ.JadeTemplateEngine.class);
    return ret;
  }
  /**
   * Set the max cache size for the engine
   * @param maxCacheSize the maxCacheSize
   * @return a reference to this for fluency
   */
  public JadeTemplateEngine setMaxCacheSize(int maxCacheSize) {
    def ret = InternalHelper.safeCreate(delegate.setMaxCacheSize(maxCacheSize), io.vertx.groovy.ext.web.templ.JadeTemplateEngine.class);
    return ret;
  }
}
