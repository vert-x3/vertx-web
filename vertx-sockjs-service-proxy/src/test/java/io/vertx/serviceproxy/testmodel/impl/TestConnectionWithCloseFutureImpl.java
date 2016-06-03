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

package io.vertx.serviceproxy.testmodel.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.testmodel.TestConnectionWithCloseFuture;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TestConnectionWithCloseFutureImpl implements TestConnectionWithCloseFuture {

  private Vertx vertx;

  public TestConnectionWithCloseFutureImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    vertx.eventBus().send("closeCalled", "blah");
    handler.handle(Future.succeededFuture());
  }

  @Override
  public void someMethod(Handler<AsyncResult<String>> resultHandler) {
    resultHandler.handle(Future.succeededFuture("the_result"));
  }
}
