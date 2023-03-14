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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.router.ResourceHelper;
import io.vertx.router.test.base.RouterBuilderTestBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.vertx.core.http.HttpMethod.GET;

class RouterBuilderSecurityOptionalTest extends RouterBuilderTestBase {

  final Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("security").resolve("security_optional_test.yaml");

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testBuilderWithAuthn(VertxTestContext testContext) {
    createServer(pathDereferencedContract, rb -> {
      rb
        .securityHandler("api_key")
        .bindBlocking(config -> SimpleAuthenticationHandler.create()
          .authenticate(ctx -> {
            if (ctx.request().getHeader("api_key") == null) {
              return Future.failedFuture("No api_key header");
            }
            ctx
              .<JsonArray>data()
              .computeIfAbsent("security", k -> new JsonArray())
              .add("api_key");
            return Future.succeededFuture(User.create(new JsonObject()));
          }));

      rb.getRoute("pets")
        .addHandler(ctx -> ctx.json(ctx.<JsonArray>get("security")));

      return Future.succeededFuture(rb);
    })
      .compose(v -> {
        return createRequest(GET, "/v1/pets").send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyAsJsonArray()).isNull();
          }));
      })
      .compose(v -> {
        return createRequest(GET, "/v1/pets").putHeader("api_key", "123456789").send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyAsJsonArray()).isEqualTo(new JsonArray().add("api_key"));
          }));
      })
      .onSuccess(v -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }
}
