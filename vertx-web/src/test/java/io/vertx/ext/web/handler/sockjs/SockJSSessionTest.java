/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.handler.sockjs;

import io.vertx.core.buffer.Buffer;
import io.vertx.test.core.TestUtils;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSSessionTest extends SockJSTestBase {

  @Test
  public void testNoDeadlockWhenWritingFromAnotherThreadWithSseTransport() {
    sockJSHandler.socketHandler(socket -> {
      AtomicBoolean closed = new AtomicBoolean();
      socket.endHandler(v -> {
        closed.set(true);
        testComplete();
      });
      new Thread(() -> {
        while (!closed.get()) {
          LockSupport.parkNanos(50);
          socket.write(Buffer.buffer(TestUtils.randomAlphaString(256)));
        }
      }).start();
    });
    client.get("/test/400/8ne8e94a/eventsource", resp -> {
      AtomicInteger count = new AtomicInteger();
      resp.handler(msg -> {
        if (count.incrementAndGet() == 400) {
          resp.request().connection().close();
        }
      });
    }).end();
    await();
  }

  @Test
  public void testNoDeadlockWhenWritingFromAnotherThreadWithWebsocketTransport() {
    int numMsg = 4000;
    waitFor(1);
    AtomicInteger clientReceived = new AtomicInteger();
    AtomicInteger serverReceived = new AtomicInteger();
    BooleanSupplier shallStop = () -> clientReceived.get() > numMsg * 256 && serverReceived.get() > numMsg * 256;
    sockJSHandler.socketHandler(socket -> {
      socket.handler(msg -> serverReceived.addAndGet(msg.length()));
      socket.write("hello");
      new Thread(() -> {
        while (!shallStop.getAsBoolean()) {
          LockSupport.parkNanos(50);
          try {
            socket.write(Buffer.buffer(TestUtils.randomAlphaString(256)));
          } catch (IllegalStateException e) {
            // Websocket has been closed
          }
        }
      }).start();
    });
    client.websocket("/test/400/8ne8e94a/websocket", ws -> ws.handler(msg -> {
      clientReceived.addAndGet(msg.length());
      ws.writeTextMessage("\"hello\"");
      if (shallStop.getAsBoolean()) {
        ws.handler(null);
        complete();
      }
    }));
    await();
  }

  @Test
  public void testCombineMultipleFramesIntoASingleMessage() {
    sockJSHandler.socketHandler(socket -> socket.handler(buf -> {
      assertEquals("Hello World", buf.toString());
      testComplete();
    }));
    client.websocket("/test/400/8ne8e94a/websocket", ws -> {
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame("[\"Hello", false));
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.continuationFrame(Buffer.buffer(" World\"]"), true));
      ws.close();
    });
    await();
  }
}
