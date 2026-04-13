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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.test.core.VertxTestBase;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class WebTestBase extends VertxTestBase {

  protected static Set<HttpMethod> METHODS = new HashSet<>(Arrays.asList(HttpMethod.DELETE, HttpMethod.GET,
    HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.OPTIONS, HttpMethod.TRACE, HttpMethod.POST, HttpMethod.PUT));

  protected HttpServer server;
  protected HttpClient client;
  protected WebSocketClient wsClient;
  protected WebClient webClient;
  protected Router router;

  public WebTestBase() {
  }

  public WebTestBase(ReportMode reportMode) {
    super(reportMode);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    router = Router.router(vertx);
    server = vertx.createHttpServer(getHttpServerOptions().setMaxFormFields(2048));
    client = vertx.createHttpClient(getHttpClientOptions());
    webClient = WebClient.wrap(client);
    wsClient = vertx.createWebSocketClient(getWebSocketClientOptions());
    server
      .requestHandler(router)
      .listen()
      .await(20, TimeUnit.SECONDS);
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

  @Override
  public void tearDown() throws Exception {
    if (client != null) {
      client
        .close()
        .await(20, TimeUnit.SECONDS);
    }
    if (server != null) {
      server
        .close()
        .await(20, TimeUnit.SECONDS);
    }
    super.tearDown();
  }

  protected void testRequest(HttpMethod method, String path, HttpResponseStatus statusCode) throws Exception {
    testRequest(webClient.request(method, path), statusCode.code(), statusCode.reasonPhrase());
  }

  protected void testRequest(HttpMethod method, String path, int statusCode, String statusMessage) throws Exception {
    testRequest(webClient.request(method, path), statusCode, statusMessage);
  }

  protected void testRequest(HttpMethod method, String path, int statusCode, String statusMessage,
                             String responseBody) throws Exception {
    testRequest(webClient.request(method, path), statusCode, statusMessage, responseBody);
  }

  protected HttpResponse<Buffer> testRequestWithContentType(HttpMethod method, String path, String contentType, int statusCode, String statusMessage) throws Exception {
    return testRequest(webClient.request(method, path).putHeader("content-type", contentType), statusCode, statusMessage);
  }

  protected void testRequestWithAccepts(HttpMethod method, String path, String accepts, int statusCode, String statusMessage) throws Exception {
    testRequest(webClient.request(method, path).putHeader("accept", accepts), statusCode, statusMessage);
  }

  protected void testRequestWithCookies(HttpMethod method, String path, String cookieHeader, int statusCode, String statusMessage) throws Exception {
    testRequest(webClient.request(method, path).putHeader("cookie", cookieHeader), statusCode, statusMessage);
  }

  protected void testRequest(RequestOptions requestOptions, int statusCode, String statusMessage, String responseBody) throws Exception {
    testRequest(webClient.request(requestOptions), statusCode, statusMessage, responseBody);
  }

  protected HttpResponse<Buffer> testRequest(HttpRequest<Buffer> request, int statusCode, String statusMessage) throws Exception {
    return testRequest(request, statusCode, statusMessage, null);
  }

  protected HttpResponse<Buffer> testRequest(HttpRequest<Buffer> request, int statusCode, String statusMessage, String responseBody) throws Exception {
    return testRequest(request.send(), statusCode, statusMessage, responseBody);
  }

  protected HttpResponse<Buffer> testRequest(Future<HttpResponse<Buffer>> request, int statusCode, String statusMessage) throws Exception {
    return testRequest(request, statusCode, statusMessage, null);
  }

  protected HttpResponse<Buffer> testRequest(Future<HttpResponse<Buffer>> request, int statusCode, String statusMessage, String responseBody) throws Exception {
    HttpResponse<Buffer> response = request.await();
    Assert.assertEquals(statusCode, response.statusCode());
    Assert.assertEquals(statusMessage, response.statusMessage());
    if (responseBody != null) {
      Assert.assertEquals(Buffer.buffer(responseBody), response.body());
    }
    return response;
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
