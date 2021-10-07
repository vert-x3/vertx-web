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
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.test.core.TestUtils;
import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSRawTransportTest extends SockJSTestBase {

  @Test
  public void testWriteText() throws Exception {
    testWrite(true);
  }

  @Test
  public void testWriteBinary() throws Exception {
    testWrite(false);
  }

  @Test
  public void disableHost() throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(expected);
      socket.endHandler(v -> {
        testComplete();
      });
    };
    startServers(new SockJSHandlerOptions());
    client.webSocket(
      new WebSocketConnectOptions()
        .setHost("localhost")
        .setPort(8080)
        .setURI("/test/websocket")
        .setAllowOriginHeader(false),
      onSuccess(ws -> {
        ws.frameHandler(frame -> {
          if (frame.isClose()) {
            //
          } else {
            assertTrue(frame.isText());
            assertEquals(expected, frame.textData());
            ws.end();
          }
        });
      }));
    await();
  }

  @Test
  public void disableHostFailWhenOriginIsRequired() throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(expected);
      socket.endHandler(v -> {
        testComplete();
      });
    };
    startServers(new SockJSHandlerOptions().setOrigin("http://localhost:8080"));
    client.webSocket(
      new WebSocketConnectOptions()
        .setHost("localhost")
        .setPort(8080)
        .setURI("/test/websocket")
        .setAllowOriginHeader(false),
      onFailure(err -> {
        assertNotNull(err);
        assertEquals("WebSocket upgrade failure: 403 (Forbidden)", err.getMessage());
        testComplete();
      }));
    await();
  }

  @Test
  public void goodOrigin() throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(expected);
      socket.endHandler(v -> {
        testComplete();
      });
    };
    startServers(new SockJSHandlerOptions().setOrigin("http://localhost:8080"));
    client.webSocket("/test/websocket", onSuccess(ws -> {
      ws.frameHandler(frame -> {
        if (frame.isClose()) {
          //
        } else {
          assertTrue(frame.isText());
          assertEquals(expected, frame.textData());
          ws.end();
        }
      });
    }));
    await();
  }

  @Test
  public void badOrigin() throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(expected);
      socket.endHandler(v -> {
        testComplete();
      });
    };
    startServers(new SockJSHandlerOptions().setOrigin("https://www.google.com"));
    client.webSocket("/test/websocket", onFailure(err -> {
      assertNotNull(err);
      assertEquals("WebSocket upgrade failure: 403 (Forbidden)", err.getMessage());
      testComplete();
    }));
    await();
  }

  private void testWrite(boolean text) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      if (text) {
        socket.write(expected);
      } else {
        socket.write(Buffer.buffer(expected));
      }
      socket.endHandler(v -> {
        testComplete();
      });
    };
    startServers(new SockJSHandlerOptions());
    client.webSocket("/test/websocket", onSuccess(ws -> {
      ws.frameHandler(frame -> {
        if (frame.isClose()) {
          //
        } else {
          if (text) {
            assertTrue(frame.isText());
            assertEquals(expected, frame.textData());
          } else {
            assertTrue(frame.isBinary());
            assertEquals(Buffer.buffer(expected), frame.binaryData());
          }
          ws.end();
        }
      });
    }));
    await();
  }
}
