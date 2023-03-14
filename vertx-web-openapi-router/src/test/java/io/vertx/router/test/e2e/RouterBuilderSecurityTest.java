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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
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
  void testBuilderWithAuthn(VertxTestContext testContext) {
    createServer(pathDereferencedContractGlobal, rb -> {
      rb.security("api_key")
        .apiKeyHandler(APIKeyHandler.create(null))
        .security("global_api_key")
        .apiKeyHandler(APIKeyHandler.create(null));
      return Future.succeededFuture(rb);
    })
      .onSuccess(v -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }

  @Test
  public void mountSingle(Vertx vertx, VertxTestContext testContext) {

    AuthenticationProvider authProvider = cred -> Future.succeededFuture(User.fromName(cred.toString()));

    JsonObject fixture = new JsonObject(
      "{" +
        "  \"access_token\": \"4adc339e0\"," +
        "  \"refresh_token\": \"ec1a59d298\"," +
        "  \"token_type\": \"bearer\"," +
        "  \"scope\": \"read write\"," +
        "  \"expires_in\": 7200" +
        "}");

    HttpServer server = vertx.createHttpServer()
      .requestHandler(req -> {
        if (req.method() == HttpMethod.POST && "/oauth/token".equals(req.path())) {
          req.setExpectMultipart(true).bodyHandler(buffer -> req.response().putHeader("Content-Type", "application/json").end(fixture.encode()));
        } else if (req.method() == HttpMethod.POST && "/oauth/revoke".equals(req.path())) {
          req.setExpectMultipart(true).bodyHandler(buffer -> req.response().end());
        } else {
          req.response().setStatusCode(200).end();
        }
      });

    server.listen(10000).onComplete(ready -> {
      if (ready.failed()) {
        throw new RuntimeException(ready.cause());
      }

      createServer(pathDereferencedContract, rb -> {
        rb
          .security("api_key")
          .apiKeyHandler(APIKeyHandler.create(authProvider))
          .security("second_api_key")
          .apiKeyHandler(APIKeyHandler.create(authProvider))
          .security("third_api_key")
          .apiKeyHandler(APIKeyHandler.create(authProvider))
          .security("sibling_second_api_key")
          .apiKeyHandler(APIKeyHandler.create(authProvider))
          .security("oauth2")
          .oauth2Handler("/callback", config -> {

            OAuth2Auth oauth2 = OAuth2Auth.create(vertx, new OAuth2Options()
              .setClientId("client-id")
              .setClientSecret("client-secret")
              .setSite("http://localhost:10000"));

            return
              // create an oauth2 handler on our domain to the callback: "http://localhost:8080/callback"
              OAuth2AuthHandler
                .create(vertx, oauth2, "http://localhost:8080/callback");
          });

        rb
          .getRoute("listPetsSingleSecurity")
          .addHandler(RoutingContext::end);

        rb
          .getRoute("listPetsAndSecurity")
          .addHandler(RoutingContext::end);

        rb
          .getRoute("listPetsOrSecurity")
          .addHandler(RoutingContext::end);

        rb
          .getRoute("listPetsOrAndSecurity")
          .addHandler(RoutingContext::end);

        rb
          .getRoute("listPetsOauth2")
          .addHandler(RoutingContext::end);

        return Future.succeededFuture(rb);
      })
        .compose(v -> {
          return createRequest(GET, "/v1/pets_single_security")
            .putHeader("api_key", "test")
            .send()
            .onSuccess(response -> testContext.verify(() -> {
              assertThat(response.statusCode()).isEqualTo(200);
            }));
        })
        .compose(v -> {
          return createRequest(GET, "/v1/pets_and_security")
            .putHeader("api_key", "test")
            .putHeader("second_api_key", "test")
            .putHeader("third_api_key", "test")
            .send()
            .onSuccess(response -> testContext.verify(() -> {
              assertThat(response.statusCode()).isEqualTo(200);
            }));
        })
        .compose(v -> {
          return createRequest(GET, "/v1/pets_or_security")
            .putHeader("api_key", "test")
            .send()
            .onSuccess(response -> testContext.verify(() -> {
              assertThat(response.statusCode()).isEqualTo(200);
            }));
        })
        .compose(v -> {
          return createRequest(GET, "/v1/pets_or_security")
            .putHeader("second_api_key", "test")
            .send()
            .onSuccess(response -> testContext.verify(() -> {
              assertThat(response.statusCode()).isEqualTo(200);
            }));
        })
        .compose(v -> {
          return createRequest(GET, "/v1/pets_or_and_security")
            .putHeader("api_key", "test")
            .send()
            .onSuccess(response -> testContext.verify(() -> {
              assertThat(response.statusCode()).isEqualTo(200);
            }));
        })
        .compose(v -> {
          return createRequest(GET, "/v1/pets_or_and_security")
            .putHeader("second_api_key", "test")
            .putHeader("sibling_second_api_key", "test")
            .send()
            .onSuccess(response -> testContext.verify(() -> {
              assertThat(response.statusCode()).isEqualTo(200);
            }));
        })
        .compose(v -> {
          return createRequest(GET, "/v1/pets_oauth2")
            .send()
            .onSuccess(response -> testContext.verify(() -> {
              assertThat(response.statusCode()).isEqualTo(200);
            }));
        })
        .onSuccess(v -> testContext.completeNow())
        .onFailure(testContext::failNow);
    });
  }
}
