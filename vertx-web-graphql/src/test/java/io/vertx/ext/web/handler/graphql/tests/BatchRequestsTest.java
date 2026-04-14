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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

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
  public void testEmptyBatch() throws Exception {
    HttpClientResponse response = client
      .request(HttpMethod.POST, 8080, "localhost", "/graphql")
      .compose(request -> request
        .send(new JsonArray().toBuffer()))
      .await(20, TimeUnit.SECONDS);
    assertEquals(200, response.statusCode());
    Buffer buffer = response.body().await();
    Object json = buffer.toJsonValue();
    assertInstanceOf(JsonArray.class, json);
    JsonArray results = (JsonArray) json;
    assertTrue(results.isEmpty());
  }

  @Test
  public void testSimpleBatch() throws Exception {
    JsonObject query = new JsonObject()
      .put("query", "query { allLinks { url } }");
    HttpClientResponse response = client.request(HttpMethod.POST, 8080, "localhost", "/graphql")
      .compose(request -> request.send(new JsonArray().add(query).toBuffer()))
      .await(20, TimeUnit.SECONDS);
    assertEquals(200, response.statusCode());
    Buffer buffer = response.body().await();
    Object json = buffer.toJsonValue();
    assertInstanceOf(JsonArray.class, json);
    JsonArray results = (JsonArray) json;
    assertEquals(1, results.size());
    testData.checkLinkUrls(testData.urls(), results.getJsonObject(0));
  }

  @Test
  public void testMissingQuery() throws Exception {
    JsonObject query = new JsonObject()
      .put("foo", "bar");
    HttpClientResponse response = client.request(HttpMethod.POST, 8080, "localhost", "/graphql")
      .compose(request -> request.send(new JsonArray().add(query).toBuffer()))
      .await(20, TimeUnit.SECONDS);
    assertEquals(400, response.statusCode());
  }
}
