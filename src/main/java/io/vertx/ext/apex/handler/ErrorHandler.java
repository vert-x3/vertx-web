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

package io.vertx.ext.apex.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.handler.impl.ErrorHandlerImpl;
import io.vertx.ext.apex.RoutingContext;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface ErrorHandler extends Handler<RoutingContext> {

  public static final String DEFAULT_ERROR_HANDLER_TEMPLATE = "apex-error.html";
  public static final boolean DEFAULT_DISPLAY_EXCEPTION_DETAILS = true;

  static ErrorHandler create() {
    return new ErrorHandlerImpl(DEFAULT_ERROR_HANDLER_TEMPLATE, DEFAULT_DISPLAY_EXCEPTION_DETAILS);
  }

  static ErrorHandler create(String errorTemplateName, boolean displayExceptionDetails) {
    return new ErrorHandlerImpl(errorTemplateName, displayExceptionDetails);
  }

  static ErrorHandler create(boolean displayExceptionDetails) {
    return new ErrorHandlerImpl(DEFAULT_ERROR_HANDLER_TEMPLATE, displayExceptionDetails);
  }

  static ErrorHandler create(String errorTemplateName) {
    return new ErrorHandlerImpl(errorTemplateName, DEFAULT_DISPLAY_EXCEPTION_DETAILS);
  }

  @Override
  void handle(RoutingContext context);

}
