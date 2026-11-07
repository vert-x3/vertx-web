/*
 * Copyright 2019 Red Hat, Inc.
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

package io.vertx.ext.web.tests.handler.sockjs;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.Transport;
import io.vertx.junit5.Checkpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ben Ripkens
 */
public class SockJSAsyncHandlerTest extends SockJSTestBase {
  private static final String STREAMING_MESSAGE = "x".repeat(SockJSHandlerOptions.DEFAULT_MAX_BYTES_STREAMING);
  private static final String SHORT_MESSAGE = "Hello World";
  private AtomicBoolean connectionClosed;

  @BeforeEach
  @Override
  public void setUp(Vertx vertx) throws Exception {
    // Use two servers so we test with HTTP request/response with load balanced SockJSSession access
    numServers = 2;
    preSockJSHandlerSetup = router -> {
      router.route().handler(BodyHandler.create());
      // simulate an async handler
      router.route().handler(rtx -> rtx.vertx().executeBlocking(() -> true).onComplete(r -> rtx.next()));
    };
    super.setUp(vertx);
  }

  @AfterEach
  void afterEach() {
    assertConnectionStillOpen();
  }

  @Test
  void testHandleMessageFromXhrPostTransportWithAsyncHandler(Checkpoint cp) throws Exception {
    setupHandler(cp, false, Transport.XHR);

    webClient.post("/test/400/8ne8e94a/xhr")
      .sendBuffer(Buffer.buffer())
      .expecting(HttpResponseExpectation.SC_OK)
      .await();

    assertConnectionStillOpen();

    webClient.post("/test/400/8ne8e94a/xhr_send")
      .sendBuffer(Buffer.buffer("\"" + SHORT_MESSAGE + "\""))
      .expecting(HttpResponseExpectation.SC_NO_CONTENT)
      .await();
  }

  @Test
  void testHandleMessageFromJsonpTransportWithAsyncHandler(Checkpoint cp) throws Exception {
    setupHandler(cp, false, Transport.JSON_P);

    HttpResponse<Buffer> resp1 = webClient.get("/test/400/8ne8e94a/jsonp?callback=123")
      .sendBuffer(Buffer.buffer())
      .expecting(HttpResponseExpectation.SC_OK)
      .await();
    assertThat(resp1.bodyAsString()).isEqualTo("/**/123(\"o\");\r\n");

    assertConnectionStillOpen();

    HttpResponse<Buffer> resp2 = webClient.post("/test/400/8ne8e94a/jsonp_send")
      .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
      .sendBuffer(Buffer.buffer("\"" + SHORT_MESSAGE + "\""))
      .expecting(HttpResponseExpectation.SC_OK)
      .await();
    assertThat(resp2.bodyAsString()).isEqualTo("ok");
  }

  @Test
  void testHandleMessageFromXhrStreamingTransportWithAsyncHandler(Checkpoint cp) throws Exception {
    // Per the spec, the transport must first send 2KiB of `h` bytes as prelude.
    var openFrame = "h".repeat(2048) + "\no\n";
    testStreamingTransport(cp, Transport.XHR, HttpMethod.POST, "/test/400/8ne8e94a/xhr_streaming", openFrame,
      resp -> assertThat(resp.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/javascript; charset=UTF-8"),
      openBody -> assertThat(openBody).isEqualTo(openFrame),
      finalBody -> assertThat(finalBody).isEqualTo(openFrame + "a[\"" + STREAMING_MESSAGE + "\"]\n"));
  }

  @Test
  void testHandleMessageFromEventSourceTransportWithAsyncHandler(Checkpoint cp) throws Exception {
    var openFrame = "\r\ndata: o\r\n\r\n";
    testStreamingTransport(cp, Transport.EVENT_SOURCE, HttpMethod.GET, "/test/400/8ne8e94a/eventsource", openFrame,
      resp -> assertThat(resp.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/event-stream"),
      openBody -> assertThat(openBody).isEqualTo(openFrame),
      finalBody -> assertThat(finalBody).isEqualTo(openFrame + "data: a[\"" + STREAMING_MESSAGE + "\"]\r\n\r\n"));
  }

  @Test
  void testHandleMessageFromHtmlFileTransportWithAsyncHandler(Checkpoint cp) throws Exception {
    var openFrame = "<script>\np(\"o\");\n</script>\r\n";
    testStreamingTransport(cp, Transport.HTML_FILE, HttpMethod.GET, "/test/400/8ne8e94a/htmlfile?c=%63allback", openFrame,
      resp -> assertThat(resp.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo("text/html; charset=UTF-8"),
      openBody -> assertThat(openBody).hasSizeGreaterThan(1024).contains("Don't panic!").contains(openFrame),
      finalBody -> assertThat(finalBody).contains("<script>\np(\"a[\\\"" + STREAMING_MESSAGE + "\\\"]\");\n</script>\r\n"));
  }

  /**
   * Opens a streaming transport, sends a message that reaches max bytes streaming via xhr_send.
   */
  private void testStreamingTransport(Checkpoint cp, Transport transport, HttpMethod method, String url, String openFrame,
                                      Consumer<HttpClientResponse> responseAssertions,
                                      Consumer<String> openBodyAssertions,
                                      Consumer<String> finalBodyAssertions) throws Exception {

    // Streaming transports need xhr_send to send messages to the server
    setupHandler(cp, true, transport, Transport.XHR);
    var socketOpenPromise = Promise.<String>promise();
    var socketEndedPromise = Promise.<String>promise();
    var body = Buffer.buffer();

    var resp1 = client.request(method, url)
      .compose(HttpClientRequest::send)
      .expecting(HttpResponseExpectation.SC_OK)
      .map(resp -> {
        resp.exceptionHandler(t -> {
          socketOpenPromise.tryFail(t);
          socketEndedPromise.tryFail(t);
        });
        resp.endHandler(v -> {
          socketOpenPromise.tryFail(transport + " stream ended before the open frame");
          socketEndedPromise.tryComplete(body.toString());
        });
        resp.handler(chunk -> {
          body.appendBuffer(chunk);
          if (body.toString().contains(openFrame)) {
            socketOpenPromise.tryComplete(body.toString());
          }
        });
        return resp;
      })
      .await();
    responseAssertions.accept(resp1);
    openBodyAssertions.accept(socketOpenPromise.future().await(10, TimeUnit.SECONDS));

    assertConnectionStillOpen();

    var resp2 = webClient.post("/test/400/8ne8e94a/xhr_send")
      .sendBuffer(Buffer.buffer("[\"" + SHORT_MESSAGE + "\"]"))
      .expecting(HttpResponseExpectation.SC_NO_CONTENT)
      .await();
    assertThat(resp2.bodyAsString()).isNullOrEmpty();

    // The echoed frame reaches max bytes streaming, so the server must end the stream
    finalBodyAssertions.accept(socketEndedPromise.future().await(10, TimeUnit.SECONDS));
  }

  private void setupHandler(Checkpoint cp, boolean writeResponse, Transport... enabledTransports) throws Exception {
    connectionClosed = new AtomicBoolean(false);
    socketHandler = () -> socket -> {
      socket.routingContext().request().connection().closeHandler(v -> connectionClosed.set(true));
      socket.handler(buf -> {
        assertThat(buf).hasToString(SHORT_MESSAGE);
        if (writeResponse) {
          socket.write(STREAMING_MESSAGE).onFailure(cp::fail).onSuccess(v -> cp.flag());
        } else {
          cp.flag();
        }
      });
    };

    var options = new SockJSHandlerOptions().setHeartbeatInterval(30_000);
    for (var t : Transport.values()) {
      if (!Arrays.asList(enabledTransports).contains(t)) {
        options.addDisabledTransport(t.name());
      }
    }

    startServers(options);
  }

  private void assertConnectionStillOpen() {
    if (keepAlive) {
      assertThat(connectionClosed.get()).as("connection should still be open").isFalse();
    }
  }
}
