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

import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.internal.buffer.BufferInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.tests.WebTestBase;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.impl.RoutingContextInternal;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.test.core.TestUtils;
import static org.junit.jupiter.api.Assertions.*;
import io.vertx.junit5.Checkpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
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
  @BeforeEach
  public void setUp(Vertx vertx) throws Exception {
    super.setUp(vertx);
    // Make sure a catch-all BodyHandler will not prevent websocket connection
    router.route().handler(BodyHandler.create());
    SockJSProtocolTest.installTestApplications(router, vertx);
  }

  @Test
  public void testGreeting() {
    testGreeting("/echo/");
    testGreeting("/echo");
  }

  private void testGreeting(String uri) {
    HttpResponse<Buffer> resp = webClient.get(uri)
      .send()
      .expecting(io.vertx.core.http.HttpResponseExpectation.SC_OK)
      .expecting(io.vertx.core.http.HttpResponseExpectation.contentType("text/plain"))
      .await();
    assertEquals("Welcome to SockJS!\n", resp.bodyAsString());
  }

  @Test
  public void testNotFound() {
    testNotFound("/echo/a");
    testNotFound("/echo/a.html");
    testNotFound("/echo/a/a");
    testNotFound("/echo/a/a/");
    testNotFound("/echo/a/");
    testNotFound("/echo//");
    testNotFound("/echo///");
  }

  // https://github.com/vert-x3/vertx-web/issues/77
  @Test
  public void testSendWebsocketContinuationFrames(Checkpoint done) {
    // Use raw websocket transport
    wsClient.connect("/echo/websocket").onComplete(TestUtils.onSuccess(ws -> {

      int size = 65535;

      Buffer buffer1 = TestUtils.randomBuffer(size);
      Buffer buffer2 = TestUtils.randomBuffer(size);

      ws.writeFrame(io.vertx.core.http.WebSocketFrame.binaryFrame(buffer1, false));
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.continuationFrame(buffer2, true));

      Buffer received = Buffer.buffer();

      ws.handler(buff -> {
        received.appendBuffer(buff);
        if (received.length() == size * 2) {
          done.flag();
        }
      });

    }));
  }

  /**
   * Writing multiple continuation frames from the client side should result in a single message on the server side
   * after the frames are re-combined
   */
  @Test
  public void testCombineBinaryContinuationFramesRawWebSocket(Checkpoint done) {
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

    WebSocket ws = setupRawWebsocketClient(serverPath, () -> {
      assertEquals(largeMessage, serverReceivedMessage.get(), "Server did not combine continuation frames correctly");
      done.flag();
    });
    ws.writeFrame(frame1);
    ws.writeFrame(frame2);
    ws.writeFrame(frame3);
  }

  @Test
  public void testSplitLargeReplyRawWebSocket(Checkpoint done) {
    String serverPath = "/split";

    String largeReply = TestUtils.randomAlphaString(65536 * 5);
    Buffer largeReplyBuffer = Buffer.buffer(largeReply);

    setupSockJsServer(serverPath, (sock, requestBuffer) -> {
      sock.write(largeReplyBuffer);
      sock.close();
    });

    Buffer totalReplyBuffer = Buffer.buffer(largeReplyBuffer.length());
    AtomicInteger receivedReplies = new AtomicInteger(0);
    WebSocket ws = setupRawWebsocketClient(serverPath, () -> {
      int receivedReplyCount = receivedReplies.get();
      assertEquals(largeReplyBuffer, totalReplyBuffer, "Combined reply on client should equal message from server");
      assertTrue(receivedReplyCount > 1, "Should have received > 1 reply frame, actually received " + receivedReplyCount);
      done.flag();
    });
    ws.handler(replyBuffer -> {
      totalReplyBuffer.appendBuffer(replyBuffer);
      receivedReplies.incrementAndGet();
    });

    ws.writeFrame(WebSocketFrame.binaryFrame(Buffer.buffer("hello"), true));
  }

  @Test
  public void testTextFrameRawWebSocket(Checkpoint done) {
    String serverPath = "/textecho";
    setupSockJsServer(serverPath, this::echoRequest);

    String message = "hello";
    AtomicReference<String> receivedReply = new AtomicReference<>();
    WebSocket ws = setupRawWebsocketClient(serverPath, () -> {
      assertEquals(message, receivedReply.get(), "Client reply should have matched request");
      done.flag();
    });

    ws.handler(replyBuffer -> receivedReply.set(replyBuffer.toString()));

    ws.writeFrame(WebSocketFrame.textFrame(message, true));
  }

  @Test
  public void testTextFrameSockJs(Checkpoint done) {
    String serverPath = "/text-sockjs";
    setupSockJsServer(serverPath, this::echoRequest);

    List<Buffer> receivedMessages = new ArrayList<>();
    String messageToSend = "[\"testMessage\"]";
    WebSocket openedWebSocket = setupSockJsClient(serverPath, receivedMessages, () -> {
      assertEquals(2, receivedMessages.size(), "Client should have received 2 messages: the reply and the close.");
      Buffer expectedReply = Buffer.buffer("a" + messageToSend);
      assertEquals(expectedReply, receivedMessages.get(0), "Client reply should have matched request");
      assertEquals(SOCKJS_CLOSE_REPLY, receivedMessages.get(1), "Final message should have been a close");
      done.flag();
    });
    openedWebSocket.writeFrame(WebSocketFrame.textFrame(messageToSend, true));
  }

  @Test
  public void testCombineTextFrameSockJs(Checkpoint done) {
    String serverPath = "/text-combine-sockjs";
    setupSockJsServer(serverPath, this::echoRequest);

    List<Buffer> receivedMessages = new ArrayList<>();
    BufferInternal largeMessage = BufferInternal.buffer("[\"" + TestUtils.randomAlphaString(30) + "\"]");

    WebSocket openedWebSocket = setupSockJsClient(serverPath, receivedMessages, () -> {
      assertEquals(2, receivedMessages.size(), "Client should have received 2 messages: the reply and the close.");
      Buffer expectedReply = Buffer.buffer("a" + largeMessage.toString());
      assertEquals(expectedReply, receivedMessages.get(0), "Client reply should have matched request");
      assertEquals(SOCKJS_CLOSE_REPLY, receivedMessages.get(1), "Final message should have been a close");
      done.flag();
    });

    WebSocketFrame frame1 = WebSocketFrame.textFrame(largeMessage.slice(0, 10).toString(StandardCharsets.UTF_8), false);
    WebSocketFrame frame2 = WebSocketFrame.continuationFrame(largeMessage.slice(10, 20), false);
    WebSocketFrame frame3 = WebSocketFrame.continuationFrame(largeMessage.slice(20, largeMessage.length()), true);

    log.debug("Client sending " + frame1.textData());
    openedWebSocket.writeFrame(frame1);
    log.debug("Client sending " + frame2.textData());
    openedWebSocket.writeFrame(frame2);
    log.debug("Client sending " + frame3.textData());
    openedWebSocket.writeFrame(frame3);
  }

  @Test
  public void testSplitLargeReplySockJs(Checkpoint done) {
    String serverPath = "/large-reply-sockjs";

    String largeMessage = TestUtils.randomAlphaString(65536 * 2);
    Buffer largeReplyBuffer = Buffer.buffer(largeMessage);

    setupSockJsServer(serverPath, (sock, requestBuffer) -> {
      sock.write(largeReplyBuffer);
      sock.close();
    });

    List<Buffer> receivedMessages = new ArrayList<>();
    WebSocket openedWebSocket = setupSockJsClient(serverPath, receivedMessages, () -> {
      int receivedReplyCount = receivedMessages.size();
      assertTrue(receivedReplyCount > 2, "Should have received > 2 reply frame, actually received " + receivedReplyCount);

      Buffer expectedReplyBuffer = Buffer.buffer("a[\"").appendBuffer(largeReplyBuffer).appendBuffer(Buffer.buffer("\"]"));
      Buffer clientReplyBuffer = combineReplies(receivedMessages.subList(0, receivedMessages.size() - 1));
      assertEquals(expectedReplyBuffer, clientReplyBuffer,
        String.format("Combined reply on client (length %s) should equal message from server (%s)",
        clientReplyBuffer.length(), expectedReplyBuffer.length()));

      Buffer finalMessage = receivedMessages.get(receivedMessages.size() - 1);
      assertEquals(SOCKJS_CLOSE_REPLY, finalMessage, "Final message should have been a close");
      done.flag();
    });

    String messageToSend = "[\"hello\"]";
    openedWebSocket.writeFrame(WebSocketFrame.textFrame(messageToSend, true));
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
    sock.exceptionHandler(null);
    sock.close();
  }

  private void setupSockJsServer(String serverPath, BiConsumer<SockJSSocket, Buffer> serverBufferHandler) {
    String path = serverPath;
    router.route(path + "*").subRouter(SockJSHandler.create(vertx)
      .socketHandler(sock -> {
        sock.handler(buffer -> serverBufferHandler.accept(sock, buffer));
        sock.exceptionHandler(err -> fail(err.getMessage()));
      }));
  }

  /**
   * This sets up a handler on the websocket
   */
  private WebSocket setupSockJsClient(String serverPath, List<Buffer> receivedMessagesCollector, Runnable onClose) {
    String requestURI = serverPath + "/000/000/websocket";

    Promise<WebSocket> promise = Promise.promise();
    wsClient.connect(requestURI).onComplete(TestUtils.onSuccess(ws -> {
      ws.handler(replyBuffer -> {
        log.debug("Client received " + replyBuffer);
        String textReply = replyBuffer.toString();
        if ("o".equals(textReply)) {
          promise.complete(ws);
        } else {
          receivedMessagesCollector.add(replyBuffer);
        }
      });
      ws.endHandler(v -> onClose.run());
      ws.exceptionHandler(err -> fail(err.getMessage()));
    }));

    return promise.future().await();
  }

  /**
   * This does not set up a handler on the websocket
   */
  private WebSocket setupRawWebsocketClient(String serverPath, Runnable onClose) {
    String requestURI = serverPath + "/websocket";

    WebSocket ws = wsClient.connect(requestURI).await();
    ws.endHandler(v -> onClose.run());
    ws.exceptionHandler(err -> fail(err.getMessage()));
    return ws;
  }

  private void testNotFound(String uri) {
    webClient.get(uri)
      .send()
      .expecting(HttpResponseExpectation.status(404))
      .await();
  }

  @Test
  public void testWebContext(Checkpoint checkpoint) {
    CountDownLatch done = checkpoint.asLatch(2);
    SessionStore store = SessionStore.create(vertx);
    SessionHandler handler = SessionHandler.create(store).setCookieless(true);
    CompletableFuture<String> sessionID = new CompletableFuture<>();
    CompletableFuture<User> sessionUser = new CompletableFuture<>();
    router.route("/webcontext*").subRouter(SockJSHandler.create(vertx)
      .socketHandler(sock -> {
        JsonObject principal = new JsonObject().put("key", "val");
        Session oldSession = sock.webSession();
        Session session = handler.newSession(sock.routingContext());
        User user = User.create(principal);
        handler.setUser(sock.routingContext(), user).onComplete((result) -> {
          assertFalse(result.failed());
          assertNotSame(session, oldSession);
          assertEquals(session, sock.webSession());
          ((RoutingContextInternal) sock.routingContext()).setSession(session);
          assertEquals(sock.webSession(), sock.routingContext().session());
          assertEquals(sock.webUser(), sock.routingContext().user());
          assertEquals(sock.webUser(), user);
          assertEquals(session, sock.webSession());
          assertEquals(session, store.get(session.id()).result());
          sessionID.complete(session.id());
          sessionUser.complete(sock.webUser());
        });
      }));

    router.route("/webcontextuser*").subRouter(SockJSHandler.create(vertx)
      .socketHandler(sock -> {
        Session session = null;
        try {
          session = store.get(sessionID.get()).result();
        } catch (InterruptedException | ExecutionException e) {
          fail(e.getMessage());
        }
        ((RoutingContextInternal) sock.routingContext()).setSession(session);
        try {
          assertEquals(sessionID.get(), store.get(sessionID.get()).result().id());
          assertEquals(sessionUser.get(), sock.webUser());
        } catch (InterruptedException | ExecutionException e) {
          fail(e.getMessage());
        }
        done.countDown();
      }));

    wsClient.connect(new WebSocketConnectOptions()
      .setPort(8080)
      .setURI("/webcontext/websocket"))
      .compose(ws -> wsClient.connect(new WebSocketConnectOptions()
        .setPort(8080)
        .setURI("/webcontextuser/websocket")))
      .onComplete(TestUtils.onSuccess(wsuser -> done.countDown()));
  }

  @Test
  public void testCookiesRemoved(Checkpoint checkpoint) {
    CountDownLatch done = checkpoint.asLatch(2);
    router.route("/cookiesremoved*").subRouter(SockJSHandler.create(vertx)
      .socketHandler(sock -> {
        MultiMap headers = sock.headers();
        String cookieHeader = headers.get("cookie");
        assertNotNull(cookieHeader);
        assertEquals("JSESSIONID=wibble", cookieHeader);
        done.countDown();
      }));
    MultiMap headers = HttpHeaders.headers();
    headers.add("cookie", "JSESSIONID=wibble");
    headers.add("cookie", "flibble=floob");

    wsClient.connect(new WebSocketConnectOptions()
      .setPort(8080)
      .setURI("/cookiesremoved/websocket")
      .setHeaders(headers)).onComplete(TestUtils.onSuccess(ws -> {
        done.countDown();
    }));
  }

  @Test
  public void testTimeoutCloseCode(Checkpoint done) {
    router.route("/ws-timeout*").subRouter(SockJSHandler
      .create(vertx)
      .bridge(new SockJSBridgeOptions().setPingTimeout(1))
    );

    wsClient.connect("/ws-timeout/websocket").onComplete(TestUtils.onSuccess(ws -> ws.frameHandler(frame -> {
      if (frame.isClose()) {
        assertEquals(1001, frame.closeStatusCode());
        assertEquals("Session expired", frame.closeReason());
        done.flag();
      }
    })));
  }

  @Test
  public void testInvalidMessageCode(Checkpoint done) {
    router.route("/ws-timeout*").subRouter(SockJSHandler
      .create(vertx)
      .bridge(new SockJSBridgeOptions().addInboundPermitted(new PermittedOptions().setAddress("SockJSHandlerTest.testInvalidMessageCode")))
    );

    vertx.eventBus().consumer("SockJSHandlerTest.testInvalidMessageCode", msg -> msg.reply(new JsonObject()));

    wsClient.connect("/ws-timeout/websocket").onComplete(TestUtils.onSuccess(ws -> {
      ws.writeFinalBinaryFrame(Buffer.buffer("durp!"));

      ws.frameHandler(frame -> {
        // we should get a normal frame with a error message
        if (!frame.isClose()) {
          JsonObject msg = new JsonObject(frame.binaryData());
          assertEquals("err", msg.getString("type"));
          assertEquals("invalid_json", msg.getString("body"));
          done.flag();
          ws.close();
        }
      });
    }));
  }
}
