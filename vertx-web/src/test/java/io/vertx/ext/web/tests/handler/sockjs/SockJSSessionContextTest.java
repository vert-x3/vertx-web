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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.test.core.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;


/**
 * @author Thomas Segismont
 */
public class SockJSSessionContextTest extends SockJSTestBase {

  @Override
  public void setUp() throws Exception {
    numServers = 2;
    super.setUp();
    // add a body handler to the setup
    preSockJSHandlerSetup = router -> router.route().handler(BodyHandler.create());
  }

  @Test
  public void testHandleMessageFromXhrTransportWithAsyncHandler() throws Exception {
    String msg = "Hello World";
    socketHandler = () -> {
      return socket -> {
        socket.handler(buffer -> {
          Assert.assertEquals(msg, buffer.toString());
          socket.write(buffer);
        });
      };
    };

    startServers();

    client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr").onComplete(TestUtils.onSuccess(req1 -> {
      req1.send(Buffer.buffer()).onComplete(TestUtils.onSuccess(resp1 -> {
        Assert.assertEquals(200, resp1.statusCode());
        client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr").onComplete(TestUtils.onSuccess(req2 -> {
          req2.send(Buffer.buffer()).onComplete(TestUtils.onSuccess(resp2 -> {
            Assert.assertEquals(200, resp2.statusCode());
            client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr_send").onComplete(TestUtils.onSuccess(req3 -> {
              req3.send(Buffer.buffer('"' + msg + '"')).onComplete(TestUtils.onSuccess(resp3 -> {
                Assert.assertEquals(204, resp3.statusCode());
                client.request(HttpMethod.POST, "/test/400/8ne8e94a/xhr").onComplete(TestUtils.onSuccess(req4 -> {
                  req4.send(Buffer.buffer()).onComplete(TestUtils.onSuccess(resp4 -> {
                    Assert.assertEquals(200, resp4.statusCode());
                    resp4.body().onComplete(TestUtils.onSuccess(buffer -> {
                      String body = buffer.toString();
                      Assertions.assertThat(body).startsWith("a");
                      JsonArray content = new JsonArray(body.substring(1));
                      Assert.assertEquals(1, content.size());
                      Assert.assertEquals(msg, content.getValue(0));
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
