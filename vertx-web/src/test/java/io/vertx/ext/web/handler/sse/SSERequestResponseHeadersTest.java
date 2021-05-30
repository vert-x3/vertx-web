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

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class SSERequestResponseHeadersTest extends SSEBaseTest {

  @Test
  public void noHeaderTextEventStreamHttpRequest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    MultiMap headers = MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.ACCEPT, "foo");

    request(client(), headers)
      .onComplete(ar -> {
        assertTrue(ar.succeeded());
        assertEquals(406, ar.result().statusCode());
        latch.countDown();
      });
    awaitLatch(latch);
  }

  @Test
  public void noHeaderHttpRequest() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    request(client(), null)
      .onComplete(ar -> {
        assertTrue(ar.succeeded());
        assertSSEHeaders(ar.result());
        latch.countDown();
      });
    awaitLatch(latch);
  }

  @Test
  public void correctResponseHeaders() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    MultiMap headers = MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.ACCEPT, "text/event-stream");

    request(client(), headers)
      .onComplete(ar -> {
        assertTrue(ar.succeeded());
        assertSSEHeaders(ar.result());
        latch.countDown();
      });
    awaitLatch(latch);
  }

  private void assertSSEHeaders(HttpClientResponse response) {
    assertEquals("text/event-stream", response.getHeader(HttpHeaders.CONTENT_TYPE));
    assertEquals("no-cache", response.getHeader(HttpHeaders.CACHE_CONTROL));
    assertEquals("keep-alive", response.getHeader(HttpHeaders.CONNECTION));
  }

  protected Future<HttpClientResponse> request(final HttpClient client, final MultiMap headers) {
    RequestOptions options = new RequestOptions()
      .setMethod(HttpMethod.GET)
      .setURI(String.format("%s?token=%s", SSE_ENDPOINT, TOKEN));

    if (headers != null) {
      options.setHeaders(headers);
    }

    return client.request(options)
      .compose(HttpClientRequest::send);
  }

}
