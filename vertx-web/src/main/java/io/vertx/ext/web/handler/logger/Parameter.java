package io.vertx.ext.web.handler.logger;

import io.vertx.ext.web.RoutingContext;

interface Parameter {

  StringBuilder print(RoutingContext context, boolean immediate);
}
