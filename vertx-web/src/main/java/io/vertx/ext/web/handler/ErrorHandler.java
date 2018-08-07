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

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.handler.impl.ErrorHandlerImpl;
import io.vertx.ext.web.RoutingContext;

/**
 * A pretty error handler for rendering error pages.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface ErrorHandler extends Handler<RoutingContext> {

  /**
   * The default template to use for rendering
   */
  String DEFAULT_ERROR_HANDLER_TEMPLATE = "META-INF/vertx/web/vertx-web-error.html";

  /**
   * Should exception details be displayed by default?
   */
  boolean DEFAULT_DISPLAY_EXCEPTION_DETAILS = true;

  /**
   * Create an error handler using defaults
   *
   * @return the handler
   */
  static ErrorHandler create() {
    return new ErrorHandlerImpl(DEFAULT_ERROR_HANDLER_TEMPLATE, DEFAULT_DISPLAY_EXCEPTION_DETAILS);
  }

  /**
   * Create an error handler
   *
   * @param errorTemplateName  the error template name - will be looked up from the classpath
   * @param displayExceptionDetails  true if exception details should be displayed
   * @return the handler
   */
  static ErrorHandler create(String errorTemplateName, boolean displayExceptionDetails) {
    return new ErrorHandlerImpl(errorTemplateName, displayExceptionDetails);
  }

  /**
   * Create an error handler
   *
   * @param displayExceptionDetails  true if exception details should be displayed
   * @return the handler
   */
  static ErrorHandler create(boolean displayExceptionDetails) {
    return new ErrorHandlerImpl(DEFAULT_ERROR_HANDLER_TEMPLATE, displayExceptionDetails);
  }

  /**
   * Create an error handler
   *
   * @param errorTemplateName  the error template name - will be looked up from the classpath
   * @return the handler
   */
  static ErrorHandler create(String errorTemplateName) {
    return new ErrorHandlerImpl(errorTemplateName, DEFAULT_DISPLAY_EXCEPTION_DETAILS);
  }

}
