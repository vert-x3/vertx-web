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
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
/**
 * A pretty error handler for rendering error pages.
*/
@CompileStatic
public class ErrorHandler implements Handler<RoutingContext> {
  private final def io.vertx.ext.web.handler.ErrorHandler delegate;
  public ErrorHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.ErrorHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) delegate).handle(arg0 != null ? (io.vertx.ext.web.RoutingContext)arg0.getDelegate() : null);
  }
  /**
   * Create an error handler using defaults
   * @return the handler
   */
  public static ErrorHandler create() {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.ErrorHandler.create(), io.vertx.groovy.ext.web.handler.ErrorHandler.class);
    return ret;
  }
  /**
   * Create an error handler
   * @param errorTemplateName the error template name - will be looked up from the classpath
   * @param displayExceptionDetails true if exception details should be displayed
   * @return the handler
   */
  public static ErrorHandler create(String errorTemplateName, boolean displayExceptionDetails) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.ErrorHandler.create(errorTemplateName, displayExceptionDetails), io.vertx.groovy.ext.web.handler.ErrorHandler.class);
    return ret;
  }
  /**
   * Create an error handler
   * @param displayExceptionDetails true if exception details should be displayed
   * @return the handler
   */
  public static ErrorHandler create(boolean displayExceptionDetails) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.ErrorHandler.create(displayExceptionDetails), io.vertx.groovy.ext.web.handler.ErrorHandler.class);
    return ret;
  }
  /**
   * Create an error handler
   * @param errorTemplateName the error template name - will be looked up from the classpath
   * @return the handler
   */
  public static ErrorHandler create(String errorTemplateName) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.ErrorHandler.create(errorTemplateName), io.vertx.groovy.ext.web.handler.ErrorHandler.class);
    return ret;
  }
}
