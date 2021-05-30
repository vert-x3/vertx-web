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
import io.vertx.core.http.RequestOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EventSourceImpl implements EventSource {
  private static final Logger log = LoggerFactory.getLogger(EventSourceImpl.class);

  private final Vertx vertx;
  private final EventSourceOptions options;
  private final Map<String, Handler<String>> eventHandlers;
  private HttpClient client;
  private boolean connected;
  private String lastId;
  private Handler<String> messageHandler;
  private SSEPacket currentPacket;
  private Long retryTimerId;

  public EventSourceImpl(final Vertx vertx, final EventSourceOptions options) {
    options.setKeepAlive(true);
    this.vertx = vertx;
    this.options = options;
    eventHandlers = new HashMap<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized EventSource connectHandler(final String path, final Handler<AsyncResult<Void>> handler) {
    return connectHandler(path, null, handler);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized EventSource connectHandler(final String path, final String lastEventId, final Handler<AsyncResult<Void>> handler) {
    if (connected) {
      throw new VertxException("SSEConnection already connected");
    }
    if (client == null) {
      client = vertx.createHttpClient(options);
    }

    client.request(createConnectOptions(path, lastEventId))
      .compose(HttpClientRequest::send)
      .onSuccess(response -> {
        if (shouldReconnect(response)) {
          client.close();
          client = null;
          getEventErrorHandler().ifPresent(errorHandler -> errorHandler.handle("")); // FIXME: error type/name
          vertx.setTimer(options.getRetryPeriod(), timerId -> {
            retryTimerId = timerId;
            connectHandler(path, lastEventId, handler);
          });
          return;
        }

        int status = response.statusCode();
        if (status != 200) {
          // redirects have been handled in `client.redirectHandler(...)` other status codes are considered errors, 204 & 205 are handled in `shouldReconnect`
          handler.handle(Future.failedFuture(new VertxException("Could not connect EventSource, the server answered with status " + status)));
        } else {
          // Connect succeeded.
          connected = true;
          response.handler(this::handleMessage)
            // reconnect automatically
            .endHandler(r -> {
              connected = false;
              connectHandler(path, this.lastId, handler);
            });
          handler.handle(Future.succeededFuture());
        }
      })
      .onFailure(cause -> handler.handle(Future.failedFuture(cause)));

    client.redirectHandler(resp -> {
      String redirect = resp.headers().get(HttpHeaders.LOCATION);
      return Future.succeededFuture(createConnectOptions(redirect, lastEventId));
    });

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void close() {
    if (retryTimerId != null) {
      vertx.cancelTimer(retryTimerId);
      retryTimerId = null;
    }
    if (client != null) {
      try {
        client.close();
      } catch (Exception e) {
        log.error("An error occurred closing the EventSource: ", e);
      }
    }
    client = null;
    connected = false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized EventSource messageHandler(Handler<String> handler) {
    this.messageHandler = handler;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized EventSource eventHandler(String eventName, Handler<String> handler) {
    eventHandlers.put(eventName, handler);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized String lastId() {
    return lastId;
  }

  private synchronized Optional<Handler<String>> getEventErrorHandler() {
    return Optional.ofNullable(eventHandlers.get("error"));
  }

  private synchronized boolean shouldReconnect(final HttpClientResponse response) {
    int status = response.statusCode();
    return status == 204
      || status == 205
      || (status == 200 && !"text/event-stream".equalsIgnoreCase(response.headers().get(HttpHeaders.CONTENT_TYPE)));
  }

  private synchronized void handleMessage(final Buffer buffer) {
    if (!connected) {
      return;
    }
    if (currentPacket == null) {
      currentPacket = new SSEPacket();
    }
    boolean terminated = currentPacket.append(buffer);
    Handler<String> eventHandler = null;
    if (terminated) {
      for (SSEHeaders header : currentPacket.headers().keySet()) {
        String value = currentPacket.headers().get(header);
        switch (header) {
          case ID:
          case LAST_EVENT_ID:
            lastId = value;
            break;
          case EVENT:
            eventHandler = eventHandlers.get(value);
            break;
        }
      }
      if (eventHandler != null) {
        eventHandler.handle(currentPacket.toString());
      } else {
        messageHandler.handle(currentPacket.toString());
      }
      currentPacket = null;
    }
  }

  private RequestOptions createConnectOptions(final String path, final String lastEventId) {
    RequestOptions options = new RequestOptions()
      .setMethod(HttpMethod.GET)
      .setURI(path)
      .setFollowRedirects(true)
      .addHeader(HttpHeaders.ACCEPT, "text/event-stream");

    if (lastEventId != null) {
      options.putHeader(SSEHeaders.LAST_EVENT_ID.toString(), lastEventId);
    }
    return options;
  }

}
