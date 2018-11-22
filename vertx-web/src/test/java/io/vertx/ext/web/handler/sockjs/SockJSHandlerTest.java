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
 * SockJS protocol tests
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SockJSHandlerTest extends WebTestBase {

  private static final Logger log = LoggerFactory.getLogger(SockJSHandlerTest.class);
  private static final Buffer SOCKJS_CLOSE_REPLY = Buffer.buffer("c[3000,\"Go away!\"]");

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // Make sure a catch-all BodyHandler will not prevent websocket connection
    router.route().handler(BodyHandler.create());
    SockJSProtocolTest.installTestApplications(router, vertx);
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

      Buffer received = Buffer.buffer();

      ws.handler(buff -> {
        received.appendBuffer(buff);
        if (received.length() == size * 2) {
          testComplete();
        }
      });

    });

    await();
  }

  /**
   * Writing multiple continuation frames from the client side should result in a single message on the server side
   * after the frames are re-combined
   */
  @Test
  public void testCombineBinaryContinuationFramesRawWebSocket() throws InterruptedException {
    String serverPath = "/combine";

    AtomicReference<Buffer> serverReceivedMessage = new AtomicReference<>();
    setupSockJsServer(serverPath, (sock, requestBuffer) -> {
      serverReceivedMessage.set(requestBuffer);
      sock.write(Buffer.buffer("reply"));
      sock.close();
    });


    Buffer largeMessage = Buffer.buffer(TestUtils.randomAlphaString(30));
    WebSocketFrame frame1 = WebSocketFrame.binaryFrame(largeMessage.slice(0, 10), false);
    WebSocketFrame frame2 = WebSocketFrame.continuationFrame(largeMessage.slice(10, 20), false);
    WebSocketFrame frame3 = WebSocketFrame.continuationFrame(largeMessage.slice(20, largeMessage.length()), true);

    WebSocket ws = setupRawWebsocketClient(serverPath);
    ws.writeFrame(frame1);
    ws.writeFrame(frame2);
    ws.writeFrame(frame3);

    await(5, TimeUnit.SECONDS);

    assertEquals("Server did not combine continuation frames correctly", largeMessage, serverReceivedMessage.get());
  }

  @Test
  public void testSplitLargeReplyRawWebSocket() throws InterruptedException {
    String serverPath = "/split";

    String largeReply = TestUtils.randomAlphaString(65536 * 5);
    Buffer largeReplyBuffer = Buffer.buffer(largeReply);

    setupSockJsServer(serverPath, (sock, requestBuffer) -> {
      sock.write(largeReplyBuffer);
      sock.close();
    });

    Buffer totalReplyBuffer = Buffer.buffer(largeReplyBuffer.length());
    AtomicInteger receivedReplies = new AtomicInteger(0);
    WebSocket ws = setupRawWebsocketClient(serverPath);
    ws.handler(replyBuffer -> {
              totalReplyBuffer.appendBuffer(replyBuffer);
              receivedReplies.incrementAndGet();

            });

    ws.writeFrame(WebSocketFrame.binaryFrame(Buffer.buffer("hello"), true));

    await(5, TimeUnit.SECONDS);

    int receivedReplyCount = receivedReplies.get();
    assertEquals("Combined reply on client should equal message from server", largeReplyBuffer, totalReplyBuffer);
    assertTrue("Should have received > 1 reply frame, actually received " + receivedReplyCount, receivedReplyCount > 1);
  }

  @Test
  public void testTextFrameRawWebSocket() throws InterruptedException {
    String serverPath = "/textecho";
    setupSockJsServer(serverPath, this::echoRequest);

    String message = "hello";
    AtomicReference<String> receivedReply = new AtomicReference<>();
    WebSocket ws = setupRawWebsocketClient(serverPath);

    ws.handler(replyBuffer -> receivedReply.set(replyBuffer.toString()));

    ws.writeFrame(WebSocketFrame.textFrame(message, true));

    await(5, TimeUnit.SECONDS);

    assertEquals("Client reply should have matched request", message, receivedReply.get());
  }

  @Test
  public void testTextFrameSockJs() throws InterruptedException {
    String serverPath = "/text-sockjs";
    setupSockJsServer(serverPath, this::echoRequest);

    List<Buffer> receivedMessages = new ArrayList<>();
    WebSocket openedWebSocket = setupSockJsClient(serverPath, receivedMessages);
    String messageToSend = "[\"testMessage\"]";
    openedWebSocket.writeFrame(WebSocketFrame.textFrame(messageToSend, true));

    await(5, TimeUnit.SECONDS);

    assertEquals("Client should have received 2 messages: the reply and the close.", 2, receivedMessages.size());
    Buffer expectedReply = Buffer.buffer("a" + messageToSend);
    assertEquals("Client reply should have matched request", expectedReply, receivedMessages.get(0));
    assertEquals("Final message should have been a close", SOCKJS_CLOSE_REPLY, receivedMessages.get(1));
  }

  @Test
  public void testCombineTextFrameSockJs() throws InterruptedException {
    String serverPath = "/text-combine-sockjs";
    setupSockJsServer(serverPath, this::echoRequest);

    List<Buffer> receivedMessages = new ArrayList<>();
    WebSocket openedWebSocket = setupSockJsClient(serverPath, receivedMessages);

    Buffer largeMessage = Buffer.buffer("[\"" + TestUtils.randomAlphaString(30) + "\"]");
    WebSocketFrame frame1 = new WebSocketFrameImpl(FrameType.TEXT, largeMessage.slice(0, 10).getByteBuf(), false);
    WebSocketFrame frame2 = WebSocketFrame.continuationFrame(largeMessage.slice(10, 20), false);
    WebSocketFrame frame3 = WebSocketFrame.continuationFrame(largeMessage.slice(20, largeMessage.length()), true);

    log.debug("Client sending " + frame1.textData());
    openedWebSocket.writeFrame(frame1);
    log.debug("Client sending " + frame2.textData());
    openedWebSocket.writeFrame(frame2);
    log.debug("Client sending " + frame3.textData());
    openedWebSocket.writeFrame(frame3);

    await(5, TimeUnit.SECONDS);

    assertEquals("Client should have received 2 messages: the reply and the close.", 2, receivedMessages.size());
    Buffer expectedReply = Buffer.buffer("a" + largeMessage.toString());
    assertEquals("Client reply should have matched request", expectedReply, receivedMessages.get(0));
    assertEquals("Final message should have been a close", SOCKJS_CLOSE_REPLY, receivedMessages.get(1));
  }

  @Test
  public void testSplitLargeReplySockJs() throws InterruptedException {
    String serverPath = "/large-reply-sockjs";

    String largeMessage = TestUtils.randomAlphaString(65536 * 2);
    Buffer largeReplyBuffer = Buffer.buffer(largeMessage);

    setupSockJsServer(serverPath, (sock, requestBuffer) -> {
      sock.write(largeReplyBuffer);
      sock.close();
    });

    List<Buffer> receivedMessages = new ArrayList<>();
    WebSocket openedWebSocket = setupSockJsClient(serverPath, receivedMessages);

    String messageToSend = "[\"hello\"]";
    openedWebSocket.writeFrame(WebSocketFrame.textFrame(messageToSend, true));

    await(5, TimeUnit.SECONDS);

    int receivedReplyCount = receivedMessages.size();
    assertTrue("Should have received > 2 reply frame, actually received " + receivedReplyCount, receivedReplyCount > 2);

    Buffer expectedReplyBuffer = Buffer.buffer("a[\"").appendBuffer(largeReplyBuffer).appendBuffer(Buffer.buffer("\"]"));
    Buffer clientReplyBuffer = combineReplies(receivedMessages.subList(0, receivedMessages.size() - 1));
    assertEquals(String.format("Combined reply on client (length %s) should equal message from server (%s)",
            clientReplyBuffer.length(), expectedReplyBuffer.length()),
            expectedReplyBuffer, clientReplyBuffer);

    Buffer finalMessage = receivedMessages.get(receivedMessages.size() - 1);
    assertEquals("Final message should have been a close", SOCKJS_CLOSE_REPLY, finalMessage);
  }

  private Buffer combineReplies(List<Buffer> receivedMessages) {
    Buffer combinedReply = Buffer.buffer();
    for (Buffer receivedMessage : receivedMessages) {
      combinedReply.appendBuffer(receivedMessage);
    }
    return combinedReply;
  }

  private void echoRequest(SockJSSocket sock, Buffer requestBuffer) {
    log.debug("Server received " + requestBuffer);
    log.debug("Server sending " + requestBuffer);
    sock.write(requestBuffer);
    sock.close();
  }

  private void setupSockJsServer(String serverPath, BiConsumer<SockJSSocket, Buffer> serverBufferHandler) {
    String path = serverPath + "/*";
    router.route(path).handler(SockJSHandler.create(vertx)
            .socketHandler(sock -> {
              sock.handler(buffer -> serverBufferHandler.accept(sock, buffer));
              sock.exceptionHandler(this::fail);
            }));
  }

  /**
   * This sets up a handler on the websocket
   */
  private WebSocket setupSockJsClient(String serverPath, List<Buffer> receivedMessagesCollector)
          throws InterruptedException
  {
    String requestURI = serverPath + "/000/000/websocket";

    AtomicReference<WebSocket> openedWebSocketReference = new AtomicReference<>();
    CountDownLatch openSocketCountDown = new CountDownLatch(1);
    client.websocket(requestURI, ws -> {
      openedWebSocketReference.set(ws);
      ws.handler(replyBuffer -> {
        log.debug("Client received " + replyBuffer);
        String textReply = replyBuffer.toString();
        if ("o".equals(textReply)) {
          openSocketCountDown.countDown();
        } else {
          receivedMessagesCollector.add(replyBuffer);
        }
      });
      ws.endHandler(v -> testComplete());
      ws.exceptionHandler(this::fail);
    });

    openSocketCountDown.await(5, TimeUnit.SECONDS);
    return openedWebSocketReference.get();
  }

  /**
   * This does not set up a handler on the websocket
   */
  private WebSocket setupRawWebsocketClient(String serverPath)
          throws InterruptedException
  {
    String requestURI = serverPath + "/websocket";

    AtomicReference<WebSocket> openedWebSocketReference = new AtomicReference<>();
    CountDownLatch openSocketCountDown = new CountDownLatch(1);
    client.websocket(requestURI, ws -> {
      openedWebSocketReference.set(ws);
      openSocketCountDown.countDown();
      ws.endHandler(v -> testComplete());
      ws.exceptionHandler(this::fail);
    });

    openSocketCountDown.await(5, TimeUnit.SECONDS);
    return openedWebSocketReference.get();
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

  @Test
  public void testTimeoutCloseCode() {
    router.route("/ws-timeout/*").handler(SockJSHandler
      .create(vertx)
      .bridge(new BridgeOptions().setPingTimeout(1))
    );

    client.websocket("/ws-timeout/websocket", ws -> ws.frameHandler(frame -> {
      if (frame.isClose()) {
        assertEquals(1001, frame.closeStatusCode());
        assertEquals("Session expired", frame.closeReason());
        testComplete();
      }
    }));
    await();
  }

  @Test
  public void testInvalidMessageCode() {
    router.route("/ws-timeout/*").handler(SockJSHandler
      .create(vertx)
      .bridge(new BridgeOptions().addInboundPermitted(new PermittedOptions().setAddress("SockJSHandlerTest.testInvalidMessageCode")))
    );

    vertx.eventBus().consumer("SockJSHandlerTest.testInvalidMessageCode", msg -> msg.reply(new JsonObject()));

    client.websocket("/ws-timeout/websocket", ws -> {
      ws.writeFinalBinaryFrame(Buffer.buffer("durp!"));

      ws.frameHandler(frame -> {
        // we should get a normal frame with a error message
        if (!frame.isClose()) {
          JsonObject msg = new JsonObject(frame.binaryData());
          assertEquals("err", msg.getString("type"));
          assertEquals("invalid_json", msg.getString("body"));
          testComplete();
          ws.close();
        }
      });
    });
    await();
  }
}
