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

package io.vertx.groovy.ext.apex.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.RoutingContext
import io.vertx.core.Handler
/**
 * A pretty error handler for rendering error pages.
*/
@CompileStatic
public class ErrorHandler implements Handler<RoutingContext> {
  final def io.vertx.ext.apex.handler.ErrorHandler delegate;
  public ErrorHandler(io.vertx.ext.apex.handler.ErrorHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.apex.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create an error handler using defaults
   * @return the handler
   */
  public static ErrorHandler create() {
    def ret= ErrorHandler.FACTORY.apply(io.vertx.ext.apex.handler.ErrorHandler.create());
    return ret;
  }
  /**
   * Create an error handler
   * @param errorTemplateName the error template name - will be looked up from the classpath
   * @param displayExceptionDetails true if exception details should be displayed
   * @return the handler
   */
  public static ErrorHandler create(String errorTemplateName, boolean displayExceptionDetails) {
    def ret= ErrorHandler.FACTORY.apply(io.vertx.ext.apex.handler.ErrorHandler.create(errorTemplateName, displayExceptionDetails));
    return ret;
  }
  /**
   * Create an error handler
   * @param displayExceptionDetails true if exception details should be displayed
   * @return the handler
   */
  public static ErrorHandler create(boolean displayExceptionDetails) {
    def ret= ErrorHandler.FACTORY.apply(io.vertx.ext.apex.handler.ErrorHandler.create(displayExceptionDetails));
    return ret;
  }
  /**
   * Create an error handler
   * @param errorTemplateName the error template name - will be looked up from the classpath
   * @return the handler
   */
  public static ErrorHandler create(String errorTemplateName) {
    def ret= ErrorHandler.FACTORY.apply(io.vertx.ext.apex.handler.ErrorHandler.create(errorTemplateName));
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.handler.ErrorHandler, ErrorHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.handler.ErrorHandler arg -> new ErrorHandler(arg);
  };
}
