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
import io.vertx.junit5.Checkpoint;
import io.vertx.test.core.TestUtils;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SockJSEventBusTest extends SockJSTestBase {

  @Test
  public void testWriteText(Checkpoint checkpoint) throws Exception {
    testWrite(true, checkpoint::flag);
  }

  @RepeatedTest(1000)
  public void testWriteBinary(Checkpoint checkpoint) throws Exception {
    testWrite(false, checkpoint::flag);
  }

  private void testWrite(boolean text, Runnable done) throws Exception {
    String expected = TestUtils.randomAlphaString(64);
    socketHandler = () -> socket -> {
      if (text) {
        vertx.eventBus().send(socket.writeHandlerID(), expected);
      } else {
        vertx.eventBus().send(socket.writeHandlerID(), Buffer.buffer(expected));
      }
      socket.endHandler(v -> {
        done.run();
      });
    };
    startServers();
    vertx.runOnContext(v -> {
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
    });
  }
}
