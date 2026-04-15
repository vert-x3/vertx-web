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

package io.vertx.ext.web.handler.graphql.tests;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Segismont
 */
public class BatchRequestsTest extends GraphQLTestBase {

  @Override
  protected GraphQLHandlerOptions createOptions() {
    return super.createOptions().setRequestBatchingEnabled(true);
  }

  @Test
  public void testEmptyBatch() {
    var response = webClient.post("/graphql")
      .sendBuffer(new JsonArray().toBuffer())
      .await();
    assertEquals(200, response.statusCode());
    JsonArray results = response.bodyAsJsonArray();
    assertTrue(results.isEmpty());
  }

  @Test
  public void testSimpleBatch() {
    JsonObject query = new JsonObject()
      .put("query", "query { allLinks { url } }");
    var response = webClient.post("/graphql")
      .sendBuffer(new JsonArray().add(query).toBuffer())
      .await();
    assertEquals(200, response.statusCode());
    JsonArray results = response.bodyAsJsonArray();
    assertEquals(1, results.size());
    testData.checkLinkUrls(testData.urls(), results.getJsonObject(0));
  }

  @Test
  public void testMissingQuery() {
    JsonObject query = new JsonObject()
      .put("foo", "bar");
    var response = webClient.post("/graphql")
      .sendBuffer(new JsonArray().add(query).toBuffer())
      .await();
    assertEquals(400, response.statusCode());
  }
}
