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

import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

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
    HttpClientRequest request = client.request(HttpMethod.POST, 8080, "localhost", "/graphql");
    request.handler(response -> {
      if (response.statusCode() != 200) {
        fail(response.statusCode() + " " + response.statusMessage());
      } else {
        response.bodyHandler(buffer -> {
          Object json = buffer.toJson();
          assertThat(json, is(instanceOf(JsonArray.class)));
          JsonArray results = (JsonArray) json;
          assertTrue(results.isEmpty());
          complete();
        });
      }
    }).exceptionHandler(this::fail).end(new JsonArray().toBuffer());
    await();
  }

  @Test
  public void testSimpleBatch() throws Exception {
    HttpClientRequest request = client.request(HttpMethod.POST, 8080, "localhost", "/graphql");
    JsonObject query = new JsonObject()
      .put("query", "query { allLinks { url } }");
    request.handler(response -> {
      if (response.statusCode() != 200) {
        fail(response.statusCode() + " " + response.statusMessage());
      } else {
        response.bodyHandler(buffer -> {
          Object json = buffer.toJson();
          assertThat(json, is(instanceOf(JsonArray.class)));
          JsonArray results = (JsonArray) json;
          assertEquals(1, results.size());
          testData.checkLinkUrls(testData.urls(), results.getJsonObject(0));
          complete();
        });
      }
    }).exceptionHandler(this::fail).end(new JsonArray().add(query).toBuffer());
    await();
  }

  @Test
  public void testMissingQuery() throws Exception {
    HttpClientRequest request = client.request(HttpMethod.POST, 8080, "localhost", "/graphql");
    JsonObject query = new JsonObject()
      .put("foo", "bar");
    request.handler(response -> {
      if (response.statusCode() == 400) {
        complete();
      } else {
        fail(response.statusCode() + " " + response.statusMessage());
      }
    }).exceptionHandler(this::fail).end(new JsonArray().add(query).toBuffer());
    await();
  }
}
