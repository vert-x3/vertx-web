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

package io.vertx.ext.web.sstore;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.ProtocolUpgradeHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.SessionHandlerTestBase;
import io.vertx.ext.web.sstore.impl.SharedDataSessionImpl;
import io.vertx.test.core.TestUtils;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClusteredSessionHandlerTest extends SessionHandlerTestBase {

  int numNodes = 3;
  byte[] bytes = TestUtils.randomByteArray(100);
  Buffer buffer = TestUtils.randomBuffer(100);

  HttpServer[] servers = new HttpServer[3];

  @Override
  public void setUp() throws Exception {
    super.setUp();
    startNodes(numNodes);
    store = ClusteredSessionStore.create(vertices[0], 3000);
    servers[0] = null;
    servers[1] = null;
    servers[2] = null;
  }

  @Override
  public void tearDown() throws Exception {
    for (HttpServer server : servers) {
      if (server != null) {
        CountDownLatch latch = new CountDownLatch(1);
        server.close((asyncResult) -> {
          assertTrue(asyncResult.succeeded());
          latch.countDown();
        });
        awaitLatch(latch);
      }
    }
    super.tearDown();
  }

  @Override
  protected ClusterManager getClusterManager() {
    return new FakeClusterManager();
  }

  @Test
  public void testClusteredSession() throws Exception {
    CountDownLatch serversReady = new CountDownLatch(3);

    Router router1 = Router.router(vertices[0]);
    SessionStore store1 = ClusteredSessionStore.create(vertices[0]);
    SessionHandler sessionHandler1 = SessionHandler.create(store1);
    router1.route().handler(sessionHandler1);
    servers[0] = vertices[0].createHttpServer(new HttpServerOptions().setPort(8081).setHost("localhost"));
    servers[0].requestHandler(router1);
    servers[0].listen(onSuccess(s -> serversReady.countDown()));

    Router router2 = Router.router(vertices[1]);
    SessionStore store2 = ClusteredSessionStore.create(vertices[1]);
    SessionHandler sessionHandler2 = SessionHandler.create(store2);
    router2.route().handler(sessionHandler2);
    servers[1] = vertices[1].createHttpServer(new HttpServerOptions().setPort(8082).setHost("localhost"));
    servers[1].requestHandler(router2);
    servers[1].listen(onSuccess(s -> serversReady.countDown()));

    Router router3 = Router.router(vertices[2]);
    SessionStore store3 = ClusteredSessionStore.create(vertices[2]);
    SessionHandler sessionHandler3 = SessionHandler.create(store3);
    router3.route().handler(sessionHandler3);
    servers[2] = vertices[2].createHttpServer(new HttpServerOptions().setPort(8083).setHost("localhost"));
    servers[2].requestHandler(router3);
    servers[2].listen(onSuccess(s -> serversReady.countDown()));

    awaitLatch(serversReady);

    router1.route().handler(rc -> {
      Session sess = rc.session();
      sess.put("foo", "bar");
      stuffSession(sess);
      rc.request().pause();
      sessionHandler1.flush(rc)
        .onFailure(err -> {
          rc.request().resume();
          rc.fail(err);
        })
        .onSuccess(v -> {
          rc.request().resume();
          rc.response().end();
        });
    });

    router2.route().handler(rc -> {
      Session sess = rc.session();
      checkSession(sess);
      assertEquals("bar", sess.get("foo"));
      sess.put("eek", "wibble");
      rc.request().pause();
      sessionHandler2.flush(rc)
        .onFailure(err -> {
          rc.request().resume();
          rc.fail(err);
        })
        .onSuccess(v -> {
          rc.request().resume();
          rc.response().end();
        });
    });

    router3.route().handler(rc -> {
      Session sess = rc.session();
      checkSession(sess);
      assertEquals("bar", sess.get("foo"));
      assertEquals("wibble", sess.get("eek"));
      rc.request().pause();
      sessionHandler3.flush(rc)
        .onFailure(err -> {
          rc.request().resume();
          rc.fail(err);
        })
        .onSuccess(v -> {
          rc.request().resume();
          rc.response().end();
        });
    });

    AtomicReference<String> rSetCookie = new AtomicReference<>();
    testRequestBuffer(client, HttpMethod.GET, 8081, "/", null, resp -> {
      String setCookie = resp.headers().get("set-cookie");
      rSetCookie.set(setCookie);
    }, 200, "OK", null);
    testRequestBuffer(client, HttpMethod.GET, 8082, "/", req -> req.putHeader("cookie", rSetCookie.get()), null, 200, "OK", null);
    testRequestBuffer(client, HttpMethod.GET, 8083, "/", req -> req.putHeader("cookie", rSetCookie.get()), null, 200, "OK", null);
  }

  @Test
  public void testSessionSerializationNullPrincipal() {
    long timeout = 123;
    SharedDataSessionImpl session = (SharedDataSessionImpl) store.createSession(timeout);
    session.setAccessed();
    long lastAccessed = session.lastAccessed();
    stuffSession(session);
    checkSession(session);
    Buffer buffer = Buffer.buffer();
    session.writeToBuffer(buffer);
    SharedDataSessionImpl session2 = (SharedDataSessionImpl) store.createSession(0);
    session2.readFromBuffer(0, buffer);
    checkSession(session2);
    assertEquals(timeout, session2.timeout());
    assertEquals(lastAccessed, session2.lastAccessed());
    assertEquals(session.id(), session2.id());
  }

  private void stuffSession(Session session) {
    session.put("somelong", 123456L);
    session.put("someint", 1234);
    session.put("someshort", (short) 123);
    session.put("somebyte", (byte) 12);
    session.put("somedouble", 123.456d);
    session.put("somefloat", 123.456f);
    session.put("somechar", 'X');
    session.put("somebooleantrue", true);
    session.put("somebooleanfalse", false);
    session.put("somestring", "wibble");
    session.put("somebytes", bytes);
    session.put("somebuffer", buffer);
    session.put("someclusterserializable", new JsonObject().put("foo", "bar"));
  }

  private void checkSession(Session session) {
    assertEquals(123456L, (long) session.get("somelong"));
    assertEquals(1234, (int) session.get("someint"));
    assertEquals((short) 123, (short) session.get("someshort"));
    assertEquals((byte) 12, (byte) session.get("somebyte"));
    assertEquals(123.456d, (double) session.get("somedouble"), 0);
    assertEquals(123.456f, (float) session.get("somefloat"), 0);
    assertEquals('X', (char) session.get("somechar"));
    assertTrue(session.get("somebooleantrue"));
    assertFalse(session.get("somebooleanfalse"));
    assertEquals("wibble", session.get("somestring"));
    assertTrue(TestUtils.byteArraysEqual(bytes, session.get("somebytes")));
    assertEquals(buffer, session.get("somebuffer"));
    JsonObject json = session.get("someclusterserializable");
    assertNotNull(json);
    assertEquals("bar", json.getString("foo"));
  }

  @Test
  public void testRetryTimeout() throws Exception {
    long val = doTestSessionRetryTimeout();
    assertTrue(String.valueOf(val), val >= 2500 && val < 5000);
  }

  @Test
  public void testDelayedLookupWithRequestUpgrade() throws InterruptedException {
    String sessionCookieName = "session";
    CountDownLatch serversReady = new CountDownLatch(3);
    CountDownLatch testsComplete = new CountDownLatch(3);

    ProtocolUpgradeHandler upgradeHandler = ctx ->
      ctx.request()
        .toWebSocket()
        .onFailure(this::fail)
        .onSuccess(serverWebSocket -> {
          System.out.println("Upgrade successful");
          serverWebSocket.textMessageHandler(msg -> {
            System.out.println("WS txt message receive successful");
            assertEquals("foo", msg);
            testsComplete.countDown();
          });
        });

    Router router1 = Router.router(vertices[0]);
    SessionStore store1 = ClusteredSessionStore.create(vertices[0]);
    SessionHandler sessionHandler1 = SessionHandler.create(store1).setSessionCookieName(sessionCookieName).setMinLength(0);
    router1.route()
      .handler(sessionHandler1)
      .handler(upgradeHandler);
    servers[0] = vertices[0].createHttpServer(new HttpServerOptions().setPort(8081).setHost("localhost"));
    servers[0].requestHandler(router1);
    servers[0].listen(onSuccess(s -> serversReady.countDown()));

    Router router2 = Router.router(vertices[1]);
    SessionStore store2 = ClusteredSessionStore.create(vertices[1]);
    SessionHandler sessionHandler2 = SessionHandler.create(store2).setSessionCookieName(sessionCookieName).setMinLength(0);
    router1.route()
      .handler(sessionHandler2)
      .handler(upgradeHandler);
    servers[1] = vertices[1].createHttpServer(new HttpServerOptions().setPort(8082).setHost("localhost"));
    servers[1].requestHandler(router2);
    servers[1].listen(onSuccess(s -> serversReady.countDown()));

    Router router3 = Router.router(vertices[2]);
    SessionStore store3 = ClusteredSessionStore.create(vertices[2]);
    SessionHandler sessionHandler3 = SessionHandler.create(store3).setSessionCookieName(sessionCookieName).setMinLength(0);
    router1.route()
      .handler(sessionHandler3)
      .handler(upgradeHandler);
    servers[2] = vertices[2].createHttpServer(new HttpServerOptions().setPort(8083).setHost("localhost"));
    servers[2].requestHandler(router3);
    servers[2].listen(onSuccess(s -> serversReady.countDown()));

    awaitLatch(serversReady);


    WebSocketConnectOptions options1 = new WebSocketConnectOptions()
      .setURI("/")
      .setPort(8081)
      .setHost("localhost")
      .addHeader("cookie", sessionCookieName + "=" + TestUtils.randomAlphaString(32));

    client.webSocket(options1, onSuccess(ws -> {
      System.out.println("WS connection successful");
      ws.writeTextMessage("foo", onSuccess(v -> {
        System.out.println("WS txt message write successful");
      }));
    }));

    WebSocketConnectOptions options2 = new WebSocketConnectOptions()
      .setURI("/")
      .setPort(8081)
      .setHost("localhost")
      .addHeader("cookie", sessionCookieName + "=" + TestUtils.randomAlphaString(32));

    client.webSocket(options2, onSuccess(ws -> {
      System.out.println("WS connection successful");
      ws.writeTextMessage("foo", onSuccess(v -> {
        System.out.println("WS txt message write successful");
      }));
    }));

    WebSocketConnectOptions options3 = new WebSocketConnectOptions()
      .setURI("/")
      .setPort(8081)
      .setHost("localhost")
      .addHeader("cookie", sessionCookieName + "=" + TestUtils.randomAlphaString(32));

    client.webSocket(options3, onSuccess(ws -> {
      System.out.println("WS connection successful");
      ws.writeTextMessage("foo", onSuccess(v -> {
        System.out.println("WS txt message write successful");
      }));
    }));

    awaitLatch(testsComplete);
  }
}
