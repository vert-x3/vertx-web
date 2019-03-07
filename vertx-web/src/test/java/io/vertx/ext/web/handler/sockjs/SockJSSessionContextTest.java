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

import io.vertx.core.json.JsonArray;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.startsWith;

/**
 * @author Thomas Segismont
 */
public class SockJSSessionContextTest extends SockJSTestBase {

  @Override
  public void setUp() throws Exception {
    numServers = 2;
    super.setUp();
  }

  @Test
  public void testHandleMessageFromXhrTransportWithAsyncHandler() throws Exception {
    String msg = "Hello World";
    socketHandler = () -> {
      return socket -> {
        socket.handler(buffer -> {
          assertEquals(msg, buffer.toString());
          socket.write(buffer);
        });
      };
    };

    startServers();

    client.post("/test/400/8ne8e94a/xhr", onSuccess(resp -> {
      assertEquals(200, resp.statusCode());

      client.post("/test/400/8ne8e94a/xhr", onSuccess(resp2 -> {
        assertEquals(200, resp.statusCode());

        client.post("/test/400/8ne8e94a/xhr_send", onSuccess(respSend -> {
          assertEquals(204, respSend.statusCode());

          client.post("/test/400/8ne8e94a/xhr", onSuccess(resp3 -> {
            assertEquals(200, resp.statusCode());
            resp3.bodyHandler(buffer -> {
              String body = buffer.toString();
              assertThat(body, startsWith("a"));
              JsonArray content = new JsonArray(body.substring(1));
              assertEquals(1, content.size());
              assertEquals(msg, content.getValue(0));
              complete();
            });
          })).end();

        })).end('"' + msg + '"');

      })).end();

    })).end();

    await();
  }
}
