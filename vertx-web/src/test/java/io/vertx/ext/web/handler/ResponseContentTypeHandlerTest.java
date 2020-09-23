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

package io.vertx.ext.web.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.util.Random;

import static io.vertx.core.http.HttpHeaders.CONTENT_LENGTH;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author Thomas Segismont
 */
public class ResponseContentTypeHandlerTest extends WebTestBase {

  private Route testRoute;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    router.route().handler(ResponseContentTypeHandler.create());
    // Added to make sure ResponseContentTypeHandler works well with others
    router.route().handler(ResponseTimeHandler.create());
    testRoute = router.route("/test");
  }

  @Test
  public void testNoMatch() {
    testRoute.handler(rc -> rc.response().end());
    client.request(HttpMethod.GET, testRoute.getPath())
      .compose(req -> req
        .putHeader(HttpHeaders.ACCEPT, "application/json")
        .send())
      .onComplete(onSuccess(resp -> {
        assertNull(contentType(resp));
        testComplete();
      }));
    await();
  }

  @Test
  public void testExistingHeader() {
    testRoute.produces("application/json").handler(rc -> rc.response().putHeader(CONTENT_TYPE, "text/plain").end());
    client.request(HttpMethod.GET, testRoute.getPath())
      .compose(req -> req
        .putHeader(HttpHeaders.ACCEPT, "application/json")
        .send())
      .onComplete(onSuccess(resp -> {
        assertEquals("text/plain", contentType(resp));
        testComplete();
      }));
    await();
  }

  @Test
  public void testFixedContent() {
    Buffer buffer = new JsonObject().put("toto", "titi").toBuffer();
    testRoute.produces("application/json").handler(rc -> rc.response().end(buffer));
    client.request(HttpMethod.GET, testRoute.getPath())
      .compose(req -> req
        .putHeader(HttpHeaders.ACCEPT, "application/json")
        .send().compose(resp -> {
          assertEquals("application/json", contentType(resp));
          assertEquals(Integer.valueOf(buffer.length()), contentLength(resp));
          return resp.body();
        })
      ).onComplete(onSuccess(buf -> {
      assertEquals(buffer, buf);
      testComplete();
    }));
    await();
  }

  @Test
  public void testChunkedContent() {
    Buffer buffer = new JsonObject().put("toto", "titi").toBuffer();
    testRoute.produces("application/json").handler(rc -> rc.response().setChunked(true).end(buffer));
    client.request(HttpMethod.GET, testRoute.getPath())
      .compose(req -> req
        .putHeader(HttpHeaders.ACCEPT, "application/json")
        .send().compose(resp -> {
          assertNull(contentLength(resp));
          return resp.body();
        })
      ).onComplete(onSuccess(buf -> {
      assertEquals(buffer, buf);
      testComplete();
    }));
    await();
  }

  @Test
  public void testNoContent() {
    testRoute.produces("application/json").handler(rc -> rc.response().end());
    client.request(HttpMethod.GET, testRoute.getPath())
      .compose(req -> req
        .putHeader(HttpHeaders.ACCEPT, "application/json")
        .send()
      ).onComplete(onSuccess(resp -> {
      assertNull(contentType(resp));
      assertEquals(Integer.valueOf(0), contentLength(resp));
      testComplete();
    }));
    await();
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
    client.request(HttpMethod.GET, testRoute.getPath())
      .compose(req -> req
        .putHeader(HttpHeaders.ACCEPT, "application/json")
        .send().compose(resp -> {
          assertNull(contentType(resp));
          assertEquals(Integer.valueOf(buffer.length()), contentLength(resp));
          return resp.body();
        })
      ).onComplete(onSuccess(buf -> {
      assertEquals(buffer, buf);
      testComplete();
    }));
    await();
  }

  @Test
  public void testProducesOrderGuaranteeOnWildcardHeader() {
    testRoute.produces("application/json").produces("text/plain")
      .handler(rc -> rc.response().putHeader(CONTENT_TYPE, rc.getAcceptableContentType()).end());

    client.request(HttpMethod.GET, testRoute.getPath())
      .compose(req -> req
        .putHeader(HttpHeaders.ACCEPT, "*/*")
        .send())
      .onComplete(onSuccess(resp -> {
        assertEquals("application/json", contentType(resp));
        testComplete();
      }));
    await();
  }

  private String contentType(HttpClientResponse resp) {
    return resp.getHeader(CONTENT_TYPE);
  }

  private Integer contentLength(HttpClientResponse resp) {
    String header = resp.getHeader(CONTENT_LENGTH);
    return header == null ? null : Integer.parseInt(header);
  }

}
