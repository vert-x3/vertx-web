package io.vertx.ext.web.openapi.router;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

@VertxGen
public interface RouterFactory
{
  Router createRouter(Vertx vertx);
}
