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

package io.vertx.router.test.base;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("NewClassNamingConvention")
@ExtendWith(VertxExtension.class)
public class HttpServerTestBase {

  protected int port;
  protected Vertx vertx;

  /**
   * Creates a new HttpServer with the passed requestHandler.
   * <p></p>
   * <b>Note:</b> This method should only be called once during a test.
   *
   * @param requestHandler The related requestHandler
   * @return a succeeded {@link Future} if the server is running, otherwise a failed {@link Future}.
   */
  protected Future<Void> createServer(Handler<HttpServerRequest> requestHandler) {
    return vertx.createHttpServer().requestHandler(requestHandler).listen(0).
      onSuccess(server -> port = server.actualPort()).mapEmpty();
  }

  @BeforeEach
  void setup(Vertx vertx) {
    this.vertx = vertx;
  }

  @AfterEach
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void tearDown(VertxTestContext testContext) {
    if (vertx != null) {
      vertx.close(testContext.succeedingThenComplete());
    } else {
      testContext.completeNow();
    }
  }

  /**
   * Returns a pre-configured HTTP request.
   *
   * @param method The HTTP method of the request
   * @param path   The path of the request
   * @return a pre-configured HTTP request.
   */
  protected HttpRequest<Buffer> createRequest(HttpMethod method, String path) {
    WebClientOptions opts = new WebClientOptions().setDefaultHost("localhost").setDefaultPort(port);
    return WebClient.create(vertx, opts).request(method, path);
  }
}
