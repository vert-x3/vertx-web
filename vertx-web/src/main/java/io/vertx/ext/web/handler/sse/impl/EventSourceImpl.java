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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.sse.EventSource;

import java.util.HashMap;
import java.util.Map;

public class EventSourceImpl implements EventSource {

  private HttpClient client;
  private boolean connected;
  private String lastId;
  private Handler<String> messageHandler;
  private final Map<String, Handler<String>> eventHandlers;
  private SSEPacket currentPacket;
  private final Vertx vertx;
  private final HttpClientOptions options;
  private Handler<Void> closeHandler;

  public EventSourceImpl(Vertx vertx, HttpClientOptions options) {
    options.setKeepAlive(true);
    this.vertx = vertx;
    this.options = options;
    eventHandlers = new HashMap<>();
  }

  @Override
  public EventSource connect(String path, Handler<AsyncResult<Void>> handler) {
    return connect(path, null, handler);
  }

  @Override
  public EventSource connect(String path, String lastEventId, Handler<AsyncResult<Void>> handler) {
    if (connected) {
      throw new VertxException("SSEConnection already connected");
    }
    if (client == null) {
      client = vertx.createHttpClient(options);
    }
    HttpClientRequest request = client.request(HttpMethod.GET, path);
    request.onFailure(cause -> handler.handle(Future.failedFuture(cause)));
    request.onSuccess(response -> {
      if (response.statusCode() != 200) {
        handler.handle(Future.failedFuture(new VertxException("Could not connect EventSource, the server answered with status " + response.statusCode())));
      } else {
        connected = true;
        response.handler(this::handleMessage);
        if (closeHandler != null) {
          response.endHandler(closeHandler);
        }
        handler.handle(Future.succeededFuture());
      }
    });
    if (lastEventId != null) {
      request.headers().add(SSEHeaders.LAST_EVENT_ID.toString(), lastEventId);
    }
    request.setChunked(true);
    request.headers().add(HttpHeaders.ACCEPT, "text/event-stream");
    request.end();
    return this;
  }

  @Override
  public void close() {
    client.close();
    client = null;
    connected = false;
  }

  @Override
  public EventSource onMessage(Handler<String> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  @Override
  public EventSource onEvent(String eventName, Handler<String> handler) {
    eventHandlers.put(eventName, handler);
    return this;
  }

  @Override
  public EventSource onClose(Handler<Void> closeHandler) {
    this.closeHandler = closeHandler;
    return this;
  }

  @Override
  public String lastId() {
    return lastId;
  }

  private void handleMessage(Buffer buffer) {
    if (currentPacket == null) {
      currentPacket = new SSEPacket();
    }
    boolean terminated = currentPacket.append(buffer);
    if (terminated) {
      // choose the right handler and call it
      Handler<String> handler = messageHandler;
      String header = currentPacket.headerName;
      if (header == null) {
        messageHandler.handle(currentPacket.toString());
      } else {
        final String headerName = currentPacket.headerName;
        if (headerName.equalsIgnoreCase(SSEHeaders.EVENT.toString())) {
          handler = eventHandlers.get(currentPacket.headerValue);
        }
        if (headerName.equalsIgnoreCase(SSEHeaders.ID.toString())) {
          lastId = currentPacket.headerValue;
        }
        if (handler != null) {
          handler.handle(currentPacket.toString());
        }
      }
      currentPacket = null;
    }
  }
}
