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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Options for configuring the {@link GraphQLWSHandler}.
 */
@DataObject(generateConverter = true)
public class GraphQLWSOptions {

  /**
   * Default maximum delay in milliseconds for the client to send the {@code CONNECTION_INIT} message = 3000.
   */
  public static final long DEFAULT_CONNECTION_INIT_WAIT_TIMEOUT = 3000L;

  private long connectionInitWaitTimeout = DEFAULT_CONNECTION_INIT_WAIT_TIMEOUT;

  /**
   * Default constructor.
   */
  public GraphQLWSOptions() {
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public GraphQLWSOptions(GraphQLWSOptions other) {
    connectionInitWaitTimeout = other.connectionInitWaitTimeout;
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public GraphQLWSOptions(JsonObject json) {
    this();
    GraphQLWSOptionsConverter.fromJson(json, this);
  }

  /**
   * @return a JSON representation of these options
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    GraphQLWSOptionsConverter.toJson(this, json);
    return json;
  }

  /**
   * @return maximum delay in milliseconds for the client to send the {@code CONNECTION_INIT} message
   */
  public long getConnectionInitWaitTimeout() {
    return connectionInitWaitTimeout;
  }

  /**
   * Set the maximum delay in milliseconds for the client to send the {@code CONNECTION_INIT} message.
   * Defaults to {@code 3000}.
   *
   * @param connectionInitWaitTimeout delay in milliseconds
   * @return a reference to this, so the API can be used fluently
   */
  public GraphQLWSOptions setConnectionInitWaitTimeout(long connectionInitWaitTimeout) {
    this.connectionInitWaitTimeout = connectionInitWaitTimeout;
    return this;
  }
}
