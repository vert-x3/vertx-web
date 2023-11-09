/*
 * Copyright (c) 2023, SAP SE
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.ext.web.api.service;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.service.impl.OpenAPIRouterHandlerImpl;
import io.vertx.openapi.contract.Operation;
import io.vertx.openapi.validation.ResponseValidator;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public interface OpenAPIRouterHandler extends Handler<RoutingContext> {
  String OPENAPI_EXTENSION = "x-vertx-event-bus";
  String OPENAPI_EXTENSION_ADDRESS = "address";
  String OPENAPI_EXTENSION_METHOD_NAME = "method";

  /**
   * Creates a new OpenAPIRouterHandler to forward requests from OpenAPI Router to Web API Service Proxies. In order to
   * automatically parse the destination of the related Web API Service Proxy, the OpenAPI Operation must be extended
   * with the "x-vertx-event-bus" property.
   * <pre>
   * {
   *   ...
   *   "operationId": "myOperation",
   *   "x-vertx-event-bus" : "myEventbusAddress"
   *   ...
   * }
   * </pre>
   *
   * <pre>
   * {
   *  ...
   *  "operationId": "myOperation",
   *  "x-vertx-event-bus" : {
   *    "address" : "myEventbusAddress"
   *  }
   *  ...
   *  }
   * </pre>
   * <pre>
   * {
   *  ...
   *  "operationId": "myOperation",
   *  "x-vertx-event-bus" : {
   *    "address" : "myEventbusAddress",
   *    "method" : "myMethod"
   *  }
   *  ...
   *  }
   * </pre>
   *
   * @param vertx     The related Vert.x instance to access the Eventbus
   * @param operation The related OpenAPI operation with the "x-vertx-event-bus" extension
   * @param validator The ResponseValidator to automatically validate the response received by the Web API Service Proxy
   * @return a new OpenAPIRouterHandler
   */
  static OpenAPIRouterHandler create(Vertx vertx, Operation operation, ResponseValidator validator) {
    return create(vertx, operation, new DeliveryOptions(), (o, r) -> new JsonObject(), validator);
  }

  /**
   * Like {@link #create(Vertx, Operation, ResponseValidator)}.
   *
   * @param vertx              The related Vert.x instance to access the Eventbus
   * @param operation          The related OpenAPI operation with the "x-vertx-event-bus" extension
   * @param validator          The ResponseValidator to automatically validate the response received by the Web API
   *                           Service Proxy
   * @param deliveryOptions    The {@link DeliveryOptions} which will be used by this handler for every eventbus
   *                           request.
   * @param extraPayloadMapper A function to extract an extra payload from incoming requests.
   * @return a new OpenAPIRouterHandler
   */
  static OpenAPIRouterHandler create(Vertx vertx, Operation operation, DeliveryOptions deliveryOptions,
                                     BiFunction<Operation, RoutingContext, JsonObject> extraPayloadMapper,
                                     ResponseValidator validator) {
    Object ebExtension = operation.getExtensions().get(OPENAPI_EXTENSION);
    Objects.requireNonNull(ebExtension, "No eventbus configuration found for Operation: " + operation.getOperationId());

    RuntimeException invalidConfig =
      new RuntimeException("Invalid eventbus configuration found for Operation: " + operation.getOperationId());

    String address;
    String method = operation.getOperationId();

    if (ebExtension instanceof String) {
      address = (String) ebExtension;
    } else if (ebExtension instanceof JsonObject) {
      Object addressObject = ((JsonObject) ebExtension).getValue(OPENAPI_EXTENSION_ADDRESS);
      if (addressObject instanceof String) {
        address = (String) addressObject;
      } else {
        throw invalidConfig;
      }
      Object methodObject = ((JsonObject) ebExtension).getValue(OPENAPI_EXTENSION_METHOD_NAME);
      if (methodObject != null) {
        if (methodObject instanceof String) {
          method = (String) methodObject;
        } else {
          throw invalidConfig;
        }
      }
    } else {
      throw invalidConfig;
    }

    DeliveryOptions delOpts = Optional.ofNullable(deliveryOptions).orElse(new DeliveryOptions());
    delOpts.addHeader("action", method);
    return new OpenAPIRouterHandlerImpl(vertx.eventBus(), address, delOpts, extraPayloadMapper, validator);
  }
}
