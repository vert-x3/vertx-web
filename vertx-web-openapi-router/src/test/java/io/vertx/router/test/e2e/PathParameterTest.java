/*
 * Copyright (c) 2025, SAP SE
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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.router.RouterBuilder;
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

import static com.google.common.truth.Truth.assertThat;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

class PathParameterTest extends RouterBuilderTestBase {

  @Timeout(value = 2, timeUnit = TimeUnit.MINUTES)
  @ParameterizedTest
  @ValueSource(strings = {"3", "-1"})
  void testPathParam(String id, VertxTestContext testContext) {
    Path pathDereferencedContract = ResourceHelper.TEST_RESOURCE_PATH.resolve("e2e").resolve("contract_various_scenarios.yaml");
    createServer(pathDereferencedContract, rb -> {
      rb.getRoute("stringPathParameter").setDoSecurity(false).addHandler(rc -> {
        ValidatedRequest validatedRequest = rc.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);
        testContext.verify(() -> {
          assertThat(validatedRequest.getPathParameters().get("id").getString()).isEqualTo(id);
        });
        rc.response().setStatusCode(200).end();
      });
      return Future.succeededFuture(rb);
    }).compose(v -> {
      return createRequest(GET, "/v1/user/" + id).send().onSuccess(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(HttpResponseStatus.OK.code());
        testContext.completeNow();
      }));
    }).onFailure(testContext::failNow);
  }
}
