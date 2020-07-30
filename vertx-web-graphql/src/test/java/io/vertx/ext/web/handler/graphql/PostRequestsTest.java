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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.List;

import static io.vertx.ext.web.handler.graphql.GraphQLRequest.GRAPHQL;
import static io.vertx.ext.web.handler.graphql.GraphQLRequest.encode;
import static java.util.stream.Collectors.toList;

/**
 * @author Thomas Segismont
 */
public class PostRequestsTest extends GraphQLTestBase {

  @Test
  public void testSimplePost() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }");
    request.send(client, onSuccess(body -> {
      if (testData.checkLinkUrls(testData.urls(), body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testSimplePostNoContentType() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setContentType(null);
    request.send(client, onSuccess(body -> {
      if (testData.checkLinkUrls(testData.urls(), body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testSimplePostQueryInParam() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setGraphQLQueryAsParam(true);
    request.send(client, onSuccess(body -> {
      if (testData.checkLinkUrls(testData.urls(), body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testSimplePostQueryAsBody() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setContentType(GRAPHQL);
    request.send(client, onSuccess(body -> {
      if (testData.checkLinkUrls(testData.urls(), body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testMultipleQueriesWithOperationName() throws Exception {
    String query = "query foo { allLinks { url } }"
      + " "
      + "query bar($secure: Boolean) { allLinks(secureOnly: $secure) { url } }";
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery(query)
      .setOperationName("bar")
      .addVariable("secure", "true");
    request.send(client, onSuccess(body -> {
      List<String> expected = testData.urls().stream()
        .filter(url -> url.startsWith("https://"))
        .collect(toList());
      if (testData.checkLinkUrls(expected, body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testSimplePostWithVariable() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query($secure: Boolean) { allLinks(secureOnly: $secure) { url } }")
      .addVariable("secure", "true");
    request.send(client, onSuccess(body -> {
      List<String> expected = testData.urls().stream()
        .filter(url -> url.startsWith("https://"))
        .collect(toList());
      if (testData.checkLinkUrls(expected, body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testPostNoQuery() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setRequestBody(new JsonObject().put("foo", "bar").toBuffer());
    request.send(client, 400, onSuccess(v -> {
      testComplete();
    }));
    await();
  }

  @Test
  public void testPostInvalidJson() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setRequestBody(new JsonArray().add("foo").add("bar").toBuffer());
    request.send(client, 400, onSuccess(v -> {
      testComplete();
    }));
    await();
  }

  @Test
  public void testPostWithInvalidVariableParam() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setHttpQueryString("query=" + encode("query { allLinks { url } }") + "&variables=" + encode("[1,2,3]"))
      .setRequestBody(null);
    request.send(client, 400, onSuccess(body -> {
      testComplete();
    }));
    await();
  }

  @Test
  public void testUnsupportedMediaType() throws Exception {
    client.post(
      "/graphql",
      HttpHeaders.set(HttpHeaders.CONTENT_TYPE, "text/html"),
      Buffer.buffer("<h1>Hello world!</h1>"),
      onSuccess(response -> {
        assertEquals(415, response.statusCode());
        testComplete();
      }));
    await();
  }

  @Test
  public void testContentTypeWithCharset() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setContentType("application/json; charset=UTF-8");
    request.send(client, onSuccess(body -> {
      if (testData.checkLinkUrls(testData.urls(), body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }
}
