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

package io.vertx.ext.web.handler.sockjs;

import io.vertx.ext.web.handler.BodyHandler;
import org.junit.Test;

/**
 * @author Ben Ripkens
 */
public class SockJSAsyncHandlerTest extends SockJSTestBase {

  @Override
  public void setUp() throws Exception {
    // Use two servers so we test with HTTP request/response with load balanced SockJSSession access
    numServers = 2;
    preSockJSHandlerSetup = router -> {
      router.route().handler(BodyHandler.create());
      // simulate an async handler
      router.route().handler(rtx -> rtx.vertx().executeBlocking(f -> f.complete(true), r -> rtx.next()));
    };
    super.setUp();
  }

  @Test
  public void testHandleMessageFromXhrTransportWithAsyncHandler() throws Exception {
    socketHandler = () -> {
      return socket -> {
        socket.handler(buf -> {
          assertEquals("Hello World", buf.toString());
          testComplete();
        });
      };
    };

    startServers();

    client.post("/test/400/8ne8e94a/xhr", onSuccess(resp -> {
      assertEquals(200, resp.statusCode());

      client.post("/test/400/8ne8e94a/xhr_send", onSuccess(respSend -> assertEquals(204, respSend.statusCode())))
        .putHeader("content-length", "13")
        .end("\"Hello World\"");
    })).end();

    await();
  }
}
