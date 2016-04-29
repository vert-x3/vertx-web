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

package io.vertx.rxjava.ext.web.handler;

import java.util.Map;
import rx.Observable;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.core.Handler;

/**
 * A pretty error handler for rendering error pages.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.ErrorHandler original} non RX-ified interface using Vert.x codegen.
 */

public class ErrorHandler implements Handler<RoutingContext> {

  final io.vertx.ext.web.handler.ErrorHandler delegate;

  public ErrorHandler(io.vertx.ext.web.handler.ErrorHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void handle(RoutingContext arg0) { 
    delegate.handle((io.vertx.ext.web.RoutingContext)arg0.getDelegate());
  }

  /**
   * Create an error handler using defaults
   * @return the handler
   */
  public static ErrorHandler create() { 
    ErrorHandler ret = ErrorHandler.newInstance(io.vertx.ext.web.handler.ErrorHandler.create());
    return ret;
  }

  /**
   * Create an error handler
   * @param errorTemplateName the error template name - will be looked up from the classpath
   * @param displayExceptionDetails true if exception details should be displayed
   * @return the handler
   */
  public static ErrorHandler create(String errorTemplateName, boolean displayExceptionDetails) { 
    ErrorHandler ret = ErrorHandler.newInstance(io.vertx.ext.web.handler.ErrorHandler.create(errorTemplateName, displayExceptionDetails));
    return ret;
  }

  /**
   * Create an error handler
   * @param displayExceptionDetails true if exception details should be displayed
   * @return the handler
   */
  public static ErrorHandler create(boolean displayExceptionDetails) { 
    ErrorHandler ret = ErrorHandler.newInstance(io.vertx.ext.web.handler.ErrorHandler.create(displayExceptionDetails));
    return ret;
  }

  /**
   * Create an error handler
   * @param errorTemplateName the error template name - will be looked up from the classpath
   * @return the handler
   */
  public static ErrorHandler create(String errorTemplateName) { 
    ErrorHandler ret = ErrorHandler.newInstance(io.vertx.ext.web.handler.ErrorHandler.create(errorTemplateName));
    return ret;
  }


  public static ErrorHandler newInstance(io.vertx.ext.web.handler.ErrorHandler arg) {
    return arg != null ? new ErrorHandler(arg) : null;
  }
}
