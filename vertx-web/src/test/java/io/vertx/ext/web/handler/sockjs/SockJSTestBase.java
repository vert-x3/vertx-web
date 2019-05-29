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

package io.vertx.ext.web.handler.sockjs;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
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
  Consumer<Router> preSockJSHandlerSetup;
  Supplier<Handler<SockJSSocket>> socketHandler;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080).setKeepAlive(false));
  }

  void startServers() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    vertx.deployVerticle(() -> new AbstractVerticle() {
      @Override
      public void start(Promise<Void> startFuture) throws Exception {

        Router router = Router.router(vertx);
        router.route().handler(CookieHandler.create());
        router.route()
          .handler(SessionHandler.create(LocalSessionStore.create(vertx))
            .setNagHttps(false)
            .setSessionTimeout(60 * 60 * 1000));

        if (preSockJSHandlerSetup != null) {
          preSockJSHandlerSetup.accept(router);
        }

        SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
        sockJSHandler.socketHandler(socketHandler.get());
        router.route("/test/*").handler(sockJSHandler);

        vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"))
          .requestHandler(router)
          .listen(ar -> {
            if (ar.succeeded()) {
              startFuture.complete();
            } else {
              startFuture.fail(ar.cause());
            }
          });
      }
    }, new DeploymentOptions().setInstances(numServers), onSuccess(id -> latch.countDown()));
    awaitLatch(latch);
  }
}
