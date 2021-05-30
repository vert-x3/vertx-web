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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * A Vert.x implementation of a Server-Sent Events EventSource.
 *
 * @see <a href="https://www.w3.org/TR/eventsource/">W3: Server-Sent Events</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventSource">MDN: EventSource</a>
 */
@VertxGen
public interface EventSource {

  /**
   * Create an {@link EventSource} with the default options.
   */
  static EventSource create(final Vertx vertx) {
    return new EventSourceImpl(vertx, new EventSourceOptions());
  }

  /**
   * Create an {@link EventSource} with the specified options.
   */
  static EventSource create(final Vertx vertx, final EventSourceOptions options) {
    return new EventSourceImpl(vertx, options);
  }

  /**
   * The id of the last event received from the server.
   */
  String lastId();

  /**
   * Handle
   * <p>
   * This is equivalent to the
   */
  @Fluent
  EventSource connectHandler(final String path, final Handler<AsyncResult<Void>> handler);

  @Fluent
  EventSource connectHandler(final String path, final String lastEventId, final Handler<AsyncResult<Void>> handler);

  default void close() {
  }

  /**
   * Set a handler for 'message' events.
   * <p>
   * This is equivalent to the specification's 'onmessage' event handler.
   *
   * @param handler the handler called when the 'error' event is received.
   */
  @Fluent
  EventSource messageHandler(final Handler<String> handler);

  /**
   * Set a handler for a given event.
   * <p>
   * This is equivalent to the specification's 'onmessage' event handler.
   *
   * @param eventName the ad-hoc event name.
   * @param handler   the handler called when the {@param eventName} event is received.
   */
  @Fluent
  EventSource eventHandler(final String eventName, final Handler<String> handler);

  /**
   * Set a handler for 'error' events.
   * <p>
   * This is equivalent to the specification's 'onerror' event handler.
   *
   * @param handler the handler called when the 'error' event is received.
   */
  @Fluent
  default EventSource exceptionHandler(final Handler<String> handler) {
    return eventHandler("error", handler);
  }

}
