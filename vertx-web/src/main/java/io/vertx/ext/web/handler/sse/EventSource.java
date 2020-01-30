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
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.handler.sse.impl.EventSourceImpl;

public interface EventSource {

  static EventSource create(Vertx vertx, HttpClientOptions options) {
    return new EventSourceImpl(vertx, options);
  }

  @Fluent
  EventSource connect(String path, Handler<AsyncResult<Void>> handler);

  @Fluent
  default EventSource close() {
    return null;
  }

  @Fluent
  EventSource connect(String path, String lastEventId, Handler<AsyncResult<Void>> handler);

  @Fluent
  EventSource onMessage(Handler<String> messageHandler);

  @Fluent
  EventSource onEvent(String eventName, Handler<String> handler);

  @Fluent
  default EventSource onClose(Handler<Void> handler) {
    return null;
  }

  String lastId();
}
