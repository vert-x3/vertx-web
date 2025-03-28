/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.tests.handler.sockjs;

import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Ben Ripkens
 */
abstract class SockJSTestBase extends VertxTestBase {

  int numServers = 1;
  HttpClient client;
  WebSocketClient wsClient;
  Consumer<Router> preSockJSHandlerSetup;
  Supplier<Handler<SockJSSocket>> socketHandler;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080).setKeepAlive(false));
    wsClient = vertx.createWebSocketClient(new WebSocketClientOptions().setDefaultPort(8080));
  }

  void startServers() throws Exception {
    startServers(new SockJSHandlerOptions());
  }

  void startServers(SockJSHandlerOptions options) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    vertx.deployVerticle(() -> new VerticleBase() {
      @Override
      public Future<?> start() throws Exception {

        Router router = Router.router(vertx);
        router.route()
          .handler(SessionHandler.create(LocalSessionStore.create(vertx))
            .setNagHttps(false)
            .setSessionTimeout(60 * 60 * 1000));

        if (preSockJSHandlerSetup != null) {
          preSockJSHandlerSetup.accept(router);
        }

        options.setHeartbeatInterval(2000);
        options.setRegisterWriteHandler(true);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
        router.route("/test/*").subRouter(sockJSHandler.socketHandler(socketHandler.get()));

        return vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"))
          .requestHandler(router)
          .listen();
      }
    }, new DeploymentOptions().setInstances(numServers)).onComplete(onSuccess(id -> latch.countDown()));
    awaitLatch(latch);
  }
}
