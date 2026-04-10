/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.tests;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@VertxTest
public class WebTestBase2 {

  protected static Set<HttpMethod> METHODS = new HashSet<>(Arrays.asList(HttpMethod.DELETE, HttpMethod.GET,
    HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.OPTIONS, HttpMethod.TRACE, HttpMethod.POST, HttpMethod.PUT));

  protected Vertx vertx;
  protected HttpServer server;
  protected HttpClient client;
  protected WebSocketClient wsClient;
  protected Router router;

  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) {
    this.vertx = vertx;
    router = Router.router(vertx);
    server = vertx.createHttpServer(getHttpServerOptions().setMaxFormFields(2048));
    client = vertx.createHttpClient(getHttpClientOptions());
    wsClient = vertx.createWebSocketClient(getWebSocketClientOptions());
    server
      .requestHandler(router)
      .listen()
      .onComplete(testContext.succeedingThenComplete());
  }

  protected HttpServerOptions getHttpServerOptions() {
    return new HttpServerOptions().setPort(8080).setHost("localhost");
  }

  protected HttpClientOptions getHttpClientOptions() {
    return new HttpClientOptions().setDefaultPort(8080);
  }

  protected WebSocketClientOptions getWebSocketClientOptions() {
    return new WebSocketClientOptions().setDefaultPort(8080);
  }

  @AfterEach
  public void tearDown(VertxTestContext testContext) {
    if (client != null) {
      client.close().onComplete(ar -> {
        if (server != null) {
          server.close().onComplete(testContext.succeedingThenComplete());
        } else {
          testContext.completeNow();
        }
      });
    } else if (server != null) {
      server.close().onComplete(testContext.succeedingThenComplete());
    } else {
      testContext.completeNow();
    }
  }

  protected void testRequest(RequestOptions requestOptions, HttpResponseStatus statusCode) {
    testRequest(requestOptions, null, statusCode.code(), statusCode.reasonPhrase(), null);
  }

  protected void testRequest(HttpMethod method, String path, HttpResponseStatus statusCode) {
    testRequest(method, path, null, statusCode.code(), statusCode.reasonPhrase(), null);
  }

  protected void testRequest(RequestOptions requestOptions, int statusCode, String statusMessage) {
    testRequest(requestOptions, null, statusCode, statusMessage, null);
  }

  protected void testRequest(HttpMethod method, String path, int statusCode, String statusMessage) {
    testRequest(method, path, null, statusCode, statusMessage, null);
  }

  protected void testRequest(RequestOptions requestOptions, int statusCode, String statusMessage,
                             String responseBody) {
    testRequest(requestOptions, null, statusCode, statusMessage, responseBody);
  }

  protected void testRequest(HttpMethod method, String path, int statusCode, String statusMessage,
                             String responseBody) {
    testRequest(method, path, null, statusCode, statusMessage, responseBody);
  }

  protected void testRequest(RequestOptions requestOptions, int statusCode, String statusMessage,
                             Buffer responseBody) {
    testRequestBuffer(requestOptions, null, null, statusCode, statusMessage, responseBody);
  }

  protected void testRequest(HttpMethod method, String path, int statusCode, String statusMessage,
                             Buffer responseBody) {
    testRequestBuffer(method, path, null, null, statusCode, statusMessage, responseBody);
  }

  protected void testRequestWithContentType(RequestOptions requestOptions, String contentType, int statusCode, String statusMessage) {
    testRequest(requestOptions, req -> req.putHeader("content-type", contentType), statusCode, statusMessage, null);
  }

  protected void testRequestWithContentType(HttpMethod method, String path, String contentType, int statusCode, String statusMessage) {
    testRequest(method, path, req -> req.putHeader("content-type", contentType), statusCode, statusMessage, null);
  }

  protected void testRequestWithContentType(HttpMethod method, String path, String contentType, int statusCode, String statusMessage, Consumer<HttpClientResponse> responseAction) {
    testRequest(method, path, req -> req.putHeader("content-type", contentType), responseAction, statusCode, statusMessage, null);
  }

  protected void testRequestWithAccepts(RequestOptions requestOptions, String accepts, int statusCode, String statusMessage) {
    testRequest(requestOptions, req -> req.putHeader("accept", accepts), statusCode, statusMessage, null);
  }

  protected void testRequestWithAccepts(HttpMethod method, String path, String accepts, int statusCode, String statusMessage) {
    testRequest(method, path, req -> req.putHeader("accept", accepts), statusCode, statusMessage, null);
  }

  protected void testRequestWithCookies(RequestOptions requestOptions, String cookieHeader, int statusCode, String statusMessage) {
    testRequest(requestOptions, req -> req.putHeader("cookie", cookieHeader), statusCode, statusMessage, null);
  }

  protected void testRequestWithCookies(HttpMethod method, String path, String cookieHeader, int statusCode, String statusMessage) {
    testRequest(method, path, req -> req.putHeader("cookie", cookieHeader), statusCode, statusMessage, null);
  }

  protected void testRequest(RequestOptions requestOptions, Consumer<HttpClientRequest> requestAction,
                             int statusCode, String statusMessage,
                             String responseBody) {
    testRequest(requestOptions, requestAction, null, statusCode, statusMessage, responseBody);
  }

  protected void testRequest(HttpMethod method, String path, Consumer<HttpClientRequest> requestAction,
                             int statusCode, String statusMessage,
                             String responseBody) {
    testRequest(method, path, requestAction, null, statusCode, statusMessage, responseBody);
  }

  protected void testRequest(RequestOptions requestOptions, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                             int statusCode, String statusMessage,
                             String responseBody) {
    testRequestBuffer(requestOptions, requestAction, responseAction, statusCode, statusMessage, responseBody != null ? Buffer.buffer(responseBody) : null, true);
  }

  protected void testRequest(HttpMethod method, String path, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                             int statusCode, String statusMessage,
                             String responseBody) {
    testRequestBuffer(method, path, requestAction, responseAction, statusCode, statusMessage, responseBody != null ? Buffer.buffer(responseBody) : null, true);
  }

  protected void testRequestBuffer(RequestOptions requestOptions, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                                   int statusCode, String statusMessage,
                                   Buffer responseBodyBuffer) {
    testRequestBuffer(requestOptions, requestAction, responseAction, statusCode, statusMessage, responseBodyBuffer, false);
  }

  protected void testRequestBuffer(HttpMethod method, String path, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                                   int statusCode, String statusMessage,
                                   Buffer responseBodyBuffer) {
    testRequestBuffer(method, path, requestAction, responseAction, statusCode, statusMessage, responseBodyBuffer, false);
  }

  protected void testRequestBuffer(RequestOptions requestOptions, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                                   int statusCode, String statusMessage,
                                   Buffer responseBodyBuffer, boolean normalizeLineEndings) {
    testRequestBuffer(client, requestOptions, requestAction, responseAction, statusCode, statusMessage, responseBodyBuffer, normalizeLineEndings);
  }

  protected void testRequestBuffer(HttpMethod method, String path, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                                   int statusCode, String statusMessage,
                                   Buffer responseBodyBuffer, boolean normalizeLineEndings) {
    testRequestBuffer(client, method, 8080, path, requestAction, responseAction, statusCode, statusMessage, responseBodyBuffer, normalizeLineEndings);
  }

  protected void testRequestBuffer(HttpClient client, RequestOptions requestOptions, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                                   int statusCode, String statusMessage,
                                   Buffer responseBodyBuffer) {
    testRequestBuffer(client, requestOptions, requestAction, responseAction, statusCode, statusMessage, responseBodyBuffer, false);
  }

  protected void testRequestBuffer(HttpClient client, HttpMethod method, int port, String path, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                                   int statusCode, String statusMessage,
                                   Buffer responseBodyBuffer) {
    testRequestBuffer(client, method, port, path, requestAction, responseAction, statusCode, statusMessage, responseBodyBuffer, false);
  }

  protected void testRequestBuffer(HttpClient client, HttpMethod method, int port, String path, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                                   int statusCode, String statusMessage,
                                   Buffer responseBodyBuffer, boolean normalizeLineEndings) {
    testRequestBuffer(client, new RequestOptions().setMethod(method).setPort(port).setURI(path).setHost("localhost"), requestAction, responseAction, statusCode, statusMessage, responseBodyBuffer, normalizeLineEndings);
  }

  protected void testRequestBuffer(HttpClient client, RequestOptions requestOptions, Consumer<HttpClientRequest> requestAction, Consumer<HttpClientResponse> responseAction,
                                   int statusCode, String statusMessage,
                                   Buffer responseBodyBuffer, boolean normalizeLineEndings) {
    Future<Buffer> bodyFuture = client.request(requestOptions)
      .compose(request  -> {

        if (requestAction != null) {
          requestAction.accept(request);
        }

        request.end();

        Future<HttpClientResponse> responseFuture = request.response()
          .expecting(HttpResponseExpectation.status(statusCode))
          .expecting(response -> response.statusMessage().equals(statusMessage));

        if (responseAction != null) {
          responseFuture = responseFuture
            .andThen(ar -> {
              if (ar.succeeded()) {
                responseAction.accept(ar.result());
              }
          });
        }

        return responseFuture.compose(HttpClientResponse::body);

      });

    Buffer body = bodyFuture.await();

    if (responseBodyBuffer != null) {
      if (normalizeLineEndings) {
        body = normalizeLineEndingsFor(body);
      }
      assertEquals(responseBodyBuffer, body);
    }
  }

  protected static Buffer normalizeLineEndingsFor(Buffer buff) {
    int buffLen = buff.length();
    Buffer normalized = Buffer.buffer(buffLen);
    for (int i = 0; i < buffLen; i++) {
      short unsignedByte = buff.getUnsignedByte(i);
      if (unsignedByte != '\r' || i + 1 == buffLen || buff.getUnsignedByte(i + 1) != '\n') {
        normalized.appendUnsignedByte(unsignedByte);
      }
    }
    return normalized;
  }

  protected void testSyncRequest(String httpMethod, String path, int statusCode, String statusMessage, String responseBody) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:" + this.server.actualPort() + path).openConnection();
    connection.setRequestMethod(httpMethod);

    assertEquals(statusCode, connection.getResponseCode());
    if (connection.getResponseCode() < 400) {
      assertEquals(statusMessage, connection.getResponseMessage());

      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      assertEquals(responseBody, response.toString());
    } else {
      assertEquals(statusMessage, connection.getResponseMessage());

      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
      String inputLine;
      StringBuilder response = new StringBuilder();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();

      assertEquals(responseBody, response.toString());
    }
  }

  // Todo : move that to TestUtils
  protected static void assertWaitUntil(java.util.function.BooleanSupplier supplier) {
    assertWaitUntil(supplier, 10_000);
  }

  // Todo : move that to TestUtils
  protected static void assertWaitUntil(java.util.function.BooleanSupplier supplier, long timeout) {
    long start = System.currentTimeMillis();
    do {
      if (supplier.getAsBoolean()) {
        return;
      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    } while (System.currentTimeMillis() - start < timeout);
    fail("Timed out");
  }

  /**
   * Cleanup upload files left by {@link BodyHandler} at location {@link BodyHandler#DEFAULT_UPLOADS_DIRECTORY}
   */
  public static void cleanupFileUploadDir() throws IOException {
    File f = new File(BodyHandler.DEFAULT_UPLOADS_DIRECTORY);
    if (f.exists() && f.isDirectory()) {
      Files.walk(f.toPath())
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
    }
  }
}
