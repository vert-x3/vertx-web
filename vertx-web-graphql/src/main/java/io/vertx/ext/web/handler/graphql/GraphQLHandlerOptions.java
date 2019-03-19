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

package io.vertx.ext.web.handler.graphql;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Options for configuring the {@link GraphQLHandler}.
 *
 * @author Thomas Segismont
 */
@DataObject(generateConverter = true)
public class GraphQLHandlerOptions {

  /**
   * Whether request batching should be enabled by default = false.
   */
  public static final boolean DEFAULT_REQUEST_BATCHING_ENABLED = false;

  private boolean requestBatchingEnabled = DEFAULT_REQUEST_BATCHING_ENABLED;

  /**
   * Default constructor.
   */
  public GraphQLHandlerOptions() {
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public GraphQLHandlerOptions(GraphQLHandlerOptions other) {
    requestBatchingEnabled = other.requestBatchingEnabled;
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public GraphQLHandlerOptions(JsonObject json) {
    this();
    GraphQLHandlerOptionsConverter.fromJson(json, this);
  }

  /**
   * @return true if request batching should be enabled, false otherwise
   */
  public boolean isRequestBatchingEnabled() {
    return requestBatchingEnabled;
  }

  /**
   * Whether request batching should be enabled. Defaults to {@code false}.
   *
   * @param requestBatchingEnabled true to enable request batching, false otherwise
   *
   * @return a reference to this, so the API can be used fluently
   */
  public GraphQLHandlerOptions setRequestBatchingEnabled(boolean requestBatchingEnabled) {
    this.requestBatchingEnabled = requestBatchingEnabled;
    return this;
  }
}
