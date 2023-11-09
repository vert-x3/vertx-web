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

package examples;

import io.vertx.core.Vertx;
import io.vertx.docgen.Source;
import io.vertx.ext.web.api.service.OpenAPIRouterHandler;
import io.vertx.ext.web.openapi.router.OpenAPIRoute;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.contract.OpenAPIContract;
import io.vertx.openapi.validation.ResponseValidator;

@Source
public class OpenAPIExamples {

  public void addHandler(RouterBuilder routerBuilder, Vertx vertx) {
    OpenAPIContract contract = getContract();
    ResponseValidator responseValidator = ResponseValidator.create(vertx, contract);

    OpenAPIRoute route = routerBuilder.getRoute("getPets");
    OpenAPIRouterHandler handler = OpenAPIRouterHandler.create(vertx,
      route.getOperation(), responseValidator);

    route.addHandler(handler);
  }

  private OpenAPIContract getContract() {
    return null;
  }
}
