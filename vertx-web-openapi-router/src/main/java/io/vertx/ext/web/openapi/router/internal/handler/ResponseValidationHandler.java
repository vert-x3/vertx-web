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
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.openapi.contract.Operation;
import io.vertx.openapi.validation.ResponseValidator;
import io.vertx.openapi.validation.ValidatableResponse;
import io.vertx.openapi.validation.ValidatedRequest;
import io.vertx.openapi.validation.ValidatorException;

import static io.vertx.ext.web.openapi.router.RouterBuilder.KEY_META_DATA_OPERATION;
import static io.vertx.ext.web.openapi.router.RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST;

public abstract class ResponseValidationHandler implements Handler<RoutingContext> {
  private final ResponseValidator responseValidator;

  protected ResponseValidationHandler(ResponseValidator responseValidator) {
    this.responseValidator = responseValidator;
  }

  @Override
  public void handle(RoutingContext routingContext) {
    ValidatedRequest validatedRequest = routingContext.get(KEY_META_DATA_VALIDATED_REQUEST);
    Operation operation = routingContext.currentRoute().getMetadata(KEY_META_DATA_OPERATION);

    processRequest(validatedRequest, operation, routingContext)
      .compose(validatableResponse -> responseValidator.validate(validatableResponse, operation.getOperationId()))
      .compose(validatedResponse -> validatedResponse.send(routingContext.response()))
      .onFailure(e -> {
        if (e instanceof ValidatorException) {
          handleValidatorException((ValidatorException) e, routingContext);
        } else {
          routingContext.fail(e);
        }
      });
  }

  abstract Future<ValidatableResponse> processRequest(ValidatedRequest request, Operation operation,
                                                      RoutingContext routingContext);

  protected void handleValidatorException(ValidatorException ve, RoutingContext rtx) {
    rtx.fail(ve);
  }
}
