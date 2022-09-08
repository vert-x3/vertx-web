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

import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.Utils;
import io.vertx.test.core.TestUtils;
import org.junit.Assume;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSSessionTest extends SockJSTestBase {

  @Test
  public void testNoDeadlockWhenWritingFromAnotherThreadWithSseTransport() throws Exception {
    socketHandler = () -> {
      return socket -> {
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
      };
    };
    startServers();
    client.request(HttpMethod.GET, "/test/400/8ne8e94a/eventsource", onSuccess(req -> {
      req.send(onSuccess(resp -> {
        AtomicInteger count = new AtomicInteger();
        resp.handler(msg -> {
          if (count.incrementAndGet() == 400) {
            resp.request().connection().close();
          }
        });
      }));
    }));
    await();
  }

  @Test
  public void testNoDeadlockWhenWritingFromAnotherThreadWithWebsocketTransport() throws Exception {
    Assume.assumeFalse(Utils.isWindows());
    final Buffer random = Buffer.buffer(TestUtils.randomAlphaString(256));
    int numMsg = 1000;
    waitFor(1);
    AtomicInteger clientReceived = new AtomicInteger();
    AtomicInteger serverReceived = new AtomicInteger();
    BooleanSupplier shallStop = () -> clientReceived.get() > numMsg * 256 && serverReceived.get() > numMsg * 256;
    socketHandler = () -> {
      return socket -> {
        socket.handler(msg -> serverReceived.addAndGet(msg.length()));
        socket.write("hello");

        new Thread(() -> {
          while (!shallStop.getAsBoolean()) {
            LockSupport.parkNanos(50);
            try {
              socket.write(random)
                .onFailure(this::fail);
            } catch (IllegalStateException e) {
              // Websocket has been closed
            }
          }
        }).start();
      };
    };
    startServers();
    client.webSocket("/test/400/8ne8e94a/websocket")
      .onFailure(this::fail)
      .onSuccess(ws -> ws.handler(msg -> {
        clientReceived.addAndGet(msg.length());
        ws.writeTextMessage("\"hello\"")
          .compose(v -> ws.write(random))
          .onFailure(this::fail)
          .onSuccess(v -> {
            if (shallStop.getAsBoolean()) {
              ws.handler(null);
              complete();
            }
          });
      }));
    try {
      await();
    } catch (Throwable e) {
      System.out.println(clientReceived.get());
      System.out.println(serverReceived.get());
      throw e;
    }
  }

  @Test
  public void testCombineMultipleFramesIntoASingleMessage() throws Exception {
    socketHandler = () -> {
      return socket -> socket.handler(buf -> {
        assertEquals("Hello World", buf.toString());
        testComplete();
      });
    };
    startServers();
    client.webSocket("/test/400/8ne8e94a/websocket", onSuccess(ws -> {
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame("[\"Hello", false));
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.continuationFrame(Buffer.buffer(" World\"]"), true));
      ws.close();
    }));
    await();
  }

  @Test
  public void doesNotSendEmptyAnswerForWriteSentInEarlierBatch() throws Exception {
    AtomicInteger answerCount = new AtomicInteger();
    socketHandler = () -> {
      return socket -> socket.handler(buf -> {
        Context transportContext = vertx.getOrCreateContext();
        new Thread(() -> {
          CountDownLatch latch = blockTransportContext(transportContext);
          socket.write(Buffer.buffer("\"\""));
          socket.write(Buffer.buffer("\"\""));
          latch.countDown();
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ignored) {
          } finally {
            socket.close();
          }
        }).start();
      });
    };
    startServers();
    client.webSocket("/test/400/8ne8e94a/websocket", onSuccess(ws -> {
      ws.frameHandler(wsf -> {
        switch (wsf.type()) {
          case TEXT:
            String text = wsf.binaryData().toString();
            if (text.startsWith("a[")) {
              answerCount.getAndIncrement();
            }
            break;
          case CLOSE:
            assertEquals(1, answerCount.get());
            testComplete();
            break;
        }
      });
      ws.writeFinalTextFrame("\"\"");
    }));
    await();
  }

  private CountDownLatch blockTransportContext(Context context) {
    CountDownLatch latch = new CountDownLatch(1);
    context.runOnContext(v -> {
      try {
        latch.await(1L, SECONDS);
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    });
    return latch;
  }
}
