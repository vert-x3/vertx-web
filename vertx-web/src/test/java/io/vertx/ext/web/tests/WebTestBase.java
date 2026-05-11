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
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

@VertxTest
public abstract class WebTestBase {

  protected static Set<HttpMethod> METHODS = new HashSet<>(Arrays.asList(HttpMethod.DELETE, HttpMethod.GET,
    HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.OPTIONS, HttpMethod.TRACE, HttpMethod.POST, HttpMethod.PUT));

  protected Vertx vertx;
  protected HttpServer server;
  protected HttpClient client;
  protected WebSocketClient wsClient;
  protected WebClient webClient;
  protected Router router;

  @BeforeEach
  public void setUp(Vertx vertx) throws Exception {
    this.vertx = vertx;
    router = Router.router(vertx);
    server = vertx.createHttpServer(getHttpServerOptions().setMaxFormFields(2048));
    client = vertx.createHttpClient(getHttpClientOptions());
    webClient = WebClient.wrap(client);
    wsClient = vertx.createWebSocketClient(getWebSocketClientOptions());
    server
      .requestHandler(router)
      .listen()
      .await();
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
  public void tearDown() throws Exception {
    if (client != null) {
      client.close().await();
    }
    if (server != null) {
      server.close().await();
    }
  }

  protected HttpResponse<Buffer> testRequest(HttpMethod method, String path, HttpResponseStatus statusCode) {
    return testRequest(webClient.request(method, path), statusCode.code(), statusCode.reasonPhrase());
  }

  protected HttpResponse<Buffer> testRequest(HttpMethod method, String path, int statusCode, String statusMessage) {
    return testRequest(webClient.request(method, path), statusCode, statusMessage);
  }

  protected HttpResponse<Buffer> testRequest(HttpMethod method, String path, int statusCode, String statusMessage,
                             String responseBody) {
    return testRequest(webClient.request(method, path), statusCode, statusMessage, responseBody);
  }

  protected HttpResponse<Buffer> testRequestWithContentType(HttpMethod method, String path, String contentType, int statusCode, String statusMessage) throws Exception {
    return testRequest(webClient.request(method, path).putHeader("content-type", contentType), statusCode, statusMessage);
  }

  protected void testRequestWithAccepts(HttpMethod method, String path, String accepts, int statusCode, String statusMessage) {
    testRequest(webClient.request(method, path).putHeader("accept", accepts), statusCode, statusMessage);
  }

  protected void testRequestWithCookies(HttpMethod method, String path, String cookieHeader, int statusCode, String statusMessage) {
    testRequest(webClient.request(method, path).putHeader("cookie", cookieHeader), statusCode, statusMessage);
  }

  protected void testRequest(RequestOptions requestOptions, int statusCode, String statusMessage, String responseBody) {
    testRequest(webClient.request(requestOptions), statusCode, statusMessage, responseBody);
  }

  protected HttpResponse<Buffer> testRequest(HttpRequest<Buffer> request, int statusCode, String statusMessage) {
    return testRequest(request, statusCode, statusMessage, null);
  }

  protected HttpResponse<Buffer> testRequest(HttpRequest<Buffer> request, int statusCode, String statusMessage, String responseBody) {
    return testRequest(request.send(), statusCode, statusMessage, responseBody);
  }

  protected HttpResponse<Buffer> testRequest(Future<HttpResponse<Buffer>> request, int statusCode, String statusMessage) {
    return testRequest(request, statusCode, statusMessage, null);
  }

  protected HttpResponse<Buffer> testRequest(Future<HttpResponse<Buffer>> request, int statusCode, String statusMessage, String responseBody) {
    HttpResponse<Buffer> response = request.await();
    assertEquals(statusCode, response.statusCode());
    assertEquals(statusMessage, response.statusMessage());
    if (responseBody != null) {
      assertEquals(Buffer.buffer(responseBody), response.body());
    }
    return response;
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
