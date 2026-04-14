/*
 * Copyright 2017 Red Hat, Inc.
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

package io.vertx.ext.web.tests.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.tests.WebTestBase2;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author Thomas Segismont
 */
public class ResponseContentTypeHandlerTest extends WebTestBase2 {

  private Route testRoute;

  @Override
  @BeforeEach
  public void setUp(Vertx vertx, VertxTestContext testContext) throws Exception {
    super.setUp(vertx, testContext);
    router.route().handler(ResponseContentTypeHandler.create());
    // Added to make sure ResponseContentTypeHandler works well with others
    router.route().handler(ResponseTimeHandler.create());
    testRoute = router.route("/test");
  }

  @Test
  public void testNoMatch() {
    testRoute.handler(rc -> rc.response().end());
    HttpResponse<Buffer> resp = webClient.get(testRoute.getPath())
      .putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
      .send()
      .expecting(HttpResponseExpectation.SC_OK)
      .await();
    assertNull(contentType(resp));
  }

  @Test
  public void testExistingHeader() {
    testRoute.produces("application/json").handler(rc -> rc.response().putHeader(CONTENT_TYPE, "text/plain").end());
    HttpResponse<Buffer> resp = webClient.get(testRoute.getPath())
      .putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
      .send()
      .expecting(HttpResponseExpectation.SC_OK)
      .await();
    assertEquals("text/plain", contentType(resp));
  }

  @Test
  public void testFixedContent() {
    Buffer buffer = new JsonObject().put("toto", "titi").toBuffer();
    testRoute.produces("application/json").handler(rc -> rc.response().end(buffer));
    HttpResponse<Buffer> resp = webClient.get(testRoute.getPath())
      .putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
      .send()
      .expecting(HttpResponseExpectation.SC_OK)
      .expecting(HttpResponseExpectation.contentType("application/json"))
      .await();
    assertEquals(Integer.valueOf(buffer.length()), contentLength(resp));
    assertEquals(buffer, resp.body());
  }

  @Test
  public void testChunkedContent() {
    Buffer buffer = new JsonObject().put("toto", "titi").toBuffer();
    testRoute.produces("application/json").handler(rc -> rc.response().setChunked(true).end(buffer));
    HttpResponse<Buffer> resp = webClient.get(testRoute.getPath())
      .putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
      .send()
      .expecting(HttpResponseExpectation.SC_OK)
      .await();
    assertNull(contentLength(resp));
    assertEquals(buffer, resp.body());
  }

  @Test
  public void testNoContent() {
    testRoute.produces("application/json").handler(rc -> rc.response().end());
    HttpResponse<Buffer> resp = webClient.get(testRoute.getPath())
      .putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
      .send()
      .expecting(HttpResponseExpectation.SC_OK)
      .await();
    assertNull(contentType(resp));
    assertEquals(Integer.valueOf(0), contentLength(resp));
  }

  @Test
  public void testDisableFlag() {
    Random random = new Random();
    byte[] bytes = new byte[128];
    random.nextBytes(bytes);
    Buffer buffer = Buffer.buffer(bytes);
    testRoute.produces("application/json").handler(rc -> {
      rc.put(ResponseContentTypeHandler.DEFAULT_DISABLE_FLAG, true);
      rc.response().end(buffer);
    });
    HttpResponse<Buffer> resp = webClient.get(testRoute.getPath())
      .putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
      .send()
      .expecting(HttpResponseExpectation.SC_OK)
      .await();
    assertNull(contentType(resp));
    assertEquals(Integer.valueOf(buffer.length()), contentLength(resp));
    assertEquals(buffer, resp.body());
  }

  @Test
  public void testProducesOrderGuaranteeOnWildcardHeader() {
    testRoute.produces("application/json").produces("text/plain")
      .handler(rc -> rc.response().putHeader(CONTENT_TYPE, rc.getAcceptableContentType()).end());

    HttpResponse<Buffer> resp = webClient.get(testRoute.getPath())
      .putHeader(HttpHeaders.ACCEPT.toString(), "*/*")
      .send()
      .expecting(HttpResponseExpectation.SC_OK)
      .expecting(HttpResponseExpectation.contentType("application/json"))
      .await();
  }

  private String contentType(HttpResponse<Buffer> resp) {
    return resp.getHeader(CONTENT_TYPE.toString());
  }

  private Integer contentLength(HttpResponse<Buffer> resp) {
    String header = resp.getHeader(CONTENT_LENGTH.toString());
    return header == null ? null : Integer.parseInt(header);
  }

}
