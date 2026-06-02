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
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.AltSvcHandler;
import io.vertx.ext.web.handler.AltSvcOptions;
import io.vertx.ext.web.tests.WebTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
  public void testDoesNotAddAltSvcHeaderForUnmatchedOrigin() {
    router.route().handler(
      AltSvcHandler.create(new AltSvcOptions().addOrigin("http://example.com", "h3:example.com:8443")));
    router.route().handler(context -> context.response().end());

    HttpResponse<Buffer> resp = testRequest(webClient.get("/").send(), 200, "OK");

    assertNull(resp.getHeader("Alt-Svc"));
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
