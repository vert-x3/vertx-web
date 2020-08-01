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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
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

    client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr", onSuccess(req1 -> {
      req1.send(Buffer.buffer(), onSuccess(resp1 -> {
        assertEquals(200, resp1.statusCode());
        client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr", onSuccess(req2 -> {
          req2.send(Buffer.buffer(), onSuccess(resp2 -> {
            assertEquals(200, resp2.statusCode());
            client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr_send", onSuccess(req3 -> {
              req3.send(Buffer.buffer('"' + msg + '"'), onSuccess(resp3 -> {
                assertEquals(204, resp3.statusCode());
                client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr", onSuccess(req4 -> {
                  req4.send(Buffer.buffer(), onSuccess(resp4 -> {
                    assertEquals(200, resp4.statusCode());
                    resp4.body(onSuccess(buffer -> {
                      String body = buffer.toString();
                      assertThat(body, startsWith("a"));
                      JsonArray content = new JsonArray(body.substring(1));
                      assertEquals(1, content.size());
                      assertEquals(msg, content.getValue(0));
                      complete();
                    }));
                  }));
                }));
              }));
            }));
          }));
        }));
      }));
    }));

    await();
  }
}
