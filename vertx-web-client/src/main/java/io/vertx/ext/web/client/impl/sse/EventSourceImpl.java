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

package io.vertx.ext.web.client.impl.sse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
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
import io.vertx.ext.web.client.sse.EventSource;
import io.vertx.ext.web.client.sse.EventSourceOptions;
import io.vertx.ext.web.client.sse.ReadyState;
import io.vertx.ext.web.sse.SSEHeaders;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EventSourceImpl implements EventSource {
  private static final Logger log = LoggerFactory.getLogger(EventSourceImpl.class);
  private static final String ERROR_EVENT = "error";

  private final Vertx vertx;
  private final String url;
  private final EventSourceOptions options;
  private final Map<String, Handler<String>> eventHandlers;
  private HttpClient client;
  private String lastId;
  private Handler<Void> connectHandler;
  private Handler<String> messageHandler;
  private SSEPacket currentPacket;
  private Long retryTimerId;
  private ReadyState readyState = ReadyState.CONNECTING;

  public EventSourceImpl(final Vertx vertx, final EventSourceOptions options) {
    options.setKeepAlive(true);
    this.vertx = vertx;
    /*
     * Parse the URL to ensure it's valid, per section 9.2.2:
     * "If urlRecord is failure, then throw a 'SyntaxError' DOMException."
     */
    try {
      new URL(options.getUrl());
      this.url = options.getUrl();
    } catch (MalformedURLException ex) {
      throw new VertxException("Invalid URL: " + options.getUrl());
    }
    this.options = options;
    eventHandlers = new HashMap<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String url() {
    return this.url;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized ReadyState readyState() {
    return this.readyState;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized String lastId() {
    return lastId;
  }

  @Override
  public synchronized EventSource connect(final Handler<AsyncResult<Void>> handler) {
    connect(null, handler);
    return this;
  }

  @Override
  public Future<Void> connect() {
    Promise<Void> promise = Promise.promise();
    connect(null, promise);
    return promise.future();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized EventSource connect(final String lastEventId, final Handler<AsyncResult<Void>> handler) {
    // If the EventSource object's readyState attribute is not set to CONNECTING, return.
    if (readyState != ReadyState.CONNECTING) {
      handler.handle(Future.failedFuture(
        String.format("EventSource's readyState is %s, not %s", readyState, ReadyState.CONNECTING))
      );
    }
    if (client == null) {
      client = vertx.createHttpClient(options);
    }

    /*
     * Handle redirections when attempting to connect:
     * "Event stream requests can be redirected using HTTP 301 and 307 redirects as with normal HTTP requests."
     */
    client.redirectHandler(resp -> {
      String redirect = resp.headers().get(HttpHeaders.LOCATION);
      return Future.succeededFuture(createRequestOptions(redirect, lastEventId));
    });

    // Submit the connection request.
    client.request(createRequestOptions(url, lastEventId))
      .compose(HttpClientRequest::send)
      .onSuccess(response -> {
        /*
         * "Clients will reconnect if the connection is closed;
         * a client can be told to stop reconnecting using the HTTP 204 No Content response code."
         */
        int status = response.statusCode();
        if (status == 204) {
          close();
          // TODO: Determine if being told to stop reconnecting counts as success or failure.
          handler.handle(Future.succeededFuture());
          return;
        }

        /*
         * TODO: Determine which status codes warrant reconnection; per the quote above,
         * clients will reconnect "if the connection is closed".
         */
        if (shouldReconnect(response)) {
          close();
          getEventErrorHandler().ifPresent(errorHandler -> errorHandler.handle("")); // FIXME: error type/name
          vertx.setTimer(options.getReconnectInterval(), timerId -> {
            retryTimerId = timerId;
            connect(lastEventId, handler);
          });
          return;
        }

        if (status == 200) {
          /*
           * "When a user agent is to announce the connection, the user agent...
           * sets the readyState attribute to OPEN and fires an event named open at the EventSource object."
           */
          readyState = ReadyState.OPEN;
          if (connectHandler != null) {
            connectHandler.handle(null);
          }
          response.handler(this::handleMessage)
            // reconnect automatically
            .endHandler(r -> {
              readyState = ReadyState.CLOSED;
              connect(this.lastId, handler);
            });
          handler.handle(Future.succeededFuture());
        } else {
          // redirects have been handled in `client.redirectHandler(...)` other status codes are considered errors, 204 & 205 are handled in `shouldReconnect`
          handler.handle(Future.failedFuture(
            new VertxException("Could not connect EventSource, the server answered with status " + status)));
        }
      })
      .onFailure(cause -> handler.handle(Future.failedFuture(cause)));


    return this;
  }

  @Override
  public Future<Void> connect(final String lastEventId) {
    Promise<Void> promise = Promise.promise();
    connect(lastEventId, promise);
    return promise.future();
  }

  @Override
  public EventSource connectHandler(Handler<Void> handler) {
    connectHandler = handler;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized EventSource messageHandler(Handler<String> handler) {
    this.messageHandler = handler;
    return this;
  }

  @Override
  public EventSource exceptionHandler(Handler<String> handler) {
    eventHandlers.put(ERROR_EVENT, handler);
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
  public void close() {
    if (readyState != ReadyState.OPEN) {
      throw new IllegalStateException("EventSource is not connected");
    }

    HttpClient client;
    synchronized (this) {
      if (retryTimerId != null) {
        vertx.cancelTimer(retryTimerId);
        retryTimerId = null;
      }

      client = this.client;
      this.client = null;
      readyState = ReadyState.CLOSED;
    }

    if (client != null) {
      try {
        client.close();
      } catch (Exception e) {
        log.error("An error occurred closing the EventSource: ", e);
      }
    }
  }


  private synchronized Optional<Handler<String>> getEventErrorHandler() {
    return Optional.ofNullable(eventHandlers.get("error"));
  }

  private synchronized boolean shouldReconnect(final HttpClientResponse response) {
    int status = response.statusCode();
    return status != 204
      && (status == 205
      || (status == 200 && !"text/event-stream".equalsIgnoreCase(response.headers().get(HttpHeaders.CONTENT_TYPE))));
  }

  private synchronized void handleMessage(final Buffer buffer) {
    if (readyState != ReadyState.OPEN) {
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

  private RequestOptions createRequestOptions(final String path, final String lastEventId) {
    RequestOptions options = new RequestOptions()
      .setMethod(HttpMethod.GET)
      .setURI(path)
      .setFollowRedirects(true)
      .addHeader(HttpHeaders.ACCEPT, "text/event-stream");

    // If the EventSource object's last event ID string is not the empty string, set `Last-Event-ID`
    if (lastEventId != null) {
      options.putHeader(SSEHeaders.LAST_EVENT_ID.toString(), lastEventId);
    }
    return options;
  }

}
