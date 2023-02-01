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

import io.vertx.core.json.JsonArray;

import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * @author Thomas Segismont
 */
public class GraphQLBatch extends AbstractList<GraphQLQuery> implements GraphQLInput, RandomAccess {

  private final JsonArray value;

  public GraphQLBatch(JsonArray value) {
    this.value = value;
  }

  @Override
  public GraphQLQuery get(int index) {
    return new GraphQLQuery(value.getJsonObject(index));
  }

  @Override
  public int size() {
    return value.size();
  }
}
