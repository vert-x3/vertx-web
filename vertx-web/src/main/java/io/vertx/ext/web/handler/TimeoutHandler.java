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
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.TimeoutHandlerImpl;

/**
 * Handler that will timeout requests if the response has not been written after a certain time.
 * Timeout requests will be ended with an HTTP status code `503`.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface TimeoutHandler extends Handler<RoutingContext> {

  /**
   * The default timeout, in ms
   */
  long DEFAULT_TIMEOUT = 5000;

  /**
   * The default error code
   */
  int DEFAULT_ERRORCODE = 503;

  /**
   * Create a handler
   *
   * @return the handler
   */
  static TimeoutHandler create() {
    return new TimeoutHandlerImpl(DEFAULT_TIMEOUT, DEFAULT_ERRORCODE);
  }

  /**
   * Create a handler
   *
   * @param timeout  the timeout, in ms
   * @return the handler
   */
  static TimeoutHandler create(long timeout) {
    return new TimeoutHandlerImpl(timeout, DEFAULT_ERRORCODE);
  }

  /**
   * Create a handler
   *
   * @param timeout  the timeout, in ms
   * @return the handler
   */
  static TimeoutHandler create(long timeout, int errorCode) {
    return new TimeoutHandlerImpl(timeout, errorCode);
  }
}
