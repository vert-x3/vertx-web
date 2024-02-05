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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.service.OpenAPIRouterHandler;
import io.vertx.ext.web.api.service.PetStoreService;
import io.vertx.ext.web.api.service.ServiceRequest;
import io.vertx.ext.web.api.service.ServiceResponse;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import io.vertx.openapi.contract.OpenAPIContract;
import io.vertx.openapi.validation.ResponseValidator;
import io.vertx.router.ResourceHelper;
import io.vertx.router.test.base.RouterBuilderTestBase;
import io.vertx.serviceproxy.ServiceBinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.truth.Truth.assertThat;
import static io.vertx.core.Future.succeededFuture;
import static io.vertx.ext.web.api.service.ServiceResponse.completedWithJson;

@Execution(ExecutionMode.SAME_THREAD) // Don't run tests in parallel, because they share a field.
class OpenAPIRouterHandlerImplTest extends RouterBuilderTestBase {
  public static final Path CONTRACT_PATH = ResourceHelper.TEST_RESOURCE_PATH.resolve("petstore.yaml");
  public static final String EVENTBUS_ADDRESS = "myEventbusAddress";

  MessageConsumer<JsonObject> consumer;

  void registerService(PetStoreService serviceInstance) {
    final ServiceBinder serviceBinder = new ServiceBinder(vertx).setAddress(EVENTBUS_ADDRESS);
    consumer = serviceBinder.register(PetStoreService.class, serviceInstance);
  }

  @AfterEach
  void tearDown() {
    if (consumer != null) consumer.unregister();
  }

  private Future<Void> createServer(VertxTestContext testContext) {
    return createServer(rb -> {
      rb.rootHandler(rtx -> {
        rtx.addEndHandler(v -> {
          if (v.failed()) {
            testContext.failNow(v.cause());
          }
        });
        rtx.next();
      });
      return rb;
    });
  }

  private Future<Void> createServer(Function<RouterBuilder, RouterBuilder> modifyRouterBuilder) {
    return OpenAPIContract.from(vertx, CONTRACT_PATH.toString()).compose(contract -> {
        ResponseValidator respValidator = ResponseValidator.create(vertx, contract);
        return createServer(CONTRACT_PATH, rb -> {
          rb.getRoutes().forEach(r -> r.addHandler(OpenAPIRouterHandler.create(vertx, r.getOperation(),
            respValidator)));
          return succeededFuture(modifyRouterBuilder.apply(rb));
        });
      }
    );
  }

  @Test
  @DisplayName("Test eventbus address determination")
  void testEventbusAddressDetermination(VertxTestContext testContext) {
    Checkpoint addressOnly = testContext.checkpoint();
    Checkpoint objectWithAddress = testContext.checkpoint();
    Checkpoint objectWithAddressAndMethod = testContext.checkpoint();

    registerService(new DummyPetStoreServiceImpl() {
      @Override
      public Future<ServiceResponse> listPets(Integer limit, ServiceRequest context) {
        addressOnly.flag();
        return super.listPets(limit, context);
      }

      @Override
      public Future<ServiceResponse> createPets(JsonObject body, ServiceRequest context) {
        objectWithAddress.flag();
        return super.createPets(body, context);
      }

      @Override
      public Future<ServiceResponse> getPetById(String petId, ServiceRequest context) {
        objectWithAddressAndMethod.flag();
        return super.getPetById(petId, context);
      }
    });

    createServer(testContext).compose(v -> createRequest(HttpMethod.GET, "/v1/pets").send())
      .compose(v -> {
        JsonObject newPet = PetStoreService.buildPet(1, "foo");
        return createRequest(HttpMethod.POST, "/v1/pets").sendJsonObject(newPet);
      }).compose(v -> createRequest(HttpMethod.GET, "/v1/pets/123").send())
      .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("Test that request parameters get forwarded correctly")
  void testParametersForwardedCorrectly(VertxTestContext testContext) {
    Checkpoint cp = testContext.checkpoint(3);

    int expectedLimit = 1337;
    JsonObject expectedPet = PetStoreService.buildPet(1337, "Foo");
    String expectedPetId = "123";

    registerService(new DummyPetStoreServiceImpl() {
      @Override
      public Future<ServiceResponse> listPets(Integer limit, ServiceRequest context) {
        testContext.verify(() -> assertThat(limit).isEqualTo(expectedLimit));
        cp.flag();
        return super.listPets(limit, context);
      }

      @Override
      public Future<ServiceResponse> createPets(JsonObject body, ServiceRequest context) {
        testContext.verify(() -> assertThat(body).isEqualTo(expectedPet));
        cp.flag();
        return super.createPets(body, context);
      }

      @Override
      public Future<ServiceResponse> getPetById(String petId, ServiceRequest context) {
        testContext.verify(() -> assertThat(petId).isEqualTo(expectedPetId));
        cp.flag();
        return super.getPetById(petId, context);
      }
    });

    createServer(testContext).compose(v -> createRequest(HttpMethod.GET, "/v1/pets").addQueryParam("limit",
        "" + expectedLimit).send())
      .compose(v -> createRequest(HttpMethod.POST, "/v1/pets").sendJsonObject(expectedPet))
      .compose(v -> createRequest(HttpMethod.GET, "/v1/pets/" + expectedPetId).send())
      .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("Test that response gets forwarded correctly")
  void testResponseIsForwardedCorrectly(VertxTestContext testContext) {
    JsonArray petsToReturn = new JsonArray().add(PetStoreService.buildPet(1, "foo"));
    Checkpoint cp = testContext.checkpoint(2);

    registerService(new DummyPetStoreServiceImpl() {
      @Override
      public Future<ServiceResponse> listPets(Integer limit, ServiceRequest context) {
        return succeededFuture(completedWithJson(petsToReturn).putHeader("X-Custom", "1"));
      }

      @Override
      public Future<ServiceResponse> createPets(JsonObject body, ServiceRequest context) {
        return succeededFuture(new ServiceResponse().setStatusCode(201).putHeader("X-Custom", "2"));
      }
    });

    Supplier<Future<Void>> requestAndVerifyList =
      () -> createRequest(HttpMethod.GET, "/v1/pets").send().onSuccess(resp -> testContext.verify(() -> {
        assertThat(resp.statusCode()).isEqualTo(200);
        assertThat(resp.getHeader("X-Custom")).isEqualTo("1");
        assertThat(resp.bodyAsJsonArray()).isEqualTo(petsToReturn);
        cp.flag();
      })).mapEmpty();

    Supplier<Future<Void>> requestAndVerifyCreate = () -> {
      JsonObject expectedPet = PetStoreService.buildPet(1337, "Foo");
      return createRequest(HttpMethod.POST, "/v1/pets").sendJsonObject(expectedPet).onSuccess(resp -> testContext.verify(() -> {
        assertThat(resp.statusCode()).isEqualTo(201);
        assertThat(resp.getHeader("X-Custom")).isEqualTo("2");
        cp.flag();
      })).mapEmpty();
    };

    createServer(testContext).compose(v -> requestAndVerifyList.get())
      .compose(v -> requestAndVerifyCreate.get())
      .onFailure(testContext::failNow);
  }

  @Test
  @DisplayName("Test that response gets forwarded correctly")
  void testResponseMissingContentHeader(VertxTestContext testContext) {
    registerService(new DummyPetStoreServiceImpl() {
      @Override
      public Future<ServiceResponse> getPetById(String petId, ServiceRequest context) {
        Buffer payload = PetStoreService.buildPet(1, "foo").toBuffer();
        return succeededFuture(new ServiceResponse().setStatusCode(200).setPayload(payload));
      }
    });

    Checkpoint cp = testContext.checkpoint(2);
    Supplier<Future<Void>> requestAndVerifyList =
      () -> createRequest(HttpMethod.GET, "/v1/pets/1").send().onSuccess(resp -> testContext.verify(() -> {
        assertThat(resp.statusCode()).isEqualTo(500);
        cp.flag();
      })).mapEmpty();

    createServer(routerBuilder -> {
      return routerBuilder.rootHandler(rtx -> {
        rtx.addEndHandler(v -> {
          if (rtx.failed()) {
            testContext.verify(() -> {
              String expectedMsg = "Content-Type header is required, when response contains a body.";
              assertThat(rtx.failure()).hasMessageThat().isEqualTo(expectedMsg);
              assertThat(rtx.failure()).isInstanceOf(IllegalArgumentException.class);
              cp.flag();
            });
          }
        });
        rtx.next();
      });
    }).compose(v -> requestAndVerifyList.get()).onFailure(testContext::failNow);
  }
}
