/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.ext.web.tests.handler;

import io.vertx.core.Completable;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.RegistrationInfo;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.tests.handler.EventbusBridgeTest.BridgeClient;
import io.vertx.ext.web.tests.handler.EventbusBridgeTest.Transport;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTest;
import io.vertx.junit5.VertxTestContext;
import io.vertx.test.core.TestUtils;
import io.vertx.test.fakecluster.FakeClusterManager;
import io.vertx.tests.eventbus.WrappedClusterManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@VertxTest
public abstract class SlowClusterEventbusBridgeTest {

  protected final Transport transport;

  protected Vertx vertx;
  protected VertxInternal node1;
  protected VertxInternal node2;
  protected Router router;
  protected HttpServer server;
  protected WebSocketClient wsClient;
  protected SockJSHandler sockJS;

  public SlowClusterEventbusBridgeTest(Transport transport) {
    this.transport = transport;
  }

  @BeforeEach
  public void setUp(Vertx vertx) throws Exception {
    this.vertx = vertx;
    SlowClusterManager clusterManager = new SlowClusterManager(vertx);
    node1 = (VertxInternal) Vertx.builder().withClusterManager(clusterManager).buildClustered().await();
    node2 = (VertxInternal) Vertx.builder().withClusterManager(clusterManager).buildClustered().await();
    router = Router.router(node1);
    server = node1.createHttpServer();
    server.requestHandler(router).listen(0).await();
    wsClient = node1.createWebSocketClient(new WebSocketClientOptions().setDefaultPort(server.actualPort()));
    sockJS = SockJSHandler.create(node1);
  }

  @AfterEach
  public void tearDown() {
    try {
      if (wsClient != null) {
        wsClient.close().await();
      }
      if (server != null) {
        server.close().await();
      }
    } catch (Exception e) {
      // Ignore if already closed
    }
    if (node2 != null) {
      node2.close().await();
    }
    if (node1 != null) {
      node1.close().await();
    }
  }

  @Test
  public void testRegistration(VertxTestContext testContext) throws Exception {
    String payload = "hello slinkydeveloper!";
    String addr = "someaddress";
    String websocketURI = "/eventbus/websocket";

    AtomicInteger step = new AtomicInteger();

    SockJSBridgeOptions allAccessOptions = new SockJSBridgeOptions()
      .addInboundPermitted(new PermittedOptions())
      .addOutboundPermitted(new PermittedOptions());

    // 1. Check if REGISTER hook is called
    // 2. Check if REGISTERED hook is called
    // 3. Try to send a message while managing REGISTERED event
    // 4. Check if bridgeClient receives the message
    router.route("/eventbus/*").subRouter(
      sockJS.bridge(allAccessOptions, be -> {
        if (be.type() == BridgeEventType.REGISTER) {
          assertTrue(step.compareAndSet(0, 1));
          assertNotNull(be.socket());
          JsonObject raw = be.getRawMessage();
          assertEquals(addr, raw.getString("address"));
        } else if (be.type() == BridgeEventType.REGISTERED) {
          assertTrue(step.compareAndSet(1, 2));
          assertNotNull(be.socket());
          JsonObject raw = be.getRawMessage();
          assertEquals(addr, raw.getString("address"));

          // The bridgeClient should be able to receive this message
          node2.eventBus().send(addr, payload);
        }
        be.complete(true);
      }));

    BridgeClient bridgeClient = new BridgeClient(wsClient, transport);

    Checkpoint checkpoint = testContext.checkpoint();

    bridgeClient.handler((address, received) -> {
      assertTrue(step.compareAndSet(2, 3));
      assertEquals(addr, address);
      assertEquals(payload, received.getString("body"));
      bridgeClient.close().onComplete(TestUtils.onSuccess(v -> checkpoint.flag()));
    });

    bridgeClient
      .connect(websocketURI)
      .compose(v -> bridgeClient.register(addr))
      .await();
  }

  @Test
  public void testNoOrphanClusteredSubscription(VertxTestContext testContext) throws Exception {
    String addr = "someaddress";
    String websocketURI = "/eventbus/websocket";

    SockJSBridgeOptions allAccessOptions = new SockJSBridgeOptions()
      .addInboundPermitted(new PermittedOptions())
      .addOutboundPermitted(new PermittedOptions());

    router.route("/eventbus/*").subRouter(sockJS.bridge(allAccessOptions));

    BridgeClient bridgeClient = new BridgeClient(wsClient, transport);

    bridgeClient
      .connect(websocketURI)
      .compose(v -> bridgeClient.register(addr))
      .compose(v -> bridgeClient.unregister(addr))
      .onComplete(TestUtils.onSuccess(v -> {
        Promise<List<RegistrationInfo>> promise = Promise.promise();
        node1.setTimer(1500, l -> {
          node1.clusterManager().getRegistrations(addr, promise);
          promise.future().onComplete(TestUtils.onSuccess(registrationInfos -> {
            assertTrue(registrationInfos == null || registrationInfos.isEmpty());
            testContext.completeNow();
          }));
        });
      }));
  }

  private static class SlowClusterManager extends WrappedClusterManager {

    final Vertx vertx;

    SlowClusterManager(Vertx vertx) {
      super(new FakeClusterManager());
      this.vertx = vertx;
    }

    @Override
    public void addRegistration(String address, RegistrationInfo registrationInfo, Completable<Void> promise) {
      vertx.setTimer(1000, l -> {
        super.addRegistration(address, registrationInfo, promise);
      });
    }
  }
}
