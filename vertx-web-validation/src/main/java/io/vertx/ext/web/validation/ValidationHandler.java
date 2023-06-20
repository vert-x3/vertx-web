package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@VertxGen
public interface ValidationHandler extends Handler<RoutingContext> {
  String REQUEST_CONTEXT_KEY = "requestParameters";
}
