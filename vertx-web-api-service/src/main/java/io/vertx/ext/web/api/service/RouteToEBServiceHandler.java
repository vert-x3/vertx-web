package io.vertx.ext.web.api.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.service.impl.RouteToEBServiceHandlerImpl;

import java.util.function.Function;

/**
 * Handler that proxy the request to an event bus endpoint, waits for the reply and then writes the HTTP response. <br/>
 *
 * The HTTP request is sent encapsulated into a {@link ServiceRequest} object through the event bus. The expected reply is a {@link ServiceResponse}, encapsulated as a {@link JsonObject}<br/>
 *
 * This handler requires a {@link io.vertx.ext.web.validation.ValidationHandler} that process request parameters, so they can be encapsulated by this handler inside the {@link ServiceRequest}
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface RouteToEBServiceHandler extends Handler<RoutingContext> {

  /**
   * When {@code extraPayloadMapper} is configured, this handler puts the evaluation result into {@link ServiceRequest#getExtra()}
   *
   * @param extraPayloadMapper mapper
   * @return
   */
  @Fluent
  RouteToEBServiceHandler extraPayloadMapper(Function<RoutingContext, JsonObject> extraPayloadMapper);

  /**
   * Build a new {@code RouteToEBServiceHandler}
   *
   * @param eventBus Vert.x event bus instance
   * @param address Event bus endpoint address
   * @param actionName action name of the endpoint. This will be configured as {@link DeliveryOptions} header named {@code action}
   * @return
   */
  static RouteToEBServiceHandler build(EventBus eventBus, String address, String actionName) {
    return new RouteToEBServiceHandlerImpl(eventBus, address, new DeliveryOptions().addHeader("action", actionName));
  }

  /**
   * Build a new {@code RouteToEBServiceHandler}
   *
   * @param eventBus Vert.x event bus instance
   * @param address Event bus endpoint address
   * @param actionName action name of the endpoint. This will be configured as {@link DeliveryOptions} header named {@code action}
   * @param deliveryOptions delivery options that will be always sent with the request
   * @return
   */
  static RouteToEBServiceHandler build(EventBus eventBus, String address, String actionName, DeliveryOptions deliveryOptions) {
    return new RouteToEBServiceHandlerImpl(eventBus, address, new DeliveryOptions(deliveryOptions).addHeader("action", actionName));
  }
}
