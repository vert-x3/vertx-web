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
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.router.ResourceHelper;
import io.vertx.router.test.base.RouterBuilderTestBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.vertx.core.http.HttpMethod.GET;

class RouterBuilderSecurityTest extends RouterBuilderTestBase {

  final Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("security").resolve("security_test.yaml");
  final Path pathDereferencedContractGlobal = ResourceHelper.TEST_RESOURCE_PATH.resolve("security").resolve("global_security_test.yaml");

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testBuilderMissingAuthn(VertxTestContext testContext) {
    createServer(pathDereferencedContract, rb -> Future.succeededFuture(rb))
      .onSuccess(v -> testContext.failNow("Should not be able to create a server without authentication"))
      .onFailure(err -> {
        assertThat(err.getMessage()).contains("Missing security handler for: 'api_key'");
        testContext.completeNow();
      });
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testBuilderWithAuthn(VertxTestContext testContext) {
    createServer(pathDereferencedContractGlobal, rb -> {
      rb.securityHandler("api_key")
        .bindBlocking(config -> APIKeyHandler.create(null).header(config.getString("name")))
        .securityHandler("global_api_key")
        .bindBlocking(config -> APIKeyHandler.create(null).header(config.getString("name")));
      return Future.succeededFuture(rb);
    })
      .onSuccess(v -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }

  @Test
  public void mountSingle(Vertx vertx, VertxTestContext testContext) {
    createServer(pathDereferencedContract, rb -> {
      rb
        .securityHandler("api_key")
        .bindBlocking(config -> SimpleAuthenticationHandler.create()
          .authenticate(ctx -> {
            ctx
              .<JsonArray>data()
              .computeIfAbsent("security", k -> new JsonArray())
              .add("api_key");
            return Future.succeededFuture(User.create(new JsonObject()));
          }))
        .securityHandler("second_api_key")
        .bindBlocking(config -> SimpleAuthenticationHandler.create()
          .authenticate(ctx -> {
            ctx
              .<JsonArray>data()
              .computeIfAbsent("security", k -> new JsonArray())
              .add("second_api_key");
            return Future.succeededFuture(User.create(new JsonObject()));
          }))
        .securityHandler("third_api_key")
        .bindBlocking(config -> SimpleAuthenticationHandler.create()
          .authenticate(ctx -> {
            ctx
              .<JsonArray>data()
              .computeIfAbsent("security", k -> new JsonArray())
              .add("third_api_key");
            return Future.succeededFuture(User.create(new JsonObject()));
          }))
        .securityHandler("oauth2")
        .bindBlocking(config -> SimpleAuthenticationHandler.create()
          .authenticate(ctx -> {
            ctx
              .<JsonArray>data()
              .computeIfAbsent("security", k -> new JsonArray())
              .add("oauth2");
            return Future.succeededFuture(User.create(new JsonObject()));
          }))
        .securityHandler("sibling_second_api_key")
        .bindBlocking(config -> SimpleAuthenticationHandler.create()
          .authenticate(ctx -> {
            ctx
              .<JsonArray>data()
              .computeIfAbsent("security", k -> new JsonArray())
              .add("sibling_second_api_key");
            return Future.succeededFuture(User.create(new JsonObject()));
          }));

      rb
        .getRoute("listPetsSingleSecurity")
        .addHandler(ctx -> ctx.json(ctx.<JsonArray>get("security")));

      rb
        .getRoute("listPetsAndSecurity")
        .addHandler(ctx -> ctx.json(ctx.<JsonArray>get("security")));

      rb
        .getRoute("listPetsOrSecurity")
        .addHandler(ctx -> ctx.json(ctx.<JsonArray>get("security")));

      rb
        .getRoute("listPetsOrAndSecurity")
        .addHandler(ctx -> ctx.json(ctx.<JsonArray>get("security")));

      rb
        .getRoute("listPetsOauth2")
        .addHandler(ctx -> ctx.json(ctx.<JsonArray>get("security")));

      return Future.succeededFuture(rb);
    })
      .compose(v -> {
        return createRequest(GET, "/v1/pets_single_security").send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyAsJsonArray()).isEqualTo(new JsonArray().add("api_key"));
          }));
      })
      .compose(v -> {
        return createRequest(GET, "/v1/pets_and_security").send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyAsJsonArray()).isEqualTo(new JsonArray().add("api_key").add("second_api_key").add("third_api_key"));
          }));
      })
      .compose(v -> {
        return createRequest(GET, "/v1/pets_or_security").send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyAsJsonArray()).isEqualTo(new JsonArray().add("api_key"));
          }));
      })
      .compose(v -> {
        return createRequest(GET, "/v1/pets_or_and_security").send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyAsJsonArray()).isEqualTo(new JsonArray().add("api_key"));
          }));
      })
      .compose(v -> {
        return createRequest(GET, "/v1/pets_oauth2").send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyAsJsonArray()).isEqualTo(new JsonArray().add("oauth2"));
          }));
      })
      .onSuccess(v -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }
}
