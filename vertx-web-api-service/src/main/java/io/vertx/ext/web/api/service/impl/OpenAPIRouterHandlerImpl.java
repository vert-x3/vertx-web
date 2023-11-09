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

package io.vertx.ext.web.api.service.impl;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.service.OpenAPIRouterHandler;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.openapi.router.internal.handler.EventbusHandler;
import io.vertx.openapi.contract.Operation;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.openapi.validation.ResponseValidator;
import io.vertx.openapi.validation.ValidatableResponse;
import io.vertx.openapi.validation.ValidatedRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;
import static java.util.Collections.emptyMap;

public class OpenAPIRouterHandlerImpl extends EventbusHandler implements OpenAPIRouterHandler {
  private final BiFunction<Operation, RoutingContext, JsonObject> extraPayloadMapper;

  public OpenAPIRouterHandlerImpl(EventBus eventBus, String address, DeliveryOptions deliveryOptions,
                                  BiFunction<Operation, RoutingContext, JsonObject> extraPayloadMapper
    , ResponseValidator validator) {
    super(eventBus, address, deliveryOptions, validator);
    this.extraPayloadMapper = extraPayloadMapper;
  }

  @Override
  protected Future<JsonObject> transformRequest(ValidatedRequest request, RoutingContext routingContext,
                                                Operation operation) {
    JsonObject params = buildParametersObject(request);
    JsonObject userPrincipal = Optional.ofNullable(routingContext.user().get()).map(User::principal).orElse(null);

    ServiceRequest sr = new ServiceRequest(
      params,
      routingContext.request().headers(),
      userPrincipal,
      extraPayloadMapper.apply(operation, routingContext)
    );

    return succeededFuture(new JsonObject().put("context", sr.toJson()));
  }

  @Override
  protected Future<ValidatableResponse> transformResponse(Message<JsonObject> ebResponse, Operation operation) {
    ServiceResponse serviceResponse = new ServiceResponse(ebResponse.body());
    int statusCode = serviceResponse.getStatusCode();
    // String statusMessage = serviceResponse.getStatusMessage()
    // Can't set status message yet, but a client SHOULD ignore the reason-phrase content anyway.
    // see https://www.rfc-editor.org/rfc/rfc7230#section-3.1.2

    Map<String, String> headers = new HashMap<>();
    serviceResponse.getHeaders().forEach(headers::put);

    Buffer body = serviceResponse.getPayload();
    if (body == null) {
      return succeededFuture(ValidatableResponse.create(statusCode, headers));
    } else {
      String contentType = headers.get(HttpHeaders.CONTENT_TYPE.toString());
      if (contentType == null || contentType.isEmpty()) {
        String msg = "Content-Type header is required, when response contains a body.";
        return failedFuture(new IllegalArgumentException(msg));
      }

      return succeededFuture(ValidatableResponse.create(statusCode, headers, body, contentType));
    }
  }

  private JsonObject buildParametersObject(ValidatedRequest vr) {
    JsonObject params = new JsonObject();
    params.put("header", transformRequestParameters(vr.getHeaders()));
    params.put("cookie", transformRequestParameters(vr.getCookies()));
    params.put("query", transformRequestParameters(vr.getQuery()));
    params.put("path", transformRequestParameters(vr.getPathParameters()));
    params.put("body", vr.getBody().get());
    return params;
  }

  private Map<String, Object> transformRequestParameters(Map<String, RequestParameter> params) {
    if (params == null) {
      return emptyMap();
    }
    Map<String, Object> mapToReturn = new HashMap<>(params.size());
    params.forEach((k, v) -> mapToReturn.put(k, v.get()));
    return mapToReturn;
  }
}
