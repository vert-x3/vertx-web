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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.openapi.validation.ValidatedRequest;
import io.vertx.router.test.ResourceHelper;
import io.vertx.router.test.base.RouterBuilderTestBase;
import io.vertx.test.core.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
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
  void testRouter(String version, VertxTestContext testContext, Checkpoint cpListPets, Checkpoint cpCreatePets, Checkpoint cpShowPetById) {
    CountDownLatch latchListPets = cpListPets.asLatch(2);
    CountDownLatch latchCreatePets = cpCreatePets.asLatch(2);
    CountDownLatch latchShowPetById = cpShowPetById.asLatch(2);

    Function<CountDownLatch, Handler<RoutingContext>> buildCheckpointHandler = cp -> rc -> {
      ValidatedRequest validatedRequest = rc.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);
      cp.countDown();
      rc.response().send(Json.encode(validatedRequest));
    };

    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve(version).resolve("petstore.json");
    HttpResponse<Buffer> response = createServer(pathDereferencedContract, rb -> {
      rb.getRoute("listPets")
        .setDoSecurity(false)
        .addHandler(buildCheckpointHandler.apply(latchListPets));
      rb.getRoute("createPets")
        .setDoSecurity(false)
        .addHandler(buildCheckpointHandler.apply(latchCreatePets));
      rb.getRoute("showPetById")
        .setDoSecurity(false)
        .addHandler(buildCheckpointHandler.apply(latchShowPetById));
      return Future.succeededFuture(rb);
    }).compose(v -> createRequest(GET, "/v1/pets")
        .addQueryParam("limit", "42").send())
      .onComplete(TestUtils.onSuccess2(r -> {
        JsonObject query = r.bodyAsJsonObject().getJsonObject("query");
        assertThat(query.getJsonObject("limit").getMap()).containsEntry("long", 42);
        latchListPets.countDown();
      }))
      .compose(v -> {
        JsonObject bodyJson = new JsonObject().put("id", 1).put("name", "FooBar");
        return createRequest(POST, "/v1/pets")
          .sendJsonObject(bodyJson).onComplete(TestUtils.onSuccess2(r -> {
          JsonObject body = r.bodyAsJsonObject().getJsonObject("body");
          JsonObject bodyValueAsJson = body.getJsonObject("jsonObject");
          assertThat(bodyValueAsJson).isEqualTo(bodyJson);
          latchCreatePets.countDown();
        }));
      })
      .compose(v -> createRequest(GET, "/v1/pets/foobar").send())
      .await();
    JsonObject path = response.bodyAsJsonObject().getJsonObject("pathParameters");
    assertThat(path.getJsonObject("petId").getMap()).containsEntry("string", "foobar");
    latchShowPetById.countDown();
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithoutValidation(VertxTestContext testContext, Checkpoint checkpoint) {
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
          .onComplete(TestUtils.onSuccess2(response -> {
            assertThat(response.bodyAsJsonObject()).isEqualTo(invalidBodyJson);
            checkpoint.flag();
          }));
      })
      .await();
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithCustomRequestExtractor(VertxTestContext testContext, Checkpoint checkpoint, Checkpoint cp2) {
    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, contract -> RouterBuilder.create(vertx, contract, withBodyHandler()), rb -> {
      rb.rootHandler(BodyHandler.create()).getRoute("createPets")
        .setDoSecurity(false)
        .addHandler(rc -> {
          ValidatedRequest validatedRequest = rc.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);
          rc.response().send(Json.encode(validatedRequest)).onComplete(cp2);
        });
      return Future.succeededFuture(rb);
    }).compose(v -> {
        JsonObject bodyJson = new JsonObject().put("id", 1).put("name", "FooBar");
        return createRequest(POST, "/v1/pets").sendJsonObject(bodyJson)
          .onComplete(TestUtils.onSuccess2(response -> {
            JsonObject body = response.bodyAsJsonObject().getJsonObject("body");
            JsonObject bodyValueAsJson = body.getJsonObject("jsonObject");
            assertThat(bodyValueAsJson).isEqualTo(bodyJson);
            checkpoint.flag();
          }));
      }).await();
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithInvalidRequest(VertxTestContext testContext, Checkpoint checkpoint) {
    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, rb -> {
      rb.getRoute("createPets")
        .setDoSecurity(false)
        .addHandler(rc -> rc.response().end(rc.body().buffer()));
      return Future.succeededFuture(rb);
    }).compose(v -> {
        JsonObject invalidBodyJson = new JsonObject().put("foo", "bar");
        return createRequest(POST, "/v1/pets").sendJsonObject(invalidBodyJson)
          .onComplete(TestUtils.onSuccess2(response -> {
            assertThat(response.statusCode()).isEqualTo(BAD_REQUEST.code());
            assertThat(response.statusMessage()).isEqualTo(BAD_REQUEST.reasonPhrase());
            checkpoint.flag();
          }));
      })
      .await();
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithNoHandlerReturns501NotImplemented(VertxTestContext testContext, Checkpoint checkpoint) {
    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, rb -> {
      // Intentionally do NOT add any handlers for the operations
      // This will trigger the default behavior of returning 501 Not Implemented
      return Future.succeededFuture(rb);
    }).compose(v -> createRequest(GET, "/v1/pets").send())
      .onComplete(TestUtils.onSuccess2(response -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        checkpoint.flag();
      }))
      .await();
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithNoHandlersReturns501ForAllOperations(VertxTestContext testContext,
      Checkpoint cpAllOperations) {
    CountDownLatch latchAllOperations = cpAllOperations.asLatch(3);

    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("v3.1").resolve("petstore.json");
    createServer(pathDereferencedContract, rb -> Future.succeededFuture(rb))
      .compose(v -> createRequest(GET, "/v1/pets").send())
      .onComplete(TestUtils.onSuccess2(response -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        latchAllOperations.countDown();
      }))
      .compose(v -> {
        JsonObject bodyJson = new JsonObject().put("id", 1).put("name", "FooBar");
        return createRequest(POST, "/v1/pets").sendJsonObject(bodyJson);
      })
      .onComplete(TestUtils.onSuccess2(response -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        latchAllOperations.countDown();
      }))
      .compose(v -> createRequest(GET, "/v1/pets/123").send())
      .onComplete(TestUtils.onSuccess2(response -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        latchAllOperations.countDown();
      }))
      .await();
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testRouterWithPartialHandlersReturns501ForUnimplemented(VertxTestContext testContext,
      Checkpoint cpAllOperations) {
    CountDownLatch latchAllOperations = cpAllOperations.asLatch(3);

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
      .onComplete(TestUtils.onSuccess2(response -> {
        assertThat(response.statusCode()).isEqualTo(OK.code());
        latchAllOperations.countDown();
      }))
      .compose(v -> {
        JsonObject bodyJson = new JsonObject().put("id", 1).put("name", "FooBar");
        return createRequest(POST, "/v1/pets").sendJsonObject(bodyJson);
      })
      .onComplete(TestUtils.onSuccess2(response -> {
        assertThat(response.statusCode()).isEqualTo(OK.code());
        latchAllOperations.countDown();
      }))
      .compose(v -> createRequest(GET, "/v1/pets/123").send())
      .onComplete(TestUtils.onSuccess2(response -> {
        assertThat(response.statusCode()).isEqualTo(NOT_IMPLEMENTED.code());
        latchAllOperations.countDown();
      }))
      .await();
  }
}
