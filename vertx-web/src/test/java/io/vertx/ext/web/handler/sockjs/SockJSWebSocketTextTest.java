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

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.http.impl.FrameType;
import io.vertx.core.http.impl.ws.WebSocketFrameImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.test.core.TestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * Test SockJS support for raw WebSocket text frames
 *
 * @author <a href="https://geek.co.il">Oded Arbel</a>
 */
public class SockJSWebSocketTextTest extends WebTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // Make sure a catch-all BodyHandler will not prevent websocket connection
    router.route().handler(BodyHandler.create());
    SockJSHandler textEchoHandler = SockJSHandler.create(vertx,
            new SockJSHandlerOptions().setMaxBytesStreaming(4096))
            .socketHandler(sock -> sock.handler(buf -> sock.write(buf.toString())));
    SockJSHandler binEchoHandler = SockJSHandler.create(vertx,
            new SockJSHandlerOptions().setMaxBytesStreaming(4096))
            .socketHandler(sock -> sock.handler(buf -> sock.write(buf)));
    router.route("/echo-text/*").handler(textEchoHandler);
    router.route("/echo-bin/*").handler(binEchoHandler);
  }

  @Test
  public void testWebSocketTextFrames() {
    String testMessage = "hello world";
    // Use raw websocket transport
    client.websocket("/echo-text/websocket", ws -> {
      ws.writeTextMessage(testMessage);
      ws.frameHandler(frame -> {
        if (frame.isClose()) return; // ignore the close frame, we expect it
        assertTrue("Should have received text reply frame, actually received binary", frame.isText());
        String received = frame.textData();
        assertEquals("Should have received test text, actually received: " + received, testMessage, received);
        complete();
      });
    });
    
    await();
  }

  @Test
  public void testWebSocketBinaryFrames() {
    Buffer testMessage = Buffer.buffer("hello binary world");
    // Use raw websocket transport
    client.websocket("/echo-bin/websocket", ws -> {
      ws.writeBinaryMessage(testMessage);
      ws.frameHandler(frame -> {
        if (frame.isClose()) return; // ignore the close frame, we expect it
        assertTrue("Should have received binary reply frame, actually received text", frame.isBinary());
        Buffer received = frame.binaryData();
        assertEquals("Should have received test data, actually received: " + received, testMessage, received);
        complete();
      });
    });
    
    await();
  }

}
