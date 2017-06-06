package io.vertx.ext.web.designdriven;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Author: Francesco Guardiani @slinkydeveloper
 */
@VertxGen(concrete = false)
public interface DesignDrivenRouterFactory {

  @Fluent
  DesignDrivenRouterFactory addSecurityHandler(String securitySchemaName, Handler<RoutingContext> handler);

  @Fluent
  DesignDrivenRouterFactory addHandler(HttpMethod method, String path, Handler<RoutingContext> handler, Handler<RoutingContext> failureHandler);

  @Fluent
  DesignDrivenRouterFactory setValidationFailureHandler(Handler<RoutingContext> handler);

  @Fluent
  DesignDrivenRouterFactory enableValidationFailureHandler(boolean enable);

  Router getRouter();

}
