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

package io.vertx.ext.web.openapi.router;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.openapi.contract.Operation;
import io.vertx.openapi.validation.ValidatableRequest;

@VertxGen
public interface RequestExtractor {

  /**
   * Extracts and transforms the parameters and the body of an incoming request into a {@link ValidatableRequest format}
   * that can be validated by the {@link io.vertx.openapi.validation.RequestValidator}.
   *
   * @param routingContext The routing context of the incoming request.
   * @param operation      The operation of the related request.
   * @return A {@link Future} holding the {@link ValidatableRequest}.
   */
  Future<ValidatableRequest> extractValidatableRequest(RoutingContext routingContext, Operation operation);
}
