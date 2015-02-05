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
import io.vertx.core.Handler;
import io.vertx.ext.apex.addons.impl.ErrorHandlerImpl;
import io.vertx.ext.apex.core.RoutingContext;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface ErrorHandler extends Handler<RoutingContext> {

  public static final String DEFAULT_ERROR_HANDLER_TEMPLATE = "apex-error.html";
  public static final boolean DEFAULT_DISPLAY_EXCEPTION_DETAILS = true;

  static ErrorHandler errorHandler() {
    return new ErrorHandlerImpl(DEFAULT_ERROR_HANDLER_TEMPLATE, DEFAULT_DISPLAY_EXCEPTION_DETAILS);
  }

  static ErrorHandler errorHandler(String errorTemplateName, boolean displayExceptionDetails) {
    return new ErrorHandlerImpl(errorTemplateName, displayExceptionDetails);
  }

  static ErrorHandler errorHandler(boolean displayExceptionDetails) {
    return new ErrorHandlerImpl(DEFAULT_ERROR_HANDLER_TEMPLATE, displayExceptionDetails);
  }

  static ErrorHandler errorHandler(String errorTemplateName) {
    return new ErrorHandlerImpl(errorTemplateName, DEFAULT_DISPLAY_EXCEPTION_DETAILS);
  }

  @Override
  void handle(RoutingContext context);

}
