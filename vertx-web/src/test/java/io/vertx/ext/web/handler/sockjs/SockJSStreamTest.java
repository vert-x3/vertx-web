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

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        assertNotNull(context);
        assertTrue(sessionContext.compareAndSet(null, context));
        socket.setWriteQueueMaxSize(5);
        socket.write("Hello");
        assertTrue(socket.writeQueueFull());
        socket.drainHandler(v -> {
          assertEquals(sessionContext.get(), Vertx.currentContext());
          socket.write("World");
        });
      };
    };

    startServers();

    List<String> messages = Collections.synchronizedList(new ArrayList<>());
    fetchMessages(messages);
    await();
  }

  private void fetchMessages(List<String> messages) {
    client.post("/test/400/8ne8e94a/xhr", onSuccess(resp -> {
      assertEquals(200, resp.statusCode());
      resp.bodyHandler(buffer -> {
        String body = buffer.toString();
        if (body.startsWith("a")) {
          JsonArray content = new JsonArray(body.substring(1));
          messages.addAll(content.stream().map(Object::toString).collect(toList()));
        }
        if (messages.size() < 2) {
          fetchMessages(messages);
        } else {
          assertEquals(Arrays.asList("Hello", "World"), messages);
          testComplete();
        }
      });
    })).end();
  }
}
