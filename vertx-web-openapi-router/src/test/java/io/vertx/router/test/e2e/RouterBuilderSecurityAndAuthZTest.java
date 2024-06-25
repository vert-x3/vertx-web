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
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.handler.AuthorizationHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.junit5.VertxTestContext;
import io.vertx.router.ResourceHelper;
import io.vertx.router.test.base.RouterBuilderTestBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static io.vertx.core.http.HttpMethod.GET;

class RouterBuilderSecurityAndAuthZTest extends RouterBuilderTestBase {

  final Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("security").resolve("security_test.yaml");

  @Test
  public void mountSingle(Vertx vertx, VertxTestContext testContext) {

    AuthenticationProvider authProvider = cred -> {
      User user = User.fromName(cred.toString());
      user.authorizations().put("fake", PermissionBasedAuthorization.create("read"));
      return Future.succeededFuture(user);
    };

    createServer(pathDereferencedContract, rb -> {
      rb
        .security("api_key")
        .apiKeyHandler(APIKeyHandler.create(authProvider));

      rb
        .getRoute("listPetsSingleSecurity")
        .addHandler(AuthorizationHandler.create(PermissionBasedAuthorization.create("read")))
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
      .onSuccess(v -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }
}
