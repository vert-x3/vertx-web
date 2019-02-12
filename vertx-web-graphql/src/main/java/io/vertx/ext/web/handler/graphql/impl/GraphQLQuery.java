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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;

/**
 * @author Thomas Segismont
 */
@JsonDeserialize(as = GraphQLQuery.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLQuery implements GraphQLInput {

  private String query;
  private Map<String, Object> variables;

  public GraphQLQuery() {
  }

  public String getQuery() {
    return query;
  }

  public GraphQLQuery setQuery(String query) {
    this.query = query;
    return this;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public GraphQLQuery setVariables(Map<String, Object> variables) {
    this.variables = variables;
    return this;
  }

  @Override
  public String toString() {
    return "GraphQLQuery{" +
      "query='" + query + '\'' +
      ", variables=" + variables +
      '}';
  }
}
