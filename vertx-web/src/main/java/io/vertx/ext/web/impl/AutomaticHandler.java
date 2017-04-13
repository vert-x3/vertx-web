package io.vertx.ext.web.impl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Function;

/**
 * @author <a href="mailto:y.lin@vanderbilt.edu">Yunyu Lin</a>
 */
public class AutomaticHandler {
  private Function<RoutingContext, AutomaticHandlerResult> resultFunction;
  private Handler<RoutingContext> contextHandler;

  public AutomaticHandler(Function<RoutingContext, AutomaticHandlerResult> resultFunction, Handler<RoutingContext> contextHandler) {
    this.resultFunction = resultFunction;
    this.contextHandler = contextHandler;
  }

  public AutomaticHandlerResult shouldHandle(RoutingContext context) {
    return resultFunction.apply(context);
  }

  public void handle(RoutingContext context) {
    contextHandler.handle(context);
  }
}
