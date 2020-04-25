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

package io.vertx.ext.web.handler.sse.impl;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sse.SSEConnection;
import io.vertx.ext.web.handler.sse.SSEHandler;

import java.util.ArrayList;
import java.util.List;

public class SSEHandlerImpl implements SSEHandler {

  private final List<Handler<SSEConnection>> connectHandlers;

  public SSEHandlerImpl() {
    connectHandlers = new ArrayList<>();
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();
    response.setChunked(true);
    if (request.headers().contains(HttpHeaders.ACCEPT.toString(), "text/event-stream", true)) {
      response.setStatusCode(406).end();
      return;
    }
    SSEConnection connection = SSEConnection.create(context);
    response.headers().add("Content-Type", "text/event-stream");
    response.headers().add("Cache-Control", "no-cache");
    response.headers().add("Connection", "keep-alive");
    connectHandlers.forEach(handler -> handler.handle(connection));
  }

  @Override
  public SSEHandler connectHandler(Handler<SSEConnection> handler) {
    connectHandlers.add(handler);
    return this;
  }

}
