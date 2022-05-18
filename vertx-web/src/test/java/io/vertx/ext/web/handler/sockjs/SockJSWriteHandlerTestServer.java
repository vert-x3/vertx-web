/*
 * Copyright 2020 Red Hat, Inc.
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

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.test.fakecluster.FakeClusterManager;

public class SockJSWriteHandlerTestServer {

  public static void main(String[] areags) {
    CompositeFuture.join(createClusteredAndDeploy(new HttpServerVerticle()), createClusteredAndDeploy(new EventBusRelayVerticle()))
      .onSuccess(cf-> System.out.println("SockJS writeHandler tests server started"))
      .onFailure(Throwable::printStackTrace);
  }

  private static Future<String> createClusteredAndDeploy(Verticle verticle) {
    VertxOptions options = new VertxOptions()
      .setClusterManager(new FakeClusterManager());
    return Vertx.clusteredVertx(options).flatMap(vertx -> vertx.deployVerticle(verticle));
  }

  private static class HttpServerVerticle extends AbstractVerticle {

    @Override
    public void start() {
      Router router = Router.router(vertx);

      router.get("/transports").handler(rc -> {
        JsonArray transports = new JsonArray();
        for (Transport transport : Transport.values()) {
          if (transport != Transport.HTML_FILE) { // Does not work otb with Puppeteer
            transports.add(transport.name());
          }
        }
        rc.json(transports);
      });

      for (Transport transport : Transport.values()) {
        setupSockJSHandler(router, transport, false, true);
        setupSockJSHandler(router, transport, true, true);
        setupSockJSHandler(router, transport, true, false);
      }

      router.post("/message").handler(BodyHandler.create()).handler(rc -> {
        EventBus eventBus = vertx.eventBus();
        JsonObject body = rc.body().asJsonObject();
        if (rc.queryParams().contains("relay")) {
          eventBus.send("relay", body);
        } else {
          sendToWriteHandler(eventBus, body);
        }
        rc.end();
      });

      router.get().handler(StaticHandler.create());

      router.route().failureHandler(ErrorHandler.create(vertx, true));

      vertx.createHttpServer()
        .requestHandler(router)
        .listen(8080);
    }

    private void setupSockJSHandler(Router router, Transport transport, boolean register, boolean local) {
      SockJSOptions options = new SockJSOptions();
      for (Transport t : Transport.values()) {
        if (t != transport) {
          options.addDisabledTransport(t.name());
        }
      }
      options.setRegisterWriteHandler(register).setLocalWriteHandler(local);
      SockJS sockJSHandler = SockJS.create(vertx, options);
      String mountPoint = "/transport/" + transport.name()
        + "/" + (register ? "registered":"unregistered")
        + "/" + (local ? "local":"clustered");
      router.route(mountPoint + "*").subRouter(sockJSHandler.socketHandler(socket -> {
        String id = socket.writeHandlerID();
        socket.write(id != null ? id:"--null--");
      }));
    }
  }

  private static void sendToWriteHandler(EventBus eventBus, JsonObject body) {
    String address = body.getString("address");
    String content = body.getString("content");
    eventBus.send(address, Buffer.buffer(content));
  }

  private static class EventBusRelayVerticle extends AbstractVerticle {

    @Override
    public void start() {
      vertx.eventBus().<JsonObject>consumer("relay", msg -> {
        sendToWriteHandler(vertx.eventBus(), msg.body());
      });
    }
  }
}
