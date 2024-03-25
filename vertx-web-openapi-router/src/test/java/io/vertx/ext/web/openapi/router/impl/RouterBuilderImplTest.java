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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.openapi.contract.OpenAPIVersion;
import io.vertx.openapi.contract.Operation;
import io.vertx.openapi.contract.impl.OpenAPIContractImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.vertx.openapi.contract.OpenAPIVersion.V3_1;
import static io.vertx.router.ResourceHelper.TEST_RESOURCE_PATH;

class RouterBuilderImplTest {

  @Test
  void testToVertxWebPath() {
    String openAPIPathWithPathParams = "/pets/{petId}/friends/{friendId}";
    String expectedWithPathParams = "/pets/:petId/friends/:friendId";
    assertThat(RouterBuilderImpl.toVertxWebPath(openAPIPathWithPathParams)).isEqualTo(expectedWithPathParams);

    String openAPIPathWithoutPathParams = "/pets/friends";
    assertThat(RouterBuilderImpl.toVertxWebPath(openAPIPathWithoutPathParams)).isEqualTo(openAPIPathWithoutPathParams);

    String openAPIPathRoot = "/";
    assertThat(RouterBuilderImpl.toVertxWebPath(openAPIPathRoot)).isEqualTo(openAPIPathRoot);
  }

  @ParameterizedTest(name = "{index} should make operations of an OpenAPI ({0}) contract accessible")
  @ValueSource(strings = {"v3.0", "v3.1"})
  void testOperationAndOperations(String version) throws IOException {
    Path pathDereferencedContract = TEST_RESOURCE_PATH.resolve(version).resolve("petstore_dereferenced.json");
    JsonObject contract = Buffer.buffer(Files.readAllBytes(pathDereferencedContract)).toJsonObject();
    RouterBuilderImpl rb =
      new RouterBuilderImpl(null, new OpenAPIContractImpl(contract, OpenAPIVersion.fromContract(contract), null), null);
    assertThat(rb.getRoutes()).hasSize(3);

    Operation listPets = rb.getRoute("listPets").getOperation();
    Operation createPets = rb.getRoute("createPets").getOperation();
    Operation showPetById = rb.getRoute("showPetById").getOperation();

    assertThat(listPets).isNotSameInstanceAs(createPets);
    assertThat(listPets).isNotSameInstanceAs(showPetById);
    assertThat(createPets).isNotSameInstanceAs(showPetById);
  }

  @Test
  void testRootHandler() {
    JsonObject dummySpec = new JsonObject().put("io/vertx/openapi", "3.0.0");
    RouterBuilderImpl rb = new RouterBuilderImpl(null, new OpenAPIContractImpl(dummySpec, V3_1, null), null);
    assertThat(rb.rootHandlers).isEmpty();
    Handler<RoutingContext> dummyHandler = RoutingContext::next;
    rb.rootHandler(dummyHandler);
    assertThat(rb.rootHandlers).hasSize(1);
    assertThat(rb.rootHandlers.get(0)).isSameInstanceAs(dummyHandler);
  }
}
