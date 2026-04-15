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

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Ben Ripkens
 */
public class SockJSAsyncHandlerTest extends SockJSTestBase {

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

  @Test
  public void testHandleMessageFromXhrTransportWithAsyncHandler(VertxTestContext testContext) throws Exception {
    Checkpoint cp = testContext.checkpoint();
    socketHandler = () -> socket -> {
      socket.handler(buf -> {
        assertEquals("Hello World", buf.toString());
        cp.flag();
      });
    };

    startServers();

    HttpResponse<Buffer> resp1 = webClient.post("/test/400/8ne8e94a/xhr")
      .sendBuffer(Buffer.buffer())
      .await();
    assertEquals(200, resp1.statusCode());

    HttpResponse<Buffer> resp2 = webClient.post("/test/400/8ne8e94a/xhr_send")
      .sendBuffer(Buffer.buffer("\"Hello World\""))
      .await();
    assertEquals(204, resp2.statusCode());
  }
}
