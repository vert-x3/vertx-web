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

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Thomas Segismont
 */
public class GraphQLInputDeserializerTest {

  @Test
  public void testSingle() {
    JsonObject query = createQuery();
    GraphQLInput graphQLInput = Json.decodeValue(query.toBuffer(), GraphQLInput.class);
    assertThat(graphQLInput, is(instanceOf(GraphQLQuery.class)));
    GraphQLQuery graphQLQuery = (GraphQLQuery) graphQLInput;
    verify(graphQLQuery);
  }

  private JsonObject createQuery() {
    return new JsonObject()
      .put("query", "foo")
      .put("operationName", "op")
      .put("variables", new JsonObject().put("bar", "baz"));
  }

  private void verify(GraphQLQuery graphQLQuery) {
    assertEquals("foo", graphQLQuery.getQuery());
    assertEquals("op", graphQLQuery.getOperationName());
    assertEquals(Collections.<String, Object>singletonMap("bar", "baz"), graphQLQuery.getVariables());
  }

  @Test
  public void testBatch() {
    JsonArray batch = new JsonArray().add(createQuery());
    GraphQLInput graphQLInput = Json.decodeValue(batch.toBuffer(), GraphQLInput.class);
    assertThat(graphQLInput, is(instanceOf(GraphQLBatch.class)));
    GraphQLBatch graphQLBatch = (GraphQLBatch) graphQLInput;
    assertEquals(1, graphQLBatch.size());
    verify(graphQLBatch.get(0));
  }
}
