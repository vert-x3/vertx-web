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

package io.vertx.ext.web.openapi.router.internal.handler;

import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.openapi.contract.Operation;
import io.vertx.openapi.validation.ResponseValidator;
import io.vertx.openapi.validation.ValidatableResponse;
import io.vertx.openapi.validation.ValidatedRequest;

public abstract class EventbusHandler extends ResponseValidationHandler {
  private final EventBus eventBus;
  private final String address;
  private final DeliveryOptions deliveryOptions;

  protected EventbusHandler(EventBus eventBus, String address, DeliveryOptions deliveryOptions,
                            ResponseValidator validator) {
    super(validator);
    this.eventBus = eventBus;
    this.address = address;
    this.deliveryOptions = deliveryOptions;
  }

  @Override
  Future<ValidatableResponse> processRequest(ValidatedRequest request, Operation operation,
                                             RoutingContext routingContext) {
    return transformRequest(request, routingContext, operation).
      compose(payload -> eventBus.<JsonObject>request(address, payload, deliveryOptions)).
      compose(ebResponse -> transformResponse(ebResponse, operation));
  }

  protected abstract Future<JsonObject> transformRequest(ValidatedRequest request, RoutingContext routingContext,
                                                         Operation operation);

  protected abstract Future<ValidatableResponse> transformResponse(Message<JsonObject> ebResponse, Operation operation);
}
