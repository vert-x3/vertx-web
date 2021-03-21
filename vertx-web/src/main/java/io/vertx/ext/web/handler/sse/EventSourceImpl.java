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

package io.vertx.ext.web.handler.sse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EventSourceImpl implements EventSource {

  private HttpClient client;
  private boolean connected;
  private String lastId;
  private Handler<String> messageHandler;
  private final Map<String, Handler<String>> eventHandlers;
  private SSEPacket currentPacket;
  private final Vertx vertx;
  private final EventSourceOptions options;
  private Long retryTimerId;

  public EventSourceImpl(Vertx vertx, EventSourceOptions options) {
    options.setKeepAlive(true);
    this.vertx = vertx;
    this.options = options;
    eventHandlers = new HashMap<>();
  }

  @Override
  public synchronized EventSource connect(String path, Handler<AsyncResult<Void>> handler) {
    return connect(path, null, handler);
  }

  @Override
  public synchronized EventSource connect(String path, String lastEventId, Handler<AsyncResult<Void>> handler) {
    if (connected) {
      throw new VertxException("SSEConnection already connected");
    }
    if (client == null) {
      client = vertx.createHttpClient(options);
    }
    client.redirectHandler(resp -> {
      String redirect = resp.headers().get(HttpHeaders.LOCATION);
      return Future.succeededFuture(createRequest(redirect, lastEventId, handler));
    });
    createRequest(path, lastEventId, handler).end();
    return this;
  }

  @Override
  public synchronized void close() {
    if (retryTimerId != null) {
      vertx.cancelTimer(retryTimerId);
      retryTimerId = null;
    }
    if (client != null) {
      try {
        client.close();
      } catch(Exception e ) {
        e.printStackTrace();
      }
    }
    client = null;
    connected = false;
  }

  @Override
  public synchronized EventSource onMessage(Handler<String> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  @Override
  public synchronized EventSource onEvent(String eventName, Handler<String> handler) {
    eventHandlers.put(eventName, handler);
    return this;
  }

  @Override
  public synchronized String lastId() {
    return lastId;
  }

  private synchronized Optional<Handler<String>> getEventErrorHandler() {
    return Optional.ofNullable(eventHandlers.get("error"));
  }

  private synchronized boolean shouldReconnect(HttpClientResponse response) {
    int status = response.statusCode();
    return status == 204
      || status == 205
      || (status == 200 && !"text/event-stream".equalsIgnoreCase(response.headers().get(HttpHeaders.CONTENT_TYPE)));
  }

  private synchronized void handleMessage(Buffer buffer) {
    if (!connected) {
      return;
    }
    if (currentPacket == null) {
      currentPacket = new SSEPacket();
    }
    boolean terminated = currentPacket.append(buffer);
    Optional<Handler<String>> eventHandler = Optional.empty();
    if (terminated) {
      for (SSEHeaders header : currentPacket.headers().keySet()) {
        String value = currentPacket.headers().get(header);
        switch (header) {
          case ID:
          case LAST_EVENT_ID:
            lastId = value;
            break;
          case EVENT:
            eventHandler = Optional.ofNullable(eventHandlers.get(value));
            break;
        }
      }
      if (eventHandler.isPresent()) {
        eventHandler.get().handle(currentPacket.toString());
      } else {
        messageHandler.handle(currentPacket.toString());
      }
      currentPacket = null;
    }
  }

  private HttpClientRequest createRequest(String path, String lastEventId, Handler<AsyncResult<Void>> handler) {
    HttpClientRequest request = client.request(HttpMethod.GET, path);
    request.setFollowRedirects(true);
    request.onFailure(cause -> handler.handle(Future.failedFuture(cause)));
    request.onSuccess(response -> {
      if (shouldReconnect(response)) {
        client.close();
        client = null;
        getEventErrorHandler().ifPresent(errorHandler -> errorHandler.handle("")); // FIXME: error type/name
        vertx.setTimer(options.getRetryPeriod(), timerId -> {
          retryTimerId = timerId;
          connect(path, lastEventId, handler);
        });
        return;
      }
      int status = response.statusCode();
      if (status != 200) { // redirects have been handled in `client.redirectHandler(...)` other status codes are considered errors, 204 & 205 are handled in `shouldReconnect`
        handler.handle(Future.failedFuture(new VertxException("Could not connect EventSource, the server answered with status " + status)));
      } else {
        connected = true;
        response.handler(this::handleMessage);
        response.endHandler(r -> {
          connected = false;
          connect(path, this.lastId, handler);
        }); // reconnect automatically
        handler.handle(Future.succeededFuture());
      }
    });
    if (lastEventId != null) {
      request.headers().add(SSEHeaders.LAST_EVENT_ID.toString(), lastEventId);
    }
    request.headers().add(HttpHeaders.ACCEPT, "text/event-stream");
    return request;
  }

}
