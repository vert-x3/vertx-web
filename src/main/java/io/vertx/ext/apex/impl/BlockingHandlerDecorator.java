package io.vertx.ext.apex.impl;

import io.vertx.core.Handler;
import io.vertx.ext.apex.Route;
import io.vertx.ext.apex.RoutingContext;

import java.util.Objects;

/**
 * Wraps a handler that would normally block and turn it into a non-blocking handler.
 * This is done by calling {@link io.vertx.core.Vertx#executeBlocking(Handler, Handler)} 
 * and wrapping the context to overload {@link io.vertx.ext.apex.RoutingContext#next()} so that
 * the next handler is run on the original event loop
 * 
 * @author <a href="mailto:stephane.bastian.dev@gmail.com>Stéphane Bastian</a>
 *
 */
public class BlockingHandlerDecorator implements Handler<RoutingContext> {

  private Handler<RoutingContext> decoratedHandler;
  
  public BlockingHandlerDecorator(Handler<RoutingContext> decoratedHandler) {
    Objects.requireNonNull(decoratedHandler);
    this.decoratedHandler = decoratedHandler;
  }
  
  @Override
  public void handle(RoutingContext context) {
    Route currentRoute = context.currentRoute();
    context.vertx().executeBlocking(fut -> {
      decoratedHandler.handle(new RoutingContextDecorator(context, currentRoute));
      fut.complete();
    }, null);
  }

}
