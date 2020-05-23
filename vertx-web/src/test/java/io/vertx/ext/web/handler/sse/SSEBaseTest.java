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
import io.vertx.ext.web.RoutingContext;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

abstract class SSEBaseTest extends VertxTestBase {

  protected final String TOKEN = "test";
  protected final String EB_ADDRESS = "eb-forward-sse";
  protected String multilineMsg = "Some message split across multiple lines";
  protected final static String SSE_NO_CONTENT_ENDPOINT = "/sse-no-content";
  protected final static String SSE_REJECT_ODDS = "/sse-reject-odds";
  protected final static String SSE_RESET_CONTENT_ENDPOINT = "/sse-reset-content";
  protected final static String SSE_RECONNECT_ENDPOINT = "/sse-reconnect";
  protected final static String SSE_REDIRECT_ENDPOINT = "/sse-redirect";
  protected final static String SSE_EVENTBUS_ENDPOINT = "/sse-eventbus";
  protected final static String SSE_MULTIPLE_MESSAGES_ENDPOINT = "/sse-multiple-messages";
  protected final static String SSE_ID_TEST_ENDPOINT = "/sse-id-tests";
  protected final static String SSE_MULTILINE_ENDPOINT = "/sse-multiline";
  protected final static String SSE_ENDPOINT = "/sse";

  private final static Integer PORT = 9009;

  protected SSEConnection connection;

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
    // Non-SSE handlers
    router.get(SSE_NO_CONTENT_ENDPOINT).handler(rc -> rc.response().setStatusCode(204).end());
    router.get(SSE_RESET_CONTENT_ENDPOINT).handler(rc -> rc.response().setStatusCode(205).end());
    router.get(SSE_REDIRECT_ENDPOINT).handler(rc -> {
      // Client-side redirect
      rc.response().putHeader(HttpHeaders.LOCATION.toString(), SSE_ENDPOINT + "?token=" + TOKEN);
      rc.response().setStatusCode(302).end();
    });
    // Proper SSE handlers
    attachSSEHandlers(router);
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

  private void attachSSEHandlers(Router router) {
    // A simple SSE handler that doesn't do anything specific apart accepting the connection
    SSEHandler sseHandler = SSEHandler.create();
    sseHandler.connectHandler(connection -> {
      this.connection = connection; // accept
    });
    router.get(SSE_ENDPOINT).handler(this::protectEndpoint);
    router.get(SSE_ENDPOINT).handler(sseHandler);

    // The purpose of this handler is to close the connection the first time a client connects, to force it to reconnect
    SSEHandler sseReconnectHandler = SSEHandler.create();
    AtomicInteger nbSSEConn = new AtomicInteger(0);
    sseReconnectHandler.connectHandler(conn -> {
      if (nbSSEConn.incrementAndGet() <= 1) {
        conn.close(); // force a reconnect on client side
      }
    });
    router.get(SSE_RECONNECT_ENDPOINT).handler(sseReconnectHandler);

    // Rejects one connect out of 2 by sending "No content"
    AtomicInteger nbConnections = new AtomicInteger(0);
    router.get(SSE_REJECT_ODDS).handler(rc -> {
      if (nbConnections.incrementAndGet() % 2 == 1) {
        rc.response().setStatusCode(204).end();
      } else {
        sseHandler.handle(rc);
      }
    });

    // Forwards messages from the event bus to an SSE connection
    SSEHandler sseEventBusHandler = SSEHandler.create();
    sseEventBusHandler.connectHandler(conn -> conn.forward(EB_ADDRESS));
    router.get(SSE_EVENTBUS_ENDPOINT).handler(sseEventBusHandler);
    SSEHandler sseMultipleMessages = SSEHandler.create();
    sseMultipleMessages.connectHandler(conn -> {
      conn.id("some-id");
      conn.data("some-data");
      vertx.setTimer(500, l -> conn.data("some-other-data-without-id"));
    });

    // Sends multiple times the same message to the connection periodically, incrementing its "id" every time a message is sent
    router.get(SSE_MULTIPLE_MESSAGES_ENDPOINT).handler(sseMultipleMessages);
    SSEHandler sseIds = SSEHandler.create();
    AtomicLong timerId = new AtomicLong(-1L);
    sseIds.connectHandler(conn -> {
      AtomicInteger idCounter = new AtomicInteger(0);
      if (conn.lastId() != null) {
        idCounter.set(Integer.parseInt(conn.lastId()));
      }
      AtomicInteger counterForConnection = new AtomicInteger(0);
      vertx.setPeriodic(150, id -> {
        if (counterForConnection.incrementAndGet() >= 3) {
          conn.close(); // client will reconnect automatically, hopefully using its last id
          return;
        }
        timerId.set(id);
        conn.id(Integer.toString(idCounter.incrementAndGet()));
        conn.data("some-data");
      });
      // clear the timer when the connection is closed
      conn.closeHandler(c -> {
        if (timerId.get() > 0) {
          vertx.cancelTimer(timerId.get());
        }
      });
    });
    router.get(SSE_ID_TEST_ENDPOINT).handler(sseIds);

    // Sends data in an SSE connection, splitting it in multiple lines
    router.get(SSE_MULTILINE_ENDPOINT).handler(
      SSEHandler.create().connectHandler(conn -> {
        String[] splitted = multilineMsg.split(" ");
        conn.data(splitted[0], true);
        for (int i = 1; i < splitted.length - 1; i++) {
          conn.data(" " + splitted[i], true);
        }
        conn.data(" " + splitted[splitted.length -1 ]);
      })
    );
  }

  // Mimics a very trivial `AuthHandler`. Only accepts connections if the expected "token" query param is sent
  private void protectEndpoint(RoutingContext rc) {
    HttpServerRequest request = rc.request();
    String token = request.getParam("token");
    if (token == null) {
      rc.fail(401);
    } else if (!TOKEN.equals(token)) {
      rc.fail(403);
    } else {
      rc.next();
    }
  }

}
