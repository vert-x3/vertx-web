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
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.handler.JWTAuthHandler;
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

class RouterBuilderSecurityScopesTest extends RouterBuilderTestBase {

  final Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("security").resolve("security_scopes_test.yaml");

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testBuilderWithAuthn(VertxTestContext testContext) {

    JWTAuth authProvider = JWTAuth.create(vertx, new JWTAuthOptions()
      .setKeyStore(new KeyStoreOptions()
        .setType("jceks")
        .setPath("keystore.jceks")
        .setPassword("secret")));

    createServer(pathDereferencedContract, rb -> {
      rb
        .security("bearerAuth")
        .httpHandler(JWTAuthHandler.create(authProvider));

      rb.getRoute("twoScopesRequired")
        .addHandler(ctx -> {
          ctx.json(ctx.currentRoute().<List>getMetadata("scopes"));
        });
      rb.getRoute("oneScopeRequired")
        .addHandler(ctx -> {
          ctx.json(ctx.currentRoute().<List>getMetadata("scopes"));
        });
      rb.getRoute("noScopesRequired")
        .addHandler(ctx -> {
          ctx.json(ctx.currentRoute().<List>getMetadata("scopes"));
        });

      return Future.succeededFuture(rb);
    })
      .compose(v -> {
        return createRequest(GET, "/v1/two_scopes_required")
          .putHeader("Authorization", "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo").put("scope", new JsonArray().add("read").add("write")), new JWTOptions()))
          .send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
          }));
      })
      .compose(v -> {
        return createRequest(GET, "/v1/one_scope_required")
          .putHeader("Authorization", "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo").put("scope", new JsonArray().add("read")), new JWTOptions()))
          .send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
          }));
      })
      .compose(v -> {
        return createRequest(GET, "/v1/no_scopes")
          .putHeader("Authorization", "Bearer " + authProvider.generateToken(new JsonObject().put("sub", "paulo"), new JWTOptions()))
          .send()
          .onSuccess(response -> testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyAsJsonArray()).isNull();
          }));
      })
      .onSuccess(v -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }
}
