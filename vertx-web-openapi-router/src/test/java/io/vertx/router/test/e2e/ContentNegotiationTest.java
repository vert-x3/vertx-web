/*
 * Copyright (c) 2026, SAP SE
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
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.junit5.Timeout;
import io.vertx.openapi.validation.ValidatedRequest;
import io.vertx.router.test.ResourceHelper;
import io.vertx.router.test.base.RouterBuilderTestBase;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static io.vertx.core.http.HttpMethod.POST;

class ContentNegotiationTest extends RouterBuilderTestBase {

  private static final Path CONTRACT =
    ResourceHelper.TEST_RESOURCE_PATH.resolve("e2e").resolve("contract_content_negotiation.yaml");

  private Future<Void> deploy() {
    return createServer(CONTRACT, rb -> {
      rb.getRoute("createPet").setDoSecurity(false)
        .addHandler(rc -> {
          ValidatedRequest validatedRequest = rc.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);
          rc.response().putHeader("content-type", "application/json")
            .end(validatedRequest.getBody().getJsonObject().toBuffer());
        });
      return Future.succeededFuture(rb);
    });
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testSupportedContentTypeIsAccepted() {
    JsonObject body = new JsonObject().put("name", "FooBar");
    deploy()
      .compose(v -> createRequest(POST, "/v1/pets").sendJsonObject(body)
        .expecting(HttpResponseExpectation.SC_OK))
      .await();
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testUnsupportedContentTypeIsRejectedWith415() {
    deploy()
      .compose(v -> createRequest(POST, "/v1/pets")
        .putHeader("content-type", "text/xml")
        .sendBuffer(Buffer.buffer("<pet/>"))
        .expecting(HttpResponseExpectation.SC_UNSUPPORTED_MEDIA_TYPE))
      .await();
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testUnsupportedAcceptIsRejectedWith406() {
    JsonObject body = new JsonObject().put("name", "FooBar");
    deploy()
      .compose(v -> createRequest(POST, "/v1/pets")
        .putHeader("accept", "text/xml")
        .sendJsonObject(body)
        .expecting(HttpResponseExpectation.SC_NOT_ACCEPTABLE))
      .await();
  }

  @Test
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void testAcceptedAcceptIsProcessed() {
    JsonObject body = new JsonObject().put("name", "FooBar");
    deploy()
      .compose(v -> createRequest(POST, "/v1/pets")
        .putHeader("accept", "application/json")
        .sendJsonObject(body)
        .expecting(HttpResponseExpectation.SC_OK))
      .map(response -> {
        assertThat(response.bodyAsJsonObject()).isEqualTo(body);
        return response;
      })
      .await();
  }
}
