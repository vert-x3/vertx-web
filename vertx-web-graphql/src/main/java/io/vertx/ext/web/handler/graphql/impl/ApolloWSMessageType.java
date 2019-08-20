/*
 * Copyright 2019 Red Hat, Inc.
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

package io.vertx.ext.web.handler.graphql.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rogelio Orts
 */
public enum ApolloWSMessageType {
  CONNECTION_INIT("connection_init"),
  CONNECTION_TERMINATE("connection_terminate"),
  START("start"),
  STOP("stop"),
  CONNECTION_ACK("connection_ack"),
  CONNECTION_ERROR("error"),
  CONNECTION_KEEP_ALIVE("ka"),
  DATA("data"),
  ERROR("error"),
  COMPLETE("complete");

  private String text;

  ApolloWSMessageType(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  private static Map<String, ApolloWSMessageType> lookup = new HashMap<>(values().length);

  static {
    for (ApolloWSMessageType apolloWSMessageType : values()) {
      lookup.put(apolloWSMessageType.text, apolloWSMessageType);
    }
  }

  public static ApolloWSMessageType from(String type) {
    return lookup.get(type);
  }
}
