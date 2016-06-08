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

package io.vertx.ext.web.handler.impl;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.TimeoutHandler;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 */
public class TimeoutHandlerImpl implements TimeoutHandler {

  private final long timeout;
  private final int errorCode;

  public TimeoutHandlerImpl(long timeout, int errorCode) {
    this.timeout = timeout;
    this.errorCode = errorCode;
  }

  @Override
  public void handle(RoutingContext ctx) {

    // We send a error response after timeout
    long tid = ctx.vertx().setTimer(timeout, t -> ctx.fail(errorCode));

    ctx.addBodyEndHandler(v -> ctx.vertx().cancelTimer(tid));

    ctx.next();
  }
}
