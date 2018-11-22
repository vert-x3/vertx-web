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

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.WebSocketBase;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.sockjs.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.test.core.TestUtils;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class EventbusBridgeTest extends WebTestBase {

  protected SockJSHandler sockJSHandler;
  protected BridgeOptions defaultOptions = new BridgeOptions();
  protected BridgeOptions allAccessOptions =
    new BridgeOptions().addInboundPermitted(new PermittedOptions()).addOutboundPermitted(new PermittedOptions());

  protected String websocketURI = "/eventbus/websocket";
  protected String addr = "someaddress";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    sockJSHandler = SockJSHandler.create(vertx);
    router.route("/eventbus/*").handler(sockJSHandler);
  }

  @Test
  public void testHookCreateSocket() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.SOCKET_CREATED) {
        assertNotNull(be.socket());
        assertNull(be.getRawMessage());
        be.complete(true);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testSend("foobar");
    await();
  }

  @Test
  public void testHookCreateSocketRejected() throws Exception {

    CountDownLatch latch = new CountDownLatch(2);

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.SOCKET_CREATED) {
        be.complete(false);
        latch.countDown();
      } else {
        be.complete(true);
      }
    });

    client.websocket(websocketURI, ws -> {
      JsonObject msg = new JsonObject().put("type", "send").put("address", addr).put("body", "foobar");
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));
      ws.closeHandler(v -> latch.countDown());
    });

    awaitLatch(latch);
  }

  @Test
  public void testHookSocketClosed() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.SOCKET_CLOSED) {
        assertNotNull(be.socket());
        assertNull(be.getRawMessage());
        be.complete(true);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    client.websocket(websocketURI, WebSocketBase::close);
    await();
  }

  @Test
  public void testHookSend() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.SEND) {
        assertNotNull(be.socket());
        JsonObject raw = be.getRawMessage();
        assertEquals(addr, raw.getString("address"));
        assertEquals("foobar", raw.getString("body"));
        be.complete(true);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testSend("foobar");
    await();
  }

  @Test
  public void testHookSendHeaders() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.SEND) {
        assertNotNull(be.socket());
        JsonObject raw = be.getRawMessage();
        assertEquals(addr, raw.getString("address"));
        assertEquals("foobar", raw.getString("body"));
        raw.put("headers", new JsonObject().put("hdr1", "val1").put("hdr2", "val2"));
        be.setRawMessage(raw);
        be.complete(true);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testSend(addr, "foobar", true);
    await();
  }

  @Test
  public void testHookSendRejected() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.SEND) {
        be.complete(false);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testError(new JsonObject().put("type", "send").put("address", addr).put("body", "foobar"),
      "rejected");
    await();
  }

  @Test
  public void testHookSendMissingAddress() throws Exception {
    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.SEND) {
        be.getRawMessage().remove("address");
        testComplete();
      }
      be.complete(true);
    });
    testError(new JsonObject().put("type", "send").put("address", addr).put("body", "foobar"),
      "missing_address");
    await();
  }

  @Test
  public void testHookPublish() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.PUBLISH) {
        assertNotNull(be.socket());
        JsonObject raw = be.getRawMessage();
        assertEquals(addr, raw.getString("address"));
        assertEquals("foobar", raw.getString("body"));
        be.complete(true);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testPublish("foobar");
    await();
  }

  @Test
  public void testHookPublishHeaders() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.PUBLISH) {
        assertNotNull(be.socket());
        JsonObject raw = be.getRawMessage();
        assertEquals(addr, raw.getString("address"));
        assertEquals("foobar", raw.getString("body"));
        raw.put("headers", new JsonObject().put("hdr1", "val1").put("hdr2", "val2"));
        be.setRawMessage(raw);
        be.complete(true);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testPublish(addr, "foobar", true);
    await();
  }

  @Test
  public void testHookPubRejected() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.PUBLISH) {
        be.complete(false);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testError(new JsonObject().put("type", "publish").put("address", addr).put("body", "foobar"),
      "rejected");
    await();
  }

  @Test
  public void testHookPublishMissingAddress() throws Exception {
    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.PUBLISH) {
        be.getRawMessage().remove("address");
        testComplete();
      }
      be.complete(true);
    });
    testError(new JsonObject().put("type", "publish").put("address", addr).put("body", "foobar"),
      "missing_address");
    await();
  }

  @Test
  public void testHookRegister() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.REGISTER) {
        assertNotNull(be.socket());
        JsonObject raw = be.getRawMessage();
        assertEquals(addr, raw.getString("address"));
        be.complete(true);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testReceive("foobar");
    await();
  }

  @Test
  public void testHookRegisterRejected() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.REGISTER) {
        be.complete(false);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testError(new JsonObject().put("type", "register").put("address", addr),
      "rejected");
    await();
  }

  @Test
  public void testHookRegisterMissingAddress() throws Exception {
    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.REGISTER) {
        be.getRawMessage().remove("address");
        testComplete();
      }
      be.complete(true);
    });
    testError(new JsonObject().put("type", "register").put("address", addr).put("body", "foobar"),
      "missing_address");
    await();
  }

  @Test
  public void testHookReceive() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.RECEIVE) {
        assertNotNull(be.socket());
        JsonObject raw = be.getRawMessage();
        assertEquals(addr, raw.getString("address"));
        assertEquals("foobar", raw.getString("body"));
        be.complete(true);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testReceive("foobar");
    await();
  }

  @Test
  public void testHookReceiveRejected() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.RECEIVE) {
        be.complete(false);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testReceiveFail(addr, "foobar");
    await();
  }

  @Test
  public void testHookUnregister() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.UNREGISTER) {
        assertNotNull(be.socket());
        JsonObject raw = be.getRawMessage();
        assertEquals(addr, raw.getString("address"));
        be.complete(true);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testUnregister(addr);
    await();
  }

  @Test
  public void testHookUnregisterRejected() throws Exception {

    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.UNREGISTER) {
        be.complete(false);
        testComplete();
      } else {
        be.complete(true);
      }
    });
    testError(new JsonObject().put("type", "unregister").put("address", addr),
      "rejected");
    await();
  }

  @Test
  public void testHookUnregisterMissingAddress() throws Exception {
    sockJSHandler.bridge(allAccessOptions, be -> {
      if (be.type() == BridgeEventType.UNREGISTER) {
        be.getRawMessage().remove("address");
        testComplete();
      }
      be.complete(true);
    });
    testError(new JsonObject().put("type", "unregister").put("address", addr).put("body", "foobar"),
      "missing_address");
    await();
  }

  @Test
  public void testSendStringAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testSend("foobar");
  }

  @Test
  public void testSendJsonObjectAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testSend(new JsonObject().put("foo", "bar").put("blah", 123));
  }

  @Test
  public void testSendJsonArrayAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testSend(new JsonArray().add("foo").add(1456));
  }

  @Test
  public void testSendNumberAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testSend(13456);
  }

  @Test
  public void testSendBooleanTrueAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testSend(true);
  }

  @Test
  public void testSendBooleanFalseAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testSend(false);
  }

  @Test
  public void testPublishStringAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testPublish("foobar");
  }

  @Test
  public void testPublishJsonObjectAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testPublish(new JsonObject().put("foo", "bar").put("blah", 123));
  }

  @Test
  public void testPublishJsonArrayAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testPublish(new JsonArray().add("foo").add(1456));
  }

  @Test
  public void testPublishNumberAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testPublish(13456);
  }

  @Test
  public void testPublishBooleanTrueAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testPublish(true);
  }

  @Test
  public void testPublishBooleanFalseAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testPublish(false);
  }

  @Test
  public void testReceiveStringAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testReceive("foobar");
  }

  @Test
  public void testReceiveJsonObjectAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testReceive(new JsonObject().put("foo", "bar").put("blah", 123));
  }

  @Test
  public void testReceiveJsonArrayAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testReceive(new JsonArray().add("foo").add(1456));
  }

  @Test
  public void testReceiveNumberAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testReceive(13456);
  }

  @Test
  public void testReceiveBooleanTrueAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testReceive(true);
  }

  @Test
  public void testReceiveBooleanFalseAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testReceive(false);
  }

  @Test
  public void testUnregisterAllAccess() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testUnregister("someaddress");
  }

  @Test
  public void testInvalidType() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testError(new JsonObject().put("type", "wibble").put("address", "addr"), "invalid_type");
  }

  @Test
  public void testInvalidJson() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testError("oqiwjdioqwjdoiqjwd", "invalid_json");
  }

  @Test
  public void testMissingType() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testError(new JsonObject().put("address", "someaddress"), "missing_type");
  }

  @Test
  public void testMissingAddress() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testError(new JsonObject().put("type", "send").put("body", "hello world"), "missing_address");
  }

  @Test
  public void testSendNotPermittedDefaultOptions() throws Exception {
    sockJSHandler.bridge(defaultOptions);
    testError(new JsonObject().put("type", "send").put("address", addr).put("body", "hello world"),
      "access_denied");
  }

  @Test
  public void testSendPermittedAllowAddress() throws Exception {
    String addr = "allow1";
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddress(addr)));
    testSend(addr, "foobar");
    testError(new JsonObject().put("type", "send").put("address", "allow2").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testSendPermittedAllowAddressRe() throws Exception {
    String addr = "allo.+";
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddressRegex(addr)));
    testSend("allow1", "foobar");
    testSend("allow2", "foobar");
    testError(new JsonObject().put("type", "send").put("address", "hello").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testSendPermittedMultipleAddresses() throws Exception {
    String addr1 = "allow1";
    String addr2 = "allow2";
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddress(addr1)).
      addInboundPermitted(new PermittedOptions().setAddress(addr2)));
    testSend("allow1", "foobar");
    testSend("allow2", "foobar");
    testError(new JsonObject().put("type", "send").put("address", "allow3").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testSendPermittedMultipleAddressRe() throws Exception {
    String addr1 = "allo.+";
    String addr2 = "ballo.+";
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddressRegex(addr1)).
      addInboundPermitted(new PermittedOptions().setAddressRegex(addr2)));
    testSend("allow1", "foobar");
    testSend("allow2", "foobar");
    testSend("ballow1", "foobar");
    testSend("ballow2", "foobar");
    testError(new JsonObject().put("type", "send").put("address", "hello").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testSendPermittedMixedAddressRe() throws Exception {
    String addr1 = "allow1";
    String addr2 = "ballo.+";
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddress(addr1)).
      addInboundPermitted(new PermittedOptions().setAddressRegex(addr2)));
    testSend("allow1", "foobar");
    testSend("ballow1", "foobar");
    testSend("ballow2", "foobar");
    testError(new JsonObject().put("type", "send").put("address", "hello").put("body", "blah"),
      "access_denied");
    testError(new JsonObject().put("type", "send").put("address", "allow2").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testSendPermittedStructureMatch() throws Exception {
    JsonObject match = new JsonObject().put("fib", "wib").put("oop", 12);
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setMatch(match)));
    testSend(addr, match);
    JsonObject json1 = match.copy();
    json1.put("blah", "foob");
    testSend(addr, json1);
    json1.remove("fib");
    testError(new JsonObject().put("type", "send").put("address", addr).put("body", json1),
      "access_denied");
  }

  @Test
  public void testSendPermittedStructureMatchWithAddress() throws Exception {
    JsonObject match = new JsonObject().put("fib", "wib").put("oop", 12);
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setMatch(match).setAddress(addr)));
    testSend(addr, match);
    JsonObject json1 = match.copy();
    json1.put("blah", "foob");
    testSend(addr, json1);
    json1.remove("fib");
    testError(new JsonObject().put("type", "send").put("address", addr).put("body", json1),
      "access_denied");
    testError(new JsonObject().put("type", "send").put("address", "otheraddress").put("body", json1),
      "access_denied");
  }

  @Test
  public void testRegisterPermittedAllowAddress() throws Exception {
    String addr = "allow1";
    sockJSHandler.bridge(defaultOptions.addOutboundPermitted(new PermittedOptions().setAddress(addr)));
    testReceive(addr, "foobar");
    testError(new JsonObject().put("type", "register").put("address", "allow2").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testRegisterPermittedAllowAddressRe() throws Exception {
    String addr = "allo.+";
    sockJSHandler.bridge(defaultOptions.addOutboundPermitted(new PermittedOptions().setAddressRegex(addr)));
    testReceive("allow1", "foobar");
    testReceive("allow2", "foobar");
    testError(new JsonObject().put("type", "register").put("address", "hello").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testRegisterPermittedMultipleAddresses() throws Exception {
    String addr1 = "allow1";
    String addr2 = "allow2";
    sockJSHandler.bridge(defaultOptions.addOutboundPermitted(new PermittedOptions().setAddress(addr1)).
      addOutboundPermitted(new PermittedOptions().setAddress(addr2)));
    testReceive("allow1", "foobar");
    testReceive("allow2", "foobar");
    testError(new JsonObject().put("type", "register").put("address", "allow3").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testRegisterPermittedMultipleAddressRe() throws Exception {
    String addr1 = "allo.+";
    String addr2 = "ballo.+";
    sockJSHandler.bridge(defaultOptions.addOutboundPermitted(new PermittedOptions().setAddressRegex(addr1)).
      addOutboundPermitted(new PermittedOptions().setAddressRegex(addr2)));
    testReceive("allow1", "foobar");
    testReceive("allow2", "foobar");
    testReceive("ballow1", "foobar");
    testReceive("ballow2", "foobar");
    testError(new JsonObject().put("type", "register").put("address", "hello").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testRegisterPermittedMixedAddressRe() throws Exception {
    String addr1 = "allow1";
    String addr2 = "ballo.+";
    sockJSHandler.bridge(defaultOptions.addOutboundPermitted(new PermittedOptions().setAddress(addr1)).
      addOutboundPermitted(new PermittedOptions().setAddressRegex(addr2)));
    testReceive("allow1", "foobar");
    testReceive("ballow1", "foobar");
    testReceive("ballow2", "foobar");
    testError(new JsonObject().put("type", "register").put("address", "hello").put("body", "blah"),
      "access_denied");
    testError(new JsonObject().put("type", "register").put("address", "allow2").put("body", "blah"),
      "access_denied");
  }

  @Test
  public void testRegisterPermittedStructureMatch() throws Exception {
    JsonObject match = new JsonObject().put("fib", "wib").put("oop", 12);
    sockJSHandler.bridge(defaultOptions.addOutboundPermitted(new PermittedOptions().setMatch(match)));
    testReceive(addr, match);
    JsonObject json1 = match.copy();
    json1.put("blah", "foob");
    testReceive(addr, json1);
    JsonObject json2 = json1.copy();
    json2.remove("fib");
    testReceiveFail(addr, json2);
  }


  @Test
  public void testRegisterPermittedStructureMatchWithAddress() throws Exception {
    JsonObject match = new JsonObject().put("fib", "wib").put("oop", 12);
    sockJSHandler.bridge(defaultOptions.addOutboundPermitted(new PermittedOptions().setMatch(match).setAddress(addr)));
    testReceive(addr, match);
    JsonObject json1 = match.copy();
    json1.put("blah", "foob");
    testReceive(addr, json1);
    JsonObject json2 = json1.copy();
    json2.remove("fib");
    testReceiveFail(addr, json2);
  }

  @Test
  public void testReplyMessagesInbound() throws Exception {

    // Only allow inbound address, reply message should still get through though
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddress(addr)));

    CountDownLatch latch = new CountDownLatch(1);

    client.websocket(websocketURI, ws -> {

      MessageConsumer<Object> consumer = vertx.eventBus().consumer(addr);

      consumer.handler(msg -> {
        Object receivedBody = msg.body();
        assertEquals("foobar", receivedBody);
        msg.reply("barfoo");
        consumer.unregister();
      });

      String replyAddress = UUID.randomUUID().toString();

      JsonObject msg = new JsonObject().put("type", "send").put("address", addr).put("replyAddress", replyAddress).put("body", "foobar");

      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        Object rec = received.getValue("body");
        assertEquals("barfoo", rec);
        ws.closeHandler(v -> latch.countDown());
        ws.close();
      });

    });

    awaitLatch(latch);
  }

  @Test
  public void testReplyMessagesInboundWithHeaders() throws Exception {

    // Only allow inbound address, reply message should still get through though
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddress(addr)));

    CountDownLatch latch = new CountDownLatch(1);

    client.websocket(websocketURI, ws -> {

      MessageConsumer<Object> consumer = vertx.eventBus().consumer(addr);

      consumer.handler(msg -> {
        Object receivedBody = msg.body();
        assertEquals("foobar", receivedBody);
        msg.reply("barfoo", new DeliveryOptions().addHeader("headfoo", "headbar").addHeader("explode", "m1").addHeader("explode", "m2"));
        consumer.unregister();
      });

      String replyAddress = UUID.randomUUID().toString();

      JsonObject msg = new JsonObject().put("type", "send").put("address", addr).put("replyAddress", replyAddress).put("body", "foobar");

      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        Object rec = received.getValue("body");
        assertEquals("barfoo", rec);
        JsonObject headers = received.getJsonObject("headers");
        assertNotNull(headers);
        assertEquals("headbar", headers.getString("headfoo"));
        assertTrue(headers.getJsonArray("explode").contains("m1"));
        assertTrue(headers.getJsonArray("explode").contains("m2"));
        ws.closeHandler(v -> latch.countDown());
        ws.close();
      });

    });

    awaitLatch(latch);
  }

  @Test
  public void testReplyMessagesOutbound() throws Exception {

    // Only allow outbound address, reply message should still get through though
    sockJSHandler.bridge(defaultOptions.addOutboundPermitted(new PermittedOptions().setAddress(addr)));

    CountDownLatch latch = new CountDownLatch(1);

    client.websocket(websocketURI, ws -> {

      JsonObject reg = new JsonObject().put("type", "register").put("address", addr);
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(reg.encode(), true));

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        Object rec = received.getValue("body");
        assertEquals("foobar", rec);

        // Now send back reply
        JsonObject reply = new JsonObject().put("type", "send").put("address", received.getString("replyAddress")).put("body", "barfoo");
        ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(reply.encode(), true));
      });

      vertx.setTimer(500, tid -> vertx.eventBus().send(addr, "foobar", res -> {
        if (res.succeeded()) {
          assertEquals("barfoo", res.result().body());
          ws.closeHandler(v2 -> latch.countDown());
          ws.close();
        }
      }));

    });

    awaitLatch(latch);
  }

  @Test
  public void testReplyToClientTimeout() throws Exception {

    sockJSHandler.bridge(allAccessOptions.setReplyTimeout(200));

    CountDownLatch latch = new CountDownLatch(1);

    client.websocket(websocketURI, ws -> {

      MessageConsumer<Object> consumer = vertx.eventBus().consumer(addr);

      consumer.handler(msg -> {
        Object receivedBody = msg.body();
        assertEquals("foobar", receivedBody);
        vertx.setTimer(500, tid -> {
          msg.reply("barfoo");
          consumer.unregister();
        });
      });

      String replyAddress = UUID.randomUUID().toString();

      JsonObject msg = new JsonObject().put("type", "send").put("address", addr).put("replyAddress", replyAddress).put("body", "foobar");

      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        Object rec = received.getValue("failureType");
        assertEquals("TIMEOUT", rec);
        ws.closeHandler(v -> latch.countDown());
        ws.close();
      });

    });

    awaitLatch(latch);
  }

  @Test
  public void testAwaitingReplyToClientTimeout() throws Exception {

    sockJSHandler.bridge(allAccessOptions.setReplyTimeout(200));

    CountDownLatch latch = new CountDownLatch(1);

    client.websocket(websocketURI, ws -> {

      MessageConsumer<Object> consumer = vertx.eventBus().consumer(addr);

      consumer.handler(msg -> {
        Object receivedBody = msg.body();
        assertEquals("one", receivedBody);
        msg.reply("two", rep -> {
          assertTrue(rep.succeeded());
          Object repReceivedBody = rep.result().body();
          assertEquals("three", repReceivedBody);
          vertx.setTimer(500, tid -> {
            rep.result().reply("four");
            consumer.unregister();
          });
        });
      });

      String replyAddress = UUID.randomUUID().toString();

      JsonObject msg = new JsonObject().put("type", "send").put("address", addr).put("replyAddress", replyAddress).put("body", "one");

      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        Object rec = received.getValue("body");
        assertEquals("two", rec);

        String secondReplyAddress = UUID.randomUUID().toString();
        JsonObject rep_msg = new JsonObject().put("type", "send").put("address", received.getValue("replyAddress")).put("replyAddress", secondReplyAddress).put("body", "three");
        ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(rep_msg.encode(), true));

        ws.handler(repBuff -> {
          String repStr = repBuff.toString();
          JsonObject repReceived = new JsonObject(repStr);
          Object repRec = repReceived.getValue("failureType");
          assertEquals("TIMEOUT", repRec);
          ws.closeHandler(v -> latch.countDown());
          ws.close();
        });
      });

    });

    awaitLatch(latch);
  }

  @Test
  public void testRegisterNotPermittedDefaultOptions() throws Exception {
    sockJSHandler.bridge(defaultOptions);
    testError(new JsonObject().put("type", "register").put("address", addr),
      "access_denied");
  }

  @Test
  public void testUnregisterNotPermittedDefaultOptions() throws Exception {
    sockJSHandler.bridge(defaultOptions);
    testError(new JsonObject().put("type", "unregister").put("address", addr),
      "access_denied");
  }

  @Test
  public void testMaxHandlersPerSocket() throws Exception {

    int maxHandlers = 10;

    CountDownLatch latch = new CountDownLatch(1);

    sockJSHandler.bridge(new BridgeOptions(allAccessOptions).setMaxHandlersPerSocket(maxHandlers));

    client.websocket(websocketURI, ws -> {

      for (int i = 0; i < maxHandlers + 1; i++) {
        JsonObject msg = new JsonObject().put("type", "register").put("address", addr);
        ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));
      }

      AtomicInteger cnt = new AtomicInteger();

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        Object rec = received.getValue("body");
        int c = cnt.getAndIncrement();
        if (c == 0) {
          assertEquals("err", received.getString("type"));
          assertEquals("max_handlers_reached", rec);
        } else if (c >= maxHandlers + 1) {
          fail("Called too many times");
        } else {
          assertEquals("rec", received.getString("type"));
          assertEquals("foobar", rec);
          if (c == maxHandlers) {
            vertx.setTimer(200, tid -> latch.countDown());
          }
        }
      });

      JsonObject msg = new JsonObject().put("type", "publish").put("address", addr).put("body", "foobar");
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

    });

    awaitLatch(latch);

  }

  @Test
  public void testMaxAddressLength() throws Exception {

    CountDownLatch latch = new CountDownLatch(1);

    sockJSHandler.bridge(new BridgeOptions(allAccessOptions).setMaxAddressLength(10));

    client.websocket(websocketURI, ws -> {

      JsonObject msg = new JsonObject().put("type", "register").put("address", "someaddressyqgyuqwdyudyug");
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        assertEquals("err", received.getString("type"));
        assertEquals("max_address_length_reached", received.getString("body"));
        latch.countDown();
      });

    });

    awaitLatch(latch);
  }

  @Test
  public void testSendRequiresAuthorityNotLoggedIn() throws Exception {
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddress(addr).setRequiredAuthority("admin")));
    testError(new JsonObject().put("type", "send").put("address", addr).put("body", "foo"), "not_logged_in");
  }

  @Test
  public void testSendRequiresAuthorityHasAuthority() throws Exception {
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddress(addr).setRequiredAuthority("bang_sticks")));
    router.clear();
    router.route().handler(CookieHandler.create());
    SessionStore store = LocalSessionStore.create(vertx);
    router.route().handler(SessionHandler.create(store));
    JsonObject authConfig = new JsonObject().put("properties_path", "classpath:login/loginusers.properties");
    AuthProvider authProvider = ShiroAuth.create(vertx, new ShiroAuthOptions().setType(ShiroAuthRealmType.PROPERTIES).setConfig(authConfig));
    addLoginHandler(router, authProvider);
    router.route("/eventbus/*").handler(sockJSHandler);
    testSend("foo");
  }

  @Test
  public void testSendRequiresAuthorityHasnotAuthority() throws Exception {
    sockJSHandler.bridge(defaultOptions.addInboundPermitted(new PermittedOptions().setAddress(addr).setRequiredAuthority("pick_nose")));
    router.clear();
    router.route().handler(CookieHandler.create());
    SessionStore store = LocalSessionStore.create(vertx);
    router.route().handler(SessionHandler.create(store));
    JsonObject authConfig = new JsonObject().put("properties_path", "classpath:login/loginusers.properties");
    AuthProvider authProvider = ShiroAuth.create(vertx, new ShiroAuthOptions().setType(ShiroAuthRealmType.PROPERTIES).setConfig(authConfig));
    addLoginHandler(router, authProvider);
    router.route("/eventbus/*").handler(sockJSHandler);
    testError(new JsonObject().put("type", "send").put("address", addr).put("body", "foo"), "access_denied");
  }

  private void addLoginHandler(Router router, AuthProvider authProvider) {
    router.route("/eventbus/*").handler(rc -> {
      // we need to be logged in
      if (rc.user() == null) {
        JsonObject authInfo = new JsonObject().put("username", "tim").put("password", "delicious:sausages");
        authProvider.authenticate(authInfo, res -> {
          if (res.succeeded()) {
            rc.setUser(res.result());
            rc.next();
          } else {
            rc.fail(res.cause());
          }
        });
      }
    });
  }

  @Test
  public void testInvalidClientReplyAddress() throws Exception {
    sockJSHandler.bridge(allAccessOptions);
    testError(new JsonObject().put("type", "send").put("address", addr).put("body", "foo")
      .put("replyAddress", "thishasmorethan36characters__________"), "invalid_reply_address");
  }

  @Test
  public void testConnectionClosedAfterPingTimeout() throws Exception {
    sockJSHandler.bridge(allAccessOptions.setPingTimeout(1000));
    CountDownLatch latch = new CountDownLatch(1);
    long start = System.currentTimeMillis();
    client.websocket(websocketURI, ws -> ws.closeHandler(v -> latch.countDown()));
    awaitLatch(latch);
    long dur = System.currentTimeMillis() - start;
    assertTrue(dur > 1000 && dur < 3000);
  }

  @Test
  public void testPermittedOptions() {
    PermittedOptions options = new PermittedOptions();
    assertEquals(PermittedOptions.DEFAULT_ADDRESS, options.getAddress());
    assertEquals(PermittedOptions.DEFAULT_ADDRESS_REGEX, options.getAddressRegex());
    assertEquals(PermittedOptions.DEFAULT_REQUIRED_AUTHORITY, options.getRequiredAuthority());
    assertEquals(PermittedOptions.DEFAULT_MATCH, options.getMatch());
    String address = TestUtils.randomAlphaString(10);
    String addressRegex = TestUtils.randomAlphaString(10);
    String requiredAuthority = TestUtils.randomAlphaString(10);
    JsonObject match = new JsonObject().put(TestUtils.randomAlphaString(10), TestUtils.randomAlphaString(10));
    assertSame(options, options.setAddress(address));
    assertSame(options, options.setAddressRegex(addressRegex));
    assertSame(options, options.setRequiredAuthority(requiredAuthority));
    assertSame(options, options.setMatch(match));
    assertEquals(address, options.getAddress());
    assertEquals(addressRegex, options.getAddressRegex());
    assertEquals(requiredAuthority, options.getRequiredAuthority());
    assertEquals(match, options.getMatch());
    PermittedOptions copy = new PermittedOptions(options);
    assertEquals(address, copy.getAddress());
    assertEquals(addressRegex, copy.getAddressRegex());
    assertEquals(requiredAuthority, copy.getRequiredAuthority());
    assertEquals(match, copy.getMatch());
    assertSame(copy, copy.setAddress(TestUtils.randomAlphaString(10)));
    assertSame(copy, copy.setAddressRegex(TestUtils.randomAlphaString(10)));
    assertSame(copy, copy.setRequiredAuthority(TestUtils.randomAlphaString(10)));
    assertSame(copy, copy.setMatch(new JsonObject().put(TestUtils.randomAlphaString(10), TestUtils.randomAlphaString(10))));
    assertSame(options, options.setAddress(address));
    assertSame(options, options.setAddressRegex(addressRegex));
    assertSame(options, options.setRequiredAuthority(requiredAuthority));
    assertSame(options, options.setMatch(match));
  }

  @Test
  public void testPermittedOptionsJson() {
    String address = TestUtils.randomAlphaString(10);
    String addressRegex = TestUtils.randomAlphaString(10);
    String requiredAuthority = TestUtils.randomAlphaString(10);
    JsonObject match = new JsonObject().put(TestUtils.randomAlphaString(10), TestUtils.randomAlphaString(10));
    JsonObject json = new JsonObject().
      put("address", address).
      put("addressRegex", addressRegex).
      put("requiredAuthority", requiredAuthority).
      put("match", match);
    PermittedOptions options = new PermittedOptions(json);
    assertEquals(address, options.getAddress());
    assertEquals(addressRegex, options.getAddressRegex());
    assertEquals(requiredAuthority, options.getRequiredAuthority());
    assertEquals(match, options.getMatch());
  }

  private void testError(JsonObject msg, String expectedErr) throws Exception {
    testError(msg.encode(), expectedErr);
  }

  private void testError(String msg, String expectedErr) throws Exception {

    CountDownLatch latch = new CountDownLatch(1);

    client.websocket(websocketURI, ws -> {

      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg, true));

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        assertEquals("err", received.getString("type"));
        assertEquals(expectedErr, received.getString("body"));
        latch.countDown();
      });
    });

    awaitLatch(latch);
  }

  private void testSend(Object body) throws Exception {
    testSend(addr, body);
  }

  private void testSend(String address, Object body) throws Exception {
    testSend(address, body, false);
  }

  private void testSend(String address, Object body, boolean headers) throws Exception {

    CountDownLatch latch = new CountDownLatch(1);

    client.websocket(websocketURI, ws -> {

      MessageConsumer<Object> consumer = vertx.eventBus().consumer(address);

      consumer.handler(msg -> {
        Object receivedBody = msg.body();
        assertEquals(body, receivedBody);
        if (headers) {
          checkHeaders(msg);
        }
        consumer.unregister(v -> latch.countDown());
      });

      JsonObject msg = new JsonObject().put("type", "send").put("address", address).put("body", body);
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

    });

    awaitLatch(latch);

  }

  private void testPublish(Object body) throws Exception {
    testPublish(addr, body);
  }

  private void testPublish(String address, Object body) throws Exception {
    testPublish(address, body, false);
  }

  private void checkHeaders(Message msg) {
    assertEquals("val1", msg.headers().get("hdr1"));
    assertEquals("val2", msg.headers().get("hdr2"));
  }

  private void testPublish(String address, Object body, boolean headers) throws Exception {
    CountDownLatch latch = new CountDownLatch(2);

    client.websocket(websocketURI, ws -> {

      vertx.eventBus().consumer(address, msg -> {
        Object receivedBody = msg.body();
        assertEquals(body, receivedBody);
        if (headers) {
          checkHeaders(msg);
        }
        latch.countDown();
      });

      vertx.eventBus().consumer(address, msg -> {
        Object receivedBody = msg.body();
        assertEquals(body, receivedBody);
        if (headers) {
          checkHeaders(msg);
        }
        latch.countDown();
      });

      JsonObject msg = new JsonObject().put("type", "publish").put("address", address).put("body", body);
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

    });

    awaitLatch(latch);
  }

  private void testReceive(Object body) throws Exception {
    testReceive("someaddress", body);
  }

  private void testReceive(String address, Object body) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    client.websocket(websocketURI, ws -> {

      // Register
      JsonObject msg = new JsonObject().put("type", "register").put("address", address);
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        assertEquals("rec", received.getString("type"));
        Object rec = received.getValue("body");
        assertEquals(body, rec);
        ws.closeHandler(v -> latch.countDown());
        ws.close();
      });

      // Wait a bit to allow the handler to be setup on the server, then send message from eventbus
      vertx.setTimer(200, tid -> vertx.eventBus().send(address, body));
    });
    awaitLatch(latch);
  }

  private void testReceiveFail(String address, Object body) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    client.websocket(websocketURI, ws -> {

      // Register
      JsonObject msg = new JsonObject().put("type", "register").put("address", address);
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

      ws.handler(buff -> fail("Shouldn't receive anything"));

      // Wait a bit to allow the handler to be setup on the server, then send message from eventbus
      vertx.setTimer(200, tid -> {
        vertx.eventBus().send(address, body);
        vertx.setTimer(200, tid2 -> latch.countDown());
      });
    });
    awaitLatch(latch);
  }

  private void testUnregister(String address) throws Exception {

    CountDownLatch latch = new CountDownLatch(1);

    client.websocket(websocketURI, ws -> {

      // Register
      JsonObject msg = new JsonObject().put("type", "register").put("address", address);
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

      ws.handler(buff -> {
        String str = buff.toString();
        JsonObject received = new JsonObject(str);
        assertEquals("rec", received.getString("type"));
        Object rec = received.getValue("body");
        assertEquals("foobar", rec);

        // Now unregister
        JsonObject msg2 = new JsonObject().put("type", "unregister").put("address", address);
        ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg2.encode(), true));

        // Send again
        msg2 = new JsonObject().put("type", "send").put("address", address).put("body", "foobar2");
        ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg2.encode(), true));

        // We shouldn't receive the second message, give it a little time to come through
        vertx.setTimer(500, tid -> latch.countDown());

      });

      msg = new JsonObject().put("type", "send").put("address", address).put("body", "foobar");
      ws.writeFrame(io.vertx.core.http.WebSocketFrame.textFrame(msg.encode(), true));

    });

    awaitLatch(latch);
  }


}
