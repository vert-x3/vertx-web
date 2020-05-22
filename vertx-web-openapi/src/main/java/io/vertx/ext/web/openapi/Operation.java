package io.vertx.ext.web.openapi;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Interface representing an <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#operationObject">Operation</a>
 */
@VertxGen
public interface Operation {

  /**
   * Mount an handler for this operation
   *
   * @param handler
   * @return
   */
  @Fluent Operation handler(Handler<RoutingContext> handler);

  /**
   * Mount a failure handler for this operation
   *
   * @param handler
   * @return
   */
  @Fluent Operation failureHandler(Handler<RoutingContext> handler);

  /**
   * Route an incoming request to this operation to a Web API Service
   *
   * @param address
   * @return
   */
  @Fluent
  Operation routeToEventBus(String address);

  /**
   * Route an incoming request to this operation to a Web API Service
   *
   * @param address
   * @param options
   * @return
   */
  @Fluent
  Operation routeToEventBus(String address, DeliveryOptions options);

  /**
   * @return operationId of this operation
   */
  String getOperationId();

  /**
   * @return model of this operation
   */
  JsonObject getOperationModel();

  /**
   * @return http method of this operation
   */
  HttpMethod getHttpMethod();

  /**
   * @return path in OpenAPI style
   */
  String getOpenAPIPath();

}
