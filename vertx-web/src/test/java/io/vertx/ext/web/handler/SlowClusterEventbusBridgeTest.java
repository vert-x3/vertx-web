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

package io.vertx.ext.web.handler;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.RegistrationInfo;
import io.vertx.core.spi.cluster.WrappedClusterManager;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.EventbusBridgeTest.BridgeClient;
import io.vertx.ext.web.handler.EventbusBridgeTest.Transport;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.test.core.VertxTestBase;
import io.vertx.test.fakecluster.FakeClusterManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

@RunWith(Parameterized.class)
public class SlowClusterEventbusBridgeTest extends VertxTestBase {

  @Parameterized.Parameters(name = "{index}: transport = {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {Transport.RAW_WS}, {Transport.WS}
    });
  }

  private final Transport transport;

  private Vertx node1;
  private Vertx node2;
  private Router router;
  private HttpServer server;
  private HttpClient client;
  protected SockJSHandler sockJS;

  public SlowClusterEventbusBridgeTest(Transport transport) {
    this.transport = transport;
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    startNodes(2);
    node1 = vertices[0];
    node2 = vertices[1];
    router = Router.router(node1);
    server = node1.createHttpServer();
    CountDownLatch latch = new CountDownLatch(1);
    server.requestHandler(router).listen(0).onComplete(onSuccess(res -> latch.countDown()));
    awaitLatch(latch);
    client = node1.createHttpClient(new HttpClientOptions().setDefaultPort(server.actualPort()));
    sockJS = SockJSHandler.create(node1);
  }


  @Override
  protected ClusterManager getClusterManager() {
    return new SlowClusterManager(vertx);
  }

  @Test
  public void testRegistration() throws Exception {
    String payload = "hello slinkydeveloper!";
    String addr = "someaddress";
    String websocketURI = "/eventbus/websocket";

    CountDownLatch registerLatch = new CountDownLatch(1);
    CountDownLatch registeredLatch = new CountDownLatch(1);
    CountDownLatch requestLatch = new CountDownLatch(1);

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
          assertNotNull(be.socket());
          JsonObject raw = be.getRawMessage();
          assertEquals(addr, raw.getString("address"));
          registerLatch.countDown();
        } else if (be.type() == BridgeEventType.REGISTERED) {
          assertNotNull(be.socket());
          JsonObject raw = be.getRawMessage();
          assertEquals(addr, raw.getString("address"));

          // The bridgeClient should be able to receive this message
          node2.eventBus().send(addr, payload);

          registeredLatch.countDown();
        }
        be.complete(true);
      }));

    BridgeClient bridgeClient = new BridgeClient(client, transport);

    bridgeClient.handler((address, received) -> {
      assertEquals(addr, address);
      assertEquals(payload, received.getString("body"));
      bridgeClient.close().onComplete(v2 -> requestLatch.countDown());
    });

    bridgeClient
      .connect(websocketURI)
      .compose(v -> bridgeClient.register(addr))
      .onComplete(onSuccess(v -> {
      }));

    awaitLatch(registerLatch);
    awaitLatch(registeredLatch);
    awaitLatch(requestLatch);
  }

  private static class SlowClusterManager extends WrappedClusterManager {

    final Vertx vertx;

    SlowClusterManager(Vertx vertx) {
      super(new FakeClusterManager());
      this.vertx = vertx;
    }

    @Override
    public void addRegistration(String address, RegistrationInfo registrationInfo, Promise<Void> promise) {
      vertx.setTimer(1000, l -> {
        super.addRegistration(address, registrationInfo, promise);
      });
    }
  }
}
