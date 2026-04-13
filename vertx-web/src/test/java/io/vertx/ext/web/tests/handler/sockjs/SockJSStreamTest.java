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

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;

/**
 * @author Thomas Segismont
 */
public class SockJSStreamTest extends SockJSTestBase {

  @Override
  public void setUp() throws Exception {
    numServers = 2;
    super.setUp();
  }

  @Test
  public void testStream() throws Exception {
    AtomicReference<Context> sessionContext = new AtomicReference<>();
    socketHandler = () -> {
      return socket -> {
        Context context = Vertx.currentContext();
        Assert.assertNotNull(context);
        Assert.assertTrue(sessionContext.compareAndSet(null, context));
        socket.setWriteQueueMaxSize(5);
        socket.write("Hello");
        Assert.assertTrue(socket.writeQueueFull());
        socket.drainHandler(v -> {
          Assert.assertEquals(sessionContext.get(), Vertx.currentContext());
          socket.write("World");
        });
      };
    };

    startServers();

    List<String> messages = new ArrayList<>();
    while (messages.size() < 2) {
      HttpResponse<Buffer> resp = webClient.post("/test/400/8ne8e94a/xhr")
        .sendBuffer(Buffer.buffer())
        .expecting(HttpResponseExpectation.SC_OK)
        .await();
      String body = resp.bodyAsString();
      if (body.startsWith("a")) {
        JsonArray content = new JsonArray(body.substring(1));
        messages.addAll(content.stream().map(Object::toString).collect(toList()));
      }
    }
    Assert.assertEquals(Arrays.asList("Hello", "World"), messages);
  }
}
