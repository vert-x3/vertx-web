/*
 * Copyright 2026 Red Hat, Inc.
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

package io.vertx.ext.web.tests.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpFrame;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.HostAndPort;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.AltSvcHandler;
import io.vertx.ext.web.handler.AltSvcOptions;
import io.vertx.ext.web.tests.WebTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AltSvcHandlerTest extends WebTestBase {

  @Test
  public void testAddsAltSvcHeaderForMatchingOrigin() {
    router.route().handler(
      AltSvcHandler.create(new AltSvcOptions().addOrigin("http://localhost:8080", "h3:localhost:8443")));
    router.route().handler(context -> context.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");

    assertEquals("h3=\"localhost:8443\"", resp.getHeader("Alt-Svc"));
  }

  @Test
  public void testAddsAltSvcHeaderOnlyOncePerConnectionForMatchingOrigin() {
    router.route().handler(
      AltSvcHandler.create(new AltSvcOptions().addOrigin("http://localhost:8080", "h3:localhost:8443")));
    router.route().handler(context -> context.response().end());

    HttpResponse<Buffer> first = testRequest(webClient.get("/").send(), 200, "OK");
    HttpResponse<Buffer> second = testRequest(webClient.get("/").send(), 200, "OK");
    HttpResponse<Buffer> third;

    HttpClient otherClient = vertx.createHttpClient(getHttpClientOptions());
    try {
      WebClient otherWebClient = WebClient.wrap(otherClient);
      third = testRequest(otherWebClient.get("/").send(), 200, "OK");
    } finally {
      otherClient.close().await();
    }

    assertEquals("h3=\"localhost:8443\"", first.getHeader("Alt-Svc"));
    assertNull(second.getHeader("Alt-Svc"));
    assertEquals("h3=\"localhost:8443\"", third.getHeader("Alt-Svc"));
  }

  @Test
  public void testWritesAltSvcFrameForHttp2() throws Exception {
    router.route().handler(
      AltSvcHandler.create(new AltSvcOptions().addOrigin("http://localhost:8080", "h3:localhost:8443")));
    router.route().handler(context -> context.response().end());

    client.close().await();
    client = vertx.createHttpClient(new HttpClientOptions()
      .setDefaultPort(8080)
      .setProtocolVersion(HttpVersion.HTTP_2)
      .setHttp2ClearTextUpgrade(false));

    HttpClientRequest request = client.request(HttpMethod.GET, 8080, "localhost", "/").await();
    Future<HttpClientResponse> responseFuture = request.response();
    request.end().await();

    HttpClientResponse response = responseFuture.await();
    Promise<HttpFrame> altSvcFrame = Promise.promise();
    response.customFrameHandler(frame -> {
      if (frame.type() == 10) {
        altSvcFrame.tryComplete(frame);
      }
    });
    response.body().await();

    HttpFrame frame = altSvcFrame.future().await(5, TimeUnit.SECONDS);
    Buffer payload = frame.payload();

    assertEquals(HttpVersion.HTTP_2, response.version());
    assertNull(response.getHeader("Alt-Svc"));
    assertEquals(10, frame.type());
    assertEquals(0, frame.flags());
    assertEquals(0, payload.getShort(0));
    assertEquals("h3=\"localhost:8443\"", payload.getString(2, payload.length()));
  }

  @Test
  public void testDoesNotAddAltSvcHeaderForUnmatchedOrigin() {
    router.route().handler(
      AltSvcHandler.create(new AltSvcOptions().addOrigin("http://example.com", "h3:example.com:8443")));
    router.route().handler(context -> context.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");

    assertNull(resp.getHeader("Alt-Svc"));
  }

  @Test
  public void testUsesAltSvcResponseApiForMatchingOrigin() {
    RoutingContext context = mock(RoutingContext.class);
    HttpServerRequest request = mock(HttpServerRequest.class);
    HttpServerResponse response = mock(HttpServerResponse.class);
    HttpConnection connection = mock(HttpConnection.class);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Handler<Void>> headersEndHandler = ArgumentCaptor.forClass(Handler.class);

    when(context.request()).thenReturn(request);
    when(context.response()).thenReturn(response);
    when(context.addHeadersEndHandler(headersEndHandler.capture())).thenReturn(1);
    when(request.scheme()).thenReturn("http");
    when(request.authority()).thenReturn(HostAndPort.create("localhost", 8080));
    when(request.connection()).thenReturn(connection);
    when(response.writeAltSvc(anyString())).thenReturn(Future.succeededFuture());

    AltSvcHandler.create(new AltSvcOptions().addOrigin("http://localhost:8080", "h3:localhost:8443"))
      .handle(context);
    headersEndHandler.getValue().handle(null);

    verify(response).writeAltSvc("h3=\"localhost:8443\"");
    verify(context).next();
  }

  @Test
  public void testOptionsJsonRoundTrip() {
    AltSvcOptions options = new AltSvcOptions()
      .addOrigin("http://localhost:8080", "h3:localhost:8443");

    JsonObject json = options.toJson();
    AltSvcOptions copy = new AltSvcOptions(json);

    assertEquals("h3:localhost:8443", json.getJsonObject("origins").getString("http://localhost:8080"));
    assertEquals(options.getOrigins(), copy.getOrigins());
  }
}
