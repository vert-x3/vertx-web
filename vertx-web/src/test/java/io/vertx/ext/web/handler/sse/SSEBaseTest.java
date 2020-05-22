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
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sse.EventSource;
import io.vertx.ext.web.handler.sse.EventSourceOptions;
import io.vertx.ext.web.handler.sse.SSEConnection;
import io.vertx.ext.web.handler.sse.SSEHandler;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

abstract class SSEBaseTest extends VertxTestBase {

  protected final String TOKEN = "test";
  protected final String EB_ADDRESS = "eb-forward-sse";
  protected final static String SSE_NO_CONTENT_ENDPOINT = "/sse-no-content";
  protected final static String SSE_REJECT_ODDS = "/sse-reject-odds";
  protected final static String SSE_RESET_CONTENT_ENDPOINT = "/sse-reset-content";
  protected final static String SSE_RECONNECT_ENDPOINT = "/sse-reconnect";
  protected final static String SSE_REDIRECT_ENDPOINT = "/sse-redirect";
  protected final static String SSE_EVENTBUS_ENDPOINT = "/sse-eventbus";
  protected final static String SSE_ENDPOINT = "/sse";

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
      this.connection = connection; // accept
    });
    router.get(SSE_ENDPOINT).handler(rc -> {
      final HttpServerRequest request = rc.request();
      final String token = request.getParam("token");
      if (token == null) {
        rc.fail(401);
      } else if (!TOKEN.equals(token)) {
        rc.fail(403);
      } else {
        rc.next();
      }
    });
    router.get(SSE_NO_CONTENT_ENDPOINT).handler(rc -> rc.response().setStatusCode(204).end());
    SSEHandler sseReconnectHandler = SSEHandler.create();
    AtomicInteger nbSSEConn = new AtomicInteger(0);
    sseReconnectHandler.connectHandler(conn -> {
      if (nbSSEConn.incrementAndGet() <= 1) {
        conn.close(); // force a reconnect on client side
      }
    });
    router.get(SSE_RECONNECT_ENDPOINT).handler(sseReconnectHandler);
    router.get(SSE_REDIRECT_ENDPOINT).handler(rc -> {
      rc.response().putHeader(HttpHeaders.LOCATION.toString(), SSE_ENDPOINT + "?token=" + TOKEN);
      rc.response().setStatusCode(302).end();
    });
    AtomicInteger nbConnections = new AtomicInteger(0);
    router.get(SSE_REJECT_ODDS).handler(rc -> {
      if (nbConnections.incrementAndGet() % 2 == 1) {
        rc.response().setStatusCode(204).end();
      } else {
        sseHandler.handle(rc);
      }
    });
    router.get(SSE_RESET_CONTENT_ENDPOINT).handler(rc -> rc.response().setStatusCode(205).end());
    router.get(SSE_ENDPOINT).handler(sseHandler);
    SSEHandler sseEventBusHandler = SSEHandler.create();
    sseEventBusHandler.connectHandler(conn -> conn.forward(EB_ADDRESS));
    router.get(SSE_EVENTBUS_ENDPOINT).handler(sseEventBusHandler);
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

  EventSource eventSource(long retryPeriod) {
    return EventSource.create(vertx, clientOptions().setRetryPeriod(retryPeriod));
  }

  EventSource eventSource() {
    return EventSource.create(vertx, clientOptions());
  }

  HttpClient client() {
    return vertx.createHttpClient(clientOptions());
  }

  private EventSourceOptions clientOptions() {
    if (options == null) {
      options = new HttpClientOptions();
      options.setDefaultPort(PORT);
    }
    return new EventSourceOptions(options);
  }

}
