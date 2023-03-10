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
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import io.vertx.router.ResourceHelper;
import io.vertx.router.test.base.RouterBuilderTestBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;

class RouterBuilderSecurityTest extends RouterBuilderTestBase {

  final Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("security").resolve("global_security_test.yaml");

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
    createServer(pathDereferencedContract, rb -> {
      rb.securityHandler("api_key")
        .bindBlocking(config -> APIKeyHandler.create(null).header(config.getString("name")))
        .securityHandler("global_api_key")
        .bindBlocking(config -> APIKeyHandler.create(null).header(config.getString("name")));
      return Future.succeededFuture(rb);
    })
      .onSuccess(v -> testContext.completeNow())
      .onFailure(testContext::failNow);
  }
}
