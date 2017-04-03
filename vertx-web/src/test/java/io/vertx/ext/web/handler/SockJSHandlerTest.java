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

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.test.core.TestUtils;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static io.vertx.core.buffer.Buffer.buffer;

/**
 * SockJS protocol tests
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SockJSHandlerTest extends WebTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();

    // These applications are required by the SockJS protocol
    router.route("/echo/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(sock -> sock.handler(sock::write)));

    router.route("/close/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(SockJSSocket::close));

    router.route("/disabled_websocket_echo/*").handler(SockJSHandler.create(vertx, new SockJSHandlerOptions()
      .setMaxBytesStreaming(4096).addDisabledTransport("WEBSOCKET")).socketHandler(sock -> sock.handler(sock::write)));

    router.route("/ticker/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(sock -> {
      long timerID = vertx.setPeriodic(1000, tid -> sock.write(buffer("tick!")));
      sock.endHandler(v -> vertx.cancelTimer(timerID));
    }));

    router.route("/amplify/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(sock -> {
      sock.handler(data -> {
        String str = data.toString();
        int n = Integer.valueOf(str);
        if (n < 0 || n > 19) {
          n = 1;
        }
        int num = (int) Math.pow(2, n);
        Buffer buff = buffer(num);
        for (int i = 0; i < num; i++) {
          buff.appendByte((byte) 'x');
        }
        sock.write(buff);
      });
    }));

    router.route("/broadcast/*").handler(SockJSHandler.create(vertx,
      new SockJSHandlerOptions().setMaxBytesStreaming(4096)).socketHandler(new Handler<SockJSSocket>() {
      Set<String> connections = new HashSet<>();

      public void handle(SockJSSocket sock) {
        connections.add(sock.writeHandlerID());
        sock.handler(buffer -> {
          for (String actorID : connections) {
            vertx.eventBus().publish(actorID, buffer);
          }
        });
        sock.endHandler(v -> {
          connections.remove(sock.writeHandlerID());
        });
      }
    }));

    router.route("/cookie_needed_echo/*").handler(SockJSHandler.create(vertx, new SockJSHandlerOptions().
      setMaxBytesStreaming(4096).setInsertJSESSIONID(true)).socketHandler(sock -> sock.handler(sock::write)));
  }

  @Test
  public void testGreeting() {
    waitFor(2);
    testGreeting("/echo/");
    testGreeting("/echo");
    await();
  }

  private void testGreeting(String uri) {
    client.getNow(uri, resp -> {
      assertEquals(200, resp.statusCode());
      assertEquals("text/plain; charset=UTF-8", resp.getHeader("content-type"));
      resp.bodyHandler(buff -> {
        assertEquals("Welcome to SockJS!\n", buff.toString());
        complete();
      });
    });
  }

  @Test
  public void testNotFound() {
    waitFor(5);

    testNotFound("/echo/a");
    testNotFound("/echo/a.html");
    testNotFound("/echo/a/a");
    testNotFound("/echo/a/a/");
    testNotFound("/echo/a/");
    testNotFound("/echo//");
    testNotFound("/echo///");

    await();
  }

  // https://github.com/vert-x3/vertx-web/issues/77
  @Test
  public void testSendWebsocketContinuationFrames() {
    // Use raw websocket transport
    client.websocket("/echo/websocket", ws -> {

      int size = 65535;

      Buffer buffer1 = TestUtils.randomBuffer(size);
      Buffer buffer2 = TestUtils.randomBuffer(size);

      ws.writeFrame(io.vertx.core.http.WebSocketFrame.binaryFrame(buffer1, false));
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.continuationFrame(buffer2, true));

      Buffer received=  Buffer.buffer();

      ws.handler(buff -> {
        received.appendBuffer(buff);
        if (received.length() == size * 2) {
          testComplete();
        }
      });

    });

    await();
  }

  private void testNotFound(String uri) {
    client.getNow(uri, resp -> {
      assertEquals(404, resp.statusCode());
      complete();
    });
  }

  @Test
  public void testCookiesRemoved() throws Exception {
    router.route("/cookiesremoved/*").handler(SockJSHandler.create(vertx)
          .socketHandler(sock -> {
            MultiMap headers = sock.headers();
            String cookieHeader = headers.get("cookie");
            assertNotNull(cookieHeader);
            assertEquals("JSESSIONID=wibble", cookieHeader);
            testComplete();
          }));
    MultiMap headers = new CaseInsensitiveHeaders();
    headers.add("cookie", "JSESSIONID=wibble");
    headers.add("cookie", "flibble=floob");

    client.websocket("/cookiesremoved/websocket", headers, ws -> {
      String frame = "foo";
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(frame, true));
    });

    await();
  }

}
