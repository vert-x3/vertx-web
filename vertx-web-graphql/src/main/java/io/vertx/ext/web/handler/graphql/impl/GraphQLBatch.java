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
import io.vertx.core.json.JsonObject;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * @author Thomas Segismont
 */
public class GraphQLBatch implements GraphQLInput, Iterable<GraphQLQuery> {

  private final JsonArray value;

  public GraphQLBatch(JsonArray value) {
    this.value = value;
  }

  @Override
  public Iterator<GraphQLQuery> iterator() {
    return new GraphQLQueryIterator(value.iterator());
  }

  @Override
  public Spliterator<GraphQLQuery> spliterator() {
    return Spliterators.spliterator(iterator(), value.size(), 0);
  }

  private static class GraphQLQueryIterator implements Iterator<GraphQLQuery> {

    private final Iterator<Object> jsonArrayIterator;

    public GraphQLQueryIterator(Iterator<Object> jsonArrayIterator) {
      this.jsonArrayIterator = jsonArrayIterator;
    }

    @Override
    public boolean hasNext() {
      return jsonArrayIterator.hasNext();
    }

    @Override
    public GraphQLQuery next() {
      return new GraphQLQuery((JsonObject) jsonArrayIterator.next());
    }
  }
}
