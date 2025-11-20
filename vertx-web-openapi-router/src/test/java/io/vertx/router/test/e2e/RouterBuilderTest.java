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

package io.vertx.router.test.e2e;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.openapi.validation.ValidatedRequest;
import io.vertx.router.test.ResourceHelper;
import io.vertx.router.test.base.RouterBuilderTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.google.common.truth.Truth.assertThat;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static io.vertx.ext.web.openapi.router.RequestExtractor.withBodyHandler;

class RouterBuilderTest extends RouterBuilderTestBase {

  @ParameterizedTest(name = "{index} should load and mount all operations of an OpenAPI ({0}) contract")
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  @ValueSource(strings = {"v3.0", "v3.1"})
  void testRouter(String version, VertxTestContext testContext) {
    Checkpoint cpListPets = testContext.checkpoint(2);
    Checkpoint cpCreatePets = testContext.checkpoint(2);
    Checkpoint cpShowPetById = testContext.checkpoint(2);

    Function<Checkpoint, Handler<RoutingContext>> buildCheckpointHandler = cp -> rc -> {
      ValidatedRequest validatedRequest = rc.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);
      cp.flag();
      rc.response().send(Json.encode(validatedRequest)).onFailure(testContext::failNow);
    };

    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve(version).resolve("petstore.json");
    createServer(pathDereferencedContract, rb -> {
      rb.getRoute("listPets")
        .setDoSecurity(false)
        .addHandler(buildCheckpointHandler.apply(cpListPets));
      rb.getRoute("createPets")
        .setDoSecurity(false)
        .addHandler(buildCheckpointHandler.apply(cpCreatePets));
      rb.getRoute("showPetById")
        .setDoSecurity(false)
        .addHandler(buildCheckpointHandler.apply(cpShowPetById));
      return Future.succeededFuture(rb);
    }).compose(v -> createRequest(GET, "/v1/pets").addQueryParam("limit", "42").send())
      .onSuccess(response -> testContext.verify(() -> {
        JsonObject query = response.bodyAsJsonObject().getJsonObject("query");
        assertThat(query.getJsonObject("limit").getMap()).containsEntry("long", 42);
        cpListPets.flag();
      }))
      .compose(v -> {
        JsonObject bodyJson = new JsonObject().put("id", 1).put("name", "FooBar");
        return createRequest(POST, "/v1/pets").sendJsonObject(bodyJson).onSuccess(response -> testContext.verify(() -> {
          JsonObject body = response.bodyAsJsonObject().getJsonObject("body");
          JsonObject bodyValueAsJson = body.getJsonObject("jsonObject");
          assertThat(bodyValueAsJson).isEqualTo(bodyJson);
          cpCreatePets.flag();
        }));
      })
      .compose(v -> createRequest(GET, "/v1/pets/foobar").send())
      .onSuccess(response -> testContext.verify(() -> {
        JsonObject path = response.bodyAsJsonObject().getJsonObject("pathParameters");
        assertThat(path.getJsonObject("petId").getMap()).containsEntry("string", "foobar");
        cpShowPetById.flag();
      }))
      .onFailure(testContext::failNow);
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithoutValidation(VertxTestContext testContext) {
    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, rb -> {
      rb.rootHandler(BodyHandler.create()).getRoute("createPets")
        .setDoValidation(false)
        .setDoSecurity(false)
        .addHandler(rc -> rc.response().end(rc.body().buffer()));
      return Future.succeededFuture(rb);
    }).compose(v -> {
        JsonObject invalidBodyJson = new JsonObject().put("foo", "bar");
        return createRequest(POST, "/v1/pets").sendJsonObject(invalidBodyJson)
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.bodyAsJsonObject()).isEqualTo(invalidBodyJson);
            testContext.completeNow();
          }));
      })
      .onFailure(testContext::failNow);
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithCustomRequestExtractor(VertxTestContext testContext) {
    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, contract -> RouterBuilder.create(vertx, contract, withBodyHandler()), rb -> {
      rb.rootHandler(BodyHandler.create()).getRoute("createPets")
        .setDoSecurity(false)
        .addHandler(rc -> {
          ValidatedRequest validatedRequest = rc.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);
          rc.response().send(Json.encode(validatedRequest)).onFailure(testContext::failNow);
        });
      return Future.succeededFuture(rb);
    }).compose(v -> {
        JsonObject bodyJson = new JsonObject().put("id", 1).put("name", "FooBar");
        return createRequest(POST, "/v1/pets").sendJsonObject(bodyJson)
          .onSuccess(response -> testContext.verify(() -> {
            JsonObject body = response.bodyAsJsonObject().getJsonObject("body");
            JsonObject bodyValueAsJson = body.getJsonObject("jsonObject");
            assertThat(bodyValueAsJson).isEqualTo(bodyJson);
            testContext.completeNow();
          }));
      })
      .onFailure(testContext::failNow);
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithInvalidRequest(VertxTestContext testContext) {
    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, rb -> {
      rb.getRoute("createPets")
        .setDoSecurity(false)
        .addHandler(rc -> rc.response().end(rc.body().buffer()));
      return Future.succeededFuture(rb);
    }).compose(v -> {
        JsonObject invalidBodyJson = new JsonObject().put("foo", "bar");
        return createRequest(POST, "/v1/pets").sendJsonObject(invalidBodyJson)
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.code());
            assertThat(response.statusMessage()).isEqualTo(BAD_REQUEST.reasonPhrase());
            testContext.completeNow();
          }));
      })
      .onFailure(testContext::failNow);
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithNoHandlerReturns501NotImplemented(VertxTestContext testContext) {
    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, rb -> {
      // Intentionally do NOT add any handlers for the operations
      // This will trigger the default behavior of returning 501 Not Implemented
      return Future.succeededFuture(rb);
    }).compose(v -> createRequest(GET, "/v1/pets").send())
      .onSuccess(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        testContext.completeNow();
      }))
      .onFailure(testContext::failNow);
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithNoHandlersReturns501ForAllOperations(VertxTestContext testContext) {
    Checkpoint cpAllOperations = testContext.checkpoint(3);

    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, rb -> Future.succeededFuture(rb))
      .compose(v -> createRequest(GET, "/v1/pets").send())
      .onSuccess(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        cpAllOperations.flag();
      }))
      .compose(v -> {
        JsonObject bodyJson = new JsonObject().put("id", 1).put("name", "FooBar");
        return createRequest(POST, "/v1/pets").sendJsonObject(bodyJson);
      })
      .onSuccess(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        cpAllOperations.flag();
      }))
      .compose(v -> createRequest(GET, "/v1/pets/123").send())
      .onSuccess(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        cpAllOperations.flag();
      }))
      .onFailure(testContext::failNow);
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithPartialHandlersReturns501ForUnimplemented(VertxTestContext testContext) {
    Checkpoint cpAllOperations = testContext.checkpoint(3);

    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, rb -> {
      // Add handlers for only listPets and createPets operations but intentionally do NOT add handler for showPetById
      rb.getRoute("listPets")
        .setDoSecurity(false)
        .addHandler(rc -> rc.response().setStatusCode(OK.code()).end("[]"));
      rb.getRoute("createPets")
        .setDoSecurity(false)
        .addHandler(rc -> rc.response().setStatusCode(OK.code()).end());
      return Future.succeededFuture(rb);
    }).compose(v -> createRequest(GET, "/v1/pets").send())
      .onSuccess(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(OK.code());
        cpAllOperations.flag();
      }))
      .compose(v -> {
        JsonObject bodyJson = new JsonObject().put("id", 1).put("name", "FooBar");
        return createRequest(POST, "/v1/pets").sendJsonObject(bodyJson);
      })
      .onSuccess(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(OK.code());
        cpAllOperations.flag();
      }))
      .compose(v -> createRequest(GET, "/v1/pets/123").send())
      .onSuccess(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        cpAllOperations.flag();
      }))
      .onFailure(testContext::failNow);
  }
}
