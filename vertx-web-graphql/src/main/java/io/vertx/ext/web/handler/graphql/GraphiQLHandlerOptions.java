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
import io.vertx.ext.web.common.WebEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * Embedded GraphiQL user interface options.
 *
 * @author Thomas Segismont
 */
@DataObject(generateConverter = true)
public class GraphiQLHandlerOptions {

  /**
   * Whether GraphiQL development tool should be enabled by default = false.
   */
  public static final boolean DEFAULT_ENABLED = WebEnvironment.development();

  private boolean enabled = DEFAULT_ENABLED;

  private String graphQLUri;

  private Map<String, String> headers;

  private String query;

  private JsonObject variables;

  /**
   * Default constructor.
   */
  public GraphiQLHandlerOptions() {
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public GraphiQLHandlerOptions(GraphiQLHandlerOptions other) {
    enabled = other.enabled;
    graphQLUri = other.graphQLUri;
    headers = other.headers == null ? null : new HashMap<>(other.headers);
    query = other.query;
    variables = other.variables == null ? null : other.variables.copy();
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public GraphiQLHandlerOptions(JsonObject json) {
    this();
    GraphiQLHandlerOptionsConverter.fromJson(json, this);
  }

  /**
   * @return a JSON representation of these options
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    GraphiQLHandlerOptionsConverter.toJson(this, json);
    return json;
  }

  /**
   * @return true if the GraphiQL development tool should be enabled, false otherwise
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Whether the GraphiQL development tool should be enabled. Defaults to {@code false}.
   *
   * @param enabled true to enable the GraphiQL development tool, false otherwise
   *
   * @return a reference to this, so the API can be used fluently
   */
  public GraphiQLHandlerOptions setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  /**
   * @return the GraphQL endpoint URI
   */
  public String getGraphQLUri() {
    return graphQLUri;
  }

  /**
   * Set the GraphQL endpoint URI. Defaults to the path used to get the GraphiQL user interface.
   *
   * @param graphQLUri the GraphQL endpoint URI
   *
   * @return a reference to this, so the API can be used fluently
   */
  public GraphiQLHandlerOptions setGraphQLUri(String graphQLUri) {
    this.graphQLUri = graphQLUri;
    return this;
  }

  /**
   * @return the fixed set of HTTP headers to add to GraphiQL requests
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * A fixed set of HTTP headers to add to GraphiQL requests. Defaults to {@code null}.
   *
   * @param headers the set of HTTP headers to add to GraphiQL requests
   *
   * @return a reference to this, so the API can be used fluently
   */
  public GraphiQLHandlerOptions setHeaders(Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  /**
   * @return the query to set as initial value in the GraphiQL user interface
   */
  public String getQuery() {
    return query;
  }

  /**
   * Initial value of the query area in the GraphiQL user interface. Defaults to {@code null}.
   *
   * @param query the query to set as initial value
   *
   * @return a reference to this, so the API can be used fluently
   */
  public GraphiQLHandlerOptions setQuery(String query) {
    this.query = query;
    return this;
  }

  /**
   * @return the variables to set as initial value in the GraphiQL user interface
   */
  public JsonObject getVariables() {
    return variables;
  }

  /**
   * Initial value of the variables area in the GraphiQL user interface. Defaults to {@code null}.
   *
   * @param variables the variables to set as initial value
   *
   * @return a reference to this, so the API can be used fluently
   */
  public GraphiQLHandlerOptions setVariables(JsonObject variables) {
    this.variables = variables;
    return this;
  }
}
