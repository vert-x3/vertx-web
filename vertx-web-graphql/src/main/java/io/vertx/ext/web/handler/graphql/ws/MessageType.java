/*
 * Copyright 2021 Red Hat, Inc.
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

package io.vertx.ext.web.handler.graphql.ws;

import io.vertx.codegen.annotations.VertxGen;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link Message} types.
 */
@VertxGen
public enum MessageType {

  CONNECTION_INIT("connection_init"),
  CONNECTION_ACK("connection_ack"),
  PING("ping"),
  PONG("pong"),
  SUBSCRIBE("subscribe"),
  NEXT("next"),
  ERROR("error"),
  COMPLETE("complete");

  private final String text;

  MessageType(String text) {
    this.text = text;
  }

  /**
   * @return text representation of the {@link Message}
   */
  public String getText() {
    return text;
  }

  private static Map<String, MessageType> lookup = new HashMap<>(values().length);

  static {
    for (MessageType messageType : values()) {
      lookup.put(messageType.text, messageType);
    }
  }

  /**
   * Get a {@link MessageType} from its text representation.
   *
   * @param type the message type text representation
   * @return the corresponding message type or null if none matches
   */
  public static MessageType from(String type) {
    return lookup.get(type);
  }
}
