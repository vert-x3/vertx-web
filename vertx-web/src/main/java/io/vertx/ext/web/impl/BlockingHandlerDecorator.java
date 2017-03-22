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
package io.vertx.ext.web.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

/**
 * Wraps a handler that would normally block and turn it into a non-blocking handler.
 * This is done by calling {@link io.vertx.core.Vertx#executeBlocking(Handler, Handler)}
 * and wrapping the context to overload {@link RoutingContext#next()} so that
 * the next handler is run on the original event loop
 *
 * @author <a href="mailto:stephane.bastian.dev@gmail.com>Stéphane Bastian</a>
 *
 */
public class BlockingHandlerDecorator implements Handler<RoutingContext> {

  private final Handler<RoutingContext> decoratedHandler;
  private final WorkerExecutor workerExecutor;
  private final boolean ordered;

  public BlockingHandlerDecorator(Handler<RoutingContext> decoratedHandler, boolean ordered) {
    Objects.requireNonNull(decoratedHandler, "decoratedHandler");
    this.decoratedHandler = decoratedHandler;
    this.ordered = ordered;
    workerExecutor = null;
  }

  public BlockingHandlerDecorator(Handler<RoutingContext> decoratedHandler, WorkerExecutor workerExecutor, boolean ordered) {
    Objects.requireNonNull(decoratedHandler, "decoratedHandler");
    Objects.requireNonNull(workerExecutor, "workerExecutor");
    this.decoratedHandler = decoratedHandler;
    this.workerExecutor = workerExecutor;
    this.ordered = ordered;
  }

  @Override
  public void handle(RoutingContext context) {
    Route currentRoute = context.currentRoute();
    if (workerExecutor == null) {
      Vertx vertx = context.vertx();
      vertx.<Void>executeBlocking(fut -> invokeBlockingHandler(fut, context, currentRoute), ordered, res -> handleResult(context, res));
    } else {
      workerExecutor.<Void>executeBlocking(fut -> invokeBlockingHandler(fut, context, currentRoute), ordered, res -> handleResult(context, res));
    }
  }

  private void invokeBlockingHandler(Future<Void> fut, RoutingContext context, Route currentRoute) {
    decoratedHandler.handle(new RoutingContextDecorator(currentRoute, context));
    fut.complete();
  }

  private void handleResult(RoutingContext context, AsyncResult<Void> res) {
    if (res.failed()) {
      // This means an exception was thrown from the blocking handler
      context.fail(res.cause());
    }
  }
}
