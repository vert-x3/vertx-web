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

package io.vertx.ext.apex.addons.impl;

import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.addons.Timeout;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 */
public class TimeoutImpl implements Timeout {

  private final long timeout;

  public TimeoutImpl(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public void handle(RoutingContext ctx) {

    // We send a 408 response after timeout
    long tid = ctx.vertx().setTimer(timeout, t -> ctx.fail(408));

    ctx.response().bodyEndHandler(v -> ctx.vertx().cancelTimer(tid));

    ctx.next();
  }
}
