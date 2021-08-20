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

import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * @author Thomas Segismont
 */
public class GraphQLQuery implements GraphQLInput {

  private String query;
  private String operationName;
  private Map<String, Object> variables;
  private Object initialValue;

  public GraphQLQuery(JsonObject value) {
    query = value.getString("query");
    operationName = value.getString("operationName");
    JsonObject vars = value.getJsonObject("variables");
    this.variables = vars != null ? vars.getMap() : null;
    this.initialValue = value.getValue("initialValue");
  }

  public GraphQLQuery(String query, String operationName, Map<String, Object> variables) {
    this.query = query;
    this.operationName = operationName;
    this.variables = variables;
  }

  public GraphQLQuery(String query, String operationName, Map<String, Object> variables, Object initialValue) {
    this(query, operationName, variables);
    this.initialValue = initialValue;
  }

  public String getQuery() {
    return query;
  }

  public GraphQLQuery setQuery(String query) {
    this.query = query;
    return this;
  }

  public String getOperationName() {
    return operationName;
  }

  public GraphQLQuery setOperationName(String operationName) {
    this.operationName = operationName;
    return this;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public GraphQLQuery setVariables(Map<String, Object> variables) {
    this.variables = variables;
    return this;
  }

  public Object getInitialValue() {
    return initialValue;
  }

  public GraphQLQuery setInitialValue(Object initialValue) {
    this.initialValue = initialValue;
    return this;
  }

  @Override
  public String toString() {
    return "GraphQLQuery{" +
      "query='" + query + '\'' +
      ", operationName='" + operationName + '\'' +
      ", variables=" + variables +
      '}';
  }
}
