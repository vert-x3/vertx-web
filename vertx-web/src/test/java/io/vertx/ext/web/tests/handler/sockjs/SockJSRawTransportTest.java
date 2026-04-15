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
package io.vertx.ext.web.tests.handler.sockjs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.junit5.VertxTestContext;
import io.vertx.test.core.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSRawTransportTest extends SockJSTestBase {

  @Test
  public void testWriteText(VertxTestContext testContext) throws Exception {
    testWrite(true, testContext);
  }

  @Test
  public void testWriteBinary(VertxTestContext testContext) throws Exception {
    testWrite(false, testContext);
  }

  @Test
  public void disableHost(VertxTestContext testContext) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(expected);
      socket.endHandler(v -> {
        testContext.completeNow();
      });
    };
    startServers(new SockJSHandlerOptions());
    wsClient.connect(
      new WebSocketConnectOptions()
        .setHost("localhost")
        .setPort(8080)
        .setURI("/test/websocket")
        .setAllowOriginHeader(false)).onComplete(TestUtils.onSuccess(ws -> {
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
  }

  @Test
  public void disableHostFailWhenOriginIsRequired(VertxTestContext testContext) throws Exception {
    socketHandler = () -> socket -> {
      socket.write(TestUtils.randomAlphaString(64));
    };
    startServers(new SockJSHandlerOptions().setOrigin("http://localhost:8080"));
    wsClient.connect(
      new WebSocketConnectOptions()
        .setHost("localhost")
        .setPort(8080)
        .setURI("/test/websocket")
        .setAllowOriginHeader(false)).onComplete(TestUtils.onFailure(err -> {
        assertNotNull(err);
        assertEquals("WebSocket upgrade failure: 403", err.getMessage());
        testContext.completeNow();
      }));
  }

  @Test
  public void goodOrigin(VertxTestContext testContext) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      socket.write(expected);
      socket.endHandler(v -> {
        testContext.completeNow();
      });
    };
    startServers(new SockJSHandlerOptions().setOrigin("http://localhost:8080"));
    wsClient.connect("/test/websocket").onComplete(TestUtils.onSuccess(ws -> {
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
  }

  @Test
  public void badOrigin(VertxTestContext testContext) throws Exception {
    socketHandler = () -> socket -> {
      socket.write(TestUtils.randomAlphaString(64));
    };
    startServers(new SockJSHandlerOptions().setOrigin("https://www.google.com"));
    wsClient.connect("/test/websocket").onComplete(TestUtils.onFailure(err -> {
      assertNotNull(err);
      assertEquals("WebSocket upgrade failure: 403", err.getMessage());
      testContext.completeNow();
    }));
  }

  private void testWrite(boolean text, VertxTestContext testContext) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      if (text) {
        socket.write(expected);
      } else {
        socket.write(Buffer.buffer(expected));
      }
      socket.endHandler(v -> {
        testContext.completeNow();
      });
    };
    startServers(new SockJSHandlerOptions());
    wsClient.connect("/test/websocket").onComplete(TestUtils.onSuccess(ws -> {
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
  }
}
