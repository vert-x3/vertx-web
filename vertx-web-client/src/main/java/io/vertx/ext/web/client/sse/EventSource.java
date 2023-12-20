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

package io.vertx.ext.web.client.sse;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.impl.sse.EventSourceImpl;

/**
 * A Vert.x implementation of a Server-Sent Events EventSource.
 *
 * @see <a href="https://www.w3.org/TR/eventsource/">W3: Server-Sent Events</a>
 * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html">Living Standard: Server-Sent Events</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventSource">MDN: EventSource</a>
 */
@VertxGen
public interface EventSource {

  /**
   * Create an {@link EventSource} with the specified options.
   */
  static EventSource create(final Vertx vertx, final EventSourceOptions options) {
    return new EventSourceImpl(vertx, options);
  }

  /**
   * The URL that will provide the event stream.
   */
  String url();

  /**
   * Returns the state of this object's connection.
   */
  ReadyState readyState();

  /**
   * The id of the last event received from the server.
   */
  @Nullable
  String lastId();

  @Fluent
  EventSource connect(final Handler<AsyncResult<Void>> handler);

  Future<Void> connect();

  @Fluent
  EventSource connect(final String lastId, final Handler<AsyncResult<Void>> handler);

  Future<Void> connect(final String lastId);

  @Fluent
  EventSource connectHandler(final Handler<Void> handler);


  /**
   * Set a handler for 'message' events.
   * <p>
   * This is equivalent to the specification's 'onmessage' event handler.
   */
  @Fluent
  EventSource messageHandler(final Handler<String> handler);

  /**
   * Set a handler for a given event.
   * <p>
   * This is equivalent to the specification's 'onmessage' event handler.
   *
   * @param eventName the ad-hoc event name.
   */
  @Fluent
  EventSource eventHandler(final String eventName, final Handler<String> handler);

  /**
   * Set a handler for 'error' events.
   * <p>
   * This is equivalent to the specification's 'onerror' event handler.
   */
  @Fluent
  EventSource exceptionHandler(final Handler<String> handler);


  /**
   * Aborts any connections and sets {@link #readyState()} to {@link ReadyState#CLOSED}.
   */
  void close();

}
