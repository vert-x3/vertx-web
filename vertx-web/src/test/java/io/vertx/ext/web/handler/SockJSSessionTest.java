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
package io.vertx.ext.web.handler;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.test.core.TestUtils;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSSessionTest extends VertxTestBase {

  private HttpClient client;
  private HttpServer server;
  private Router router;
  private SockJSHandler sockJSHandler;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080));
    router = Router.router(vertx);
    router.route().handler(CookieHandler.create());
    router.route()
      .handler(SessionHandler.create(LocalSessionStore.create(vertx))
        .setNagHttps(false)
        .setSessionTimeout(60 * 60 * 1000));
    SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
    sockJSHandler = SockJSHandler.create(vertx, options);
    router.route("/test/*").handler(sockJSHandler);
    server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router::accept).listen(ar -> latch.countDown());
    awaitLatch(latch);
  }

  @Test
  public void testNoDeadlockWhenWritingFromAnotherThreadWithSseTransport() {
    sockJSHandler.socketHandler(socket -> {
      AtomicBoolean closed = new AtomicBoolean();
      socket.endHandler(v -> {
        closed.set(true);
        testComplete();
      });
      new Thread() {
        @Override
        public void run() {
          while (!closed.get()) {
            LockSupport.parkNanos(50);
            socket.write(Buffer.buffer(TestUtils.randomAlphaString(256)));
          }
        }
      }.start();
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
    sockJSHandler.socketHandler(socket -> {
      AtomicBoolean closed = new AtomicBoolean();
      socket.endHandler(v -> {
        closed.set(true);
        testComplete();
      });
      new Thread() {
        @Override
        public void run() {
          while (!closed.get()) {
            LockSupport.parkNanos(50);
            try {
              socket.write(Buffer.buffer(TestUtils.randomAlphaString(256)));
            } catch (IllegalStateException e) {
              // Websocket has been closed
            }
          }
        }
      }.start();
    });
    client.websocket("/test/400/8ne8e94a/websocket", ws -> {
      AtomicInteger count = new AtomicInteger();
      ws.handler(msg -> {
        if (count.incrementAndGet() == 400) {
          ws.close();
        }
      });
    });
    await();
  }
}
