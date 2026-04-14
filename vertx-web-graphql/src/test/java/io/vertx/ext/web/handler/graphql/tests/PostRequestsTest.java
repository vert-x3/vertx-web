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
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.vertx.ext.web.handler.graphql.tests.GraphQLRequest.GRAPHQL;
import static io.vertx.ext.web.handler.graphql.tests.GraphQLRequest.encode;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Segismont
 */
public class PostRequestsTest extends GraphQLTestBase {

  @Test
  public void testSimplePost() {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }");
    JsonObject body = request.send(webClient);
    assertTrue(testData.checkLinkUrls(testData.urls(), body), body.toString());
  }

  @Test
  public void testSimplePostNoContentType() {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setContentType(null);
    JsonObject body = request.send(webClient);
    assertTrue(testData.checkLinkUrls(testData.urls(), body), body.toString());
  }

  @Test
  public void testSimplePostQueryInParam() {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setGraphQLQueryAsParam(true);
    JsonObject body = request.send(webClient);
    assertTrue(testData.checkLinkUrls(testData.urls(), body), body.toString());
  }

  @Test
  public void testSimplePostQueryAsBody() {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setContentType(GRAPHQL);
    JsonObject body = request.send(webClient);
    assertTrue(testData.checkLinkUrls(testData.urls(), body), body.toString());
  }

  @Test
  public void testMultipleQueriesWithOperationName() {
    String query = "query foo { allLinks { url } }"
      + " "
      + "query bar($secure: Boolean) { allLinks(secureOnly: $secure) { url } }";
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery(query)
      .setOperationName("bar")
      .addVariable("secure", true);
    JsonObject body = request.send(webClient);
    List<String> expected = testData.urls().stream()
      .filter(url -> url.startsWith("https://"))
      .collect(toList());
    assertTrue(testData.checkLinkUrls(expected, body), body.toString());
  }

  @Test
  public void testSimplePostWithVariable() {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query($secure: Boolean) { allLinks(secureOnly: $secure) { url } }")
      .addVariable("secure", true);
    JsonObject body = request.send(webClient);
    List<String> expected = testData.urls().stream()
      .filter(url -> url.startsWith("https://"))
      .collect(toList());
    assertTrue(testData.checkLinkUrls(expected, body), body.toString());
  }

  @Test
  public void testSimplePostWithInitialValueInParam() {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { description } }")
      .setInitialValue(12345)
      .setInitialValueAsParam(true);
    JsonObject body = request.send(webClient);
    String[] values = new String[testData.links.size()];
    Arrays.fill(values, "12345");
    List<String> expected = Arrays.asList(values);
    assertTrue(testData.checkLinkDescriptions(expected, body), body.toString());
  }

  @Test
  public void testSimplePostWithInitialValue() {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { description } }")
      .setInitialValue(12345);
    JsonObject body = request.send(webClient);
    String[] values = new String[testData.links.size()];
    Arrays.fill(values, "12345");
    List<String> expected = Arrays.asList(values);
    assertTrue(testData.checkLinkDescriptions(expected, body), body.toString());
  }

  @Test
  public void testSimplePostWithNoInitialValue() {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { description } }");
    JsonObject body = request.send(webClient);
    assertTrue(testData.checkLinkDescriptions(testData.descriptions(), body), body.toString());
  }

  @Test
  public void testPostNoQuery() {
    GraphQLRequest request = new GraphQLRequest()
      .setRequestBody(new JsonObject().put("foo", "bar").toBuffer());
    request.send(webClient, 400);
  }

  @Test
  public void testPostInvalidJson() {
    GraphQLRequest request = new GraphQLRequest()
      .setRequestBody(new JsonArray().add("foo").add("bar").toBuffer());
    request.send(webClient, 400);
  }

  @Test
  public void testPostWithInvalidVariableParam() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setHttpQueryString("query=" + encode("query { allLinks { url } }") + "&variables=" + encode("[1,2,3]"))
      .setRequestBody(null);
    request.send(webClient, 400);
  }

  @Test
  public void testUnsupportedMediaType() {
    var response = client.request(HttpMethod.POST, "/graphql")
      .compose(req -> req
        .putHeader(HttpHeaders.CONTENT_TYPE, "text/html")
        .send(Buffer.buffer("<h1>Hello world!</h1>")))
      .await();
    assertEquals(415, response.statusCode());
  }

  @Test
  public void testContentTypeWithCharset() {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setContentType("application/json; charset=UTF-8");
    JsonObject body = request.send(webClient);
    assertTrue(testData.checkLinkUrls(testData.urls(), body), body.toString());
  }
}
