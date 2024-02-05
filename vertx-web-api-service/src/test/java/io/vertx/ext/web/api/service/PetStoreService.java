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

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

@WebApiServiceGen
public interface PetStoreService {
  static JsonObject buildPet(int id, String name) {
    return new JsonObject().put("id", id).put("name", name);
  }

  Future<ServiceResponse> listPets(Integer limit, ServiceRequest context);

  Future<ServiceResponse> createPets(JsonObject body, ServiceRequest context);

  Future<ServiceResponse> getPetById(String petId, ServiceRequest context);
}
