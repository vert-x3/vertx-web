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
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.router.ResourceHelper;
import io.vertx.router.test.base.RouterBuilderTestBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.vertx.core.http.HttpMethod.GET;

class RouterBuilderSecurityOptionalCallbackawareTest extends RouterBuilderTestBase {

  final Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("security").resolve("security_optional_callbackaware.yaml");

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testBuilderWithAuthn(VertxTestContext testContext) {

    AuthenticationProvider authProvider = cred -> Future.succeededFuture(User.fromName(cred.toString()));

    createServer(pathDereferencedContract, rb -> rb
      .security("oidc")
      .openIdConnectHandler(discoveryUrl ->
        OpenIDConnectAuth
          .discover(vertx,
            // hardcoded Azure AD because it's pretty easy to assume it will be always available
            new OAuth2Options()
              // force v2.0
              .setSite("https://login.microsoftonline.com/{tenant}/v2.0")
              .setClientId("client-id")
              .setClientSecret("client-secret")
              .setTenant("common")
              .setValidateIssuer(false)
              // for extra security enforce the audience validation
              .setJWTOptions(new JWTOptions()
                .addAudience("api://client-id")))
          .map(oidc -> OAuth2AuthHandler.create(vertx, oidc)))
      .onSuccess(self -> {
        self
          .getRoute("opA")
          .addHandler(ctx -> ctx.json(ctx.user().get().principal()));
        self
          .getRoute("opB")
          .addHandler(ctx -> ctx.json(ctx.user().get().principal()));
      }))
      // this test may seem useless but it proves that the chain auth properly sets up a chain when the a handler
      // can perform redirects (callback aware) and doesn't throw an exception at setup time.
      .onSuccess(v -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }
}
