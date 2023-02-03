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

package io.vertx.ext.web.openapi.router.impl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.router.OpenAPIRoute;
import io.vertx.openapi.contract.Operation;

import java.util.ArrayList;
import java.util.List;

public class OpenAPIRouteImpl implements OpenAPIRoute {
  private final List<Handler<RoutingContext>> handlers = new ArrayList<>();
  private final List<Handler<RoutingContext>> failureHandlers = new ArrayList<>();

  private final Operation operation;

  private boolean doValidation = true;

  public OpenAPIRouteImpl(Operation operation) {
    this.operation = operation;
  }

  @Override
  public OpenAPIRoute addHandler(Handler<RoutingContext> handler) {
    handlers.add(handler);
    return this;
  }

  @Override
  public List<Handler<RoutingContext>> getHandlers() {
    return handlers;
  }

  @Override
  public OpenAPIRoute addFailureHandler(Handler<RoutingContext> handler) {
    failureHandlers.add(handler);
    return this;
  }

  @Override
  public List<Handler<RoutingContext>> getFailureHandlers() {
    return failureHandlers;
  }

  @Override
  public Operation getOperation() {
    return operation;
  }

  @Override
  public boolean doValidation() {
    return doValidation;
  }

  @Override
  public OpenAPIRoute setDoValidation(boolean doValidation) {
    this.doValidation = doValidation;
    return this;
  }
}
