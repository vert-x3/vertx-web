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

package io.vertx.groovy.ext.web.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.ext.web.templ.TemplateEngine
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
/**
 *
 * A handler which renders responses using a template engine and where the template name is selected from the URI
 * path.
*/
@CompileStatic
public class TemplateHandler implements Handler<RoutingContext> {
  private final def io.vertx.ext.web.handler.TemplateHandler delegate;
  public TemplateHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.TemplateHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) delegate).handle(arg0 != null ? (io.vertx.ext.web.RoutingContext)arg0.getDelegate() : null);
  }
  /**
   * Create a handler
   * @param engine the template engine
   * @return the handler
   */
  public static TemplateHandler create(TemplateEngine engine) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.TemplateHandler.create(engine != null ? (io.vertx.ext.web.templ.TemplateEngine)engine.getDelegate() : null), io.vertx.groovy.ext.web.handler.TemplateHandler.class);
    return ret;
  }
  /**
   * Create a handler
   * @param engine the template engine
   * @param templateDirectory the template directory where templates will be looked for
   * @param contentType the content type header to be used in the response
   * @return the handler
   */
  public static TemplateHandler create(TemplateEngine engine, String templateDirectory, String contentType) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.TemplateHandler.create(engine != null ? (io.vertx.ext.web.templ.TemplateEngine)engine.getDelegate() : null, templateDirectory, contentType), io.vertx.groovy.ext.web.handler.TemplateHandler.class);
    return ret;
  }
}
