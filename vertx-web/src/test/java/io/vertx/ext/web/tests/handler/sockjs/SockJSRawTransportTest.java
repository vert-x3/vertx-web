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
import io.vertx.test.core.TestUtils;
import org.junit.Assert;
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
            Assert.assertTrue(frame.isText());
            Assert.assertEquals(expected, frame.textData());
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
    wsClient.connect(
      new WebSocketConnectOptions()
        .setHost("localhost")
        .setPort(8080)
        .setURI("/test/websocket")
        .setAllowOriginHeader(false)).onComplete(TestUtils.onFailure(err -> {
        Assert.assertNotNull(err);
        Assert.assertEquals("WebSocket upgrade failure: 403", err.getMessage());
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
    wsClient.connect("/test/websocket").onComplete(TestUtils.onSuccess(ws -> {
      ws.frameHandler(frame -> {
        if (frame.isClose()) {
          //
        } else {
          Assert.assertTrue(frame.isText());
          Assert.assertEquals(expected, frame.textData());
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
    wsClient.connect("/test/websocket").onComplete(TestUtils.onFailure(err -> {
      Assert.assertNotNull(err);
      Assert.assertEquals("WebSocket upgrade failure: 403", err.getMessage());
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
    wsClient.connect("/test/websocket").onComplete(TestUtils.onSuccess(ws -> {
      ws.frameHandler(frame -> {
        if (frame.isClose()) {
          //
        } else {
          if (text) {
            Assert.assertTrue(frame.isText());
            Assert.assertEquals(expected, frame.textData());
          } else {
            Assert.assertTrue(frame.isBinary());
            Assert.assertEquals(Buffer.buffer(expected), frame.binaryData());
          }
          ws.end();
        }
      });
    }));
    await();
  }
}
