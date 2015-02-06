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
import io.vertx.ext.apex.handler.impl.LoggerHandlerImpl;
import io.vertx.ext.apex.RoutingContext;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
@VertxGen
public interface LoggerHandler extends Handler<RoutingContext> {

  static LoggerHandler create() {
    return new LoggerHandlerImpl();
  }

  static LoggerHandler create(Format format) {
    return new LoggerHandlerImpl(format);
  }

  static LoggerHandler create(boolean immediate, Format format) {
    return new LoggerHandlerImpl(immediate, format);
  }

  /**
   * The possible out of the box formats.
   */
  public enum Format {
    DEFAULT,
    SHORT,
    TINY
  }

  @Override
  void handle(RoutingContext event);
}
