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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.PetStoreService;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;

import static io.vertx.core.Future.succeededFuture;
import static io.vertx.ext.web.api.service.PetStoreService.buildPet;
import static io.vertx.ext.web.api.service.ServiceResponse.completedWithJson;

public class DummyPetStoreServiceImpl implements PetStoreService {

  @Override
  public Future<ServiceResponse> listPets(Integer limit, ServiceRequest context) {
    JsonArray respBody = new JsonArray().add(buildPet(1, "foo"));
    return succeededFuture(completedWithJson(respBody));
  }

  @Override
  public Future<ServiceResponse> createPets(JsonObject body, ServiceRequest context) {
    return succeededFuture(new ServiceResponse().setStatusCode(201));
  }

  @Override
  public Future<ServiceResponse> getPetById(String petId, ServiceRequest context) {
    return succeededFuture(completedWithJson(buildPet(1, "foo")));
  }
}
