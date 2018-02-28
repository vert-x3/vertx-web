package io.vertx.ext.web.handler.impl.logger;

import io.vertx.ext.web.RoutingContext;

public interface Parameter {

  StringBuilder print(RoutingContext context);
}
