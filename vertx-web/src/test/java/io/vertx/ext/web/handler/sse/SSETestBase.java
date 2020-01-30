/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.handler.sse;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CountDownLatch;

abstract class SSETestBase extends VertxTestBase {

  protected final String TOKEN = "test";

  private final static Integer PORT = 9009;

  protected SSEConnection connection;
  protected SSEHandler sseHandler;

  private HttpServer server;
  private HttpClientOptions options;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    CountDownLatch latch = new CountDownLatch(1);
    HttpServerOptions options = new HttpServerOptions();
    options.setPort(PORT);
    server = vertx.createHttpServer(options);
    Router router = Router.router(vertx);
    sseHandler = SSEHandler.create();
    sseHandler.connectHandler(connection -> {
      final HttpServerRequest request = connection.request();
      final String token = request.getParam("token");
      if (token == null) {
        connection.reject(401);
      } else if (!TOKEN.equals(token)) {
        connection.reject(403);
      } else {
        this.connection = connection; // accept
      }
    });
    sseHandler.closeHandler(connection -> {
      if (this.connection != null) {
        this.connection = null;
      }
    });
    router.get("/sse").handler(sseHandler);
    server.requestHandler(router);
    server.listen(ar -> {
      if (ar.failed()) {
        fail(ar.cause());
      }
      latch.countDown();
    });
    awaitLatch(latch);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    connection = null;
    sseHandler = null;
  }

  EventSource eventSource() {
    return EventSource.create(vertx, clientOptions());
  }

  HttpClient client() {
    return vertx.createHttpClient(clientOptions());
  }

  private HttpClientOptions clientOptions() {
    if (options == null) {
      options = new HttpClientOptions();
      options.setDefaultPort(PORT);
    }
    return options;
  }

}
