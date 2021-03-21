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

import io.vertx.codegen.annotations.VertxGen;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration describing Event-Stream message types
 * This enumeration is public since it can be used as EventBus message headers,
 * in case the user needs to forward messages from the event-bus with metadata (like "event:", or "id:")
 */
@VertxGen
public enum SSEHeaders {
  EVENT("event"),
  ID("id"),
  RETRY("retry"),
  LAST_EVENT_ID("Last-Event-ID");

  private final String name;

  SSEHeaders(String name) {
    this.name = name;
  }

  public static Optional<SSEHeaders> fromString(String name) {
    return Arrays.stream(values()).filter(sseHeader -> sseHeader.name.equalsIgnoreCase(name)).findFirst();
  }

  @Override
  public String toString() {
    return name;
  }
}
