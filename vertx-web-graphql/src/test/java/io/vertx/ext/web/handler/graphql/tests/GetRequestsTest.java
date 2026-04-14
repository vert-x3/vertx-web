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

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.vertx.core.http.HttpMethod.*;
import static io.vertx.ext.web.handler.graphql.tests.GraphQLRequest.*;
import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Segismont
 */
public class GetRequestsTest extends GraphQLTestBase {

  @Test
  public void testSimpleGet() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setGraphQLQuery("query { allLinks { url } }");
    JsonObject body = request.send(webClient);
    assertTrue(testData.checkLinkUrls(testData.urls(), body), body.toString());
  }

  @Test
  public void testMultipleQueriesWithOperationName() {
    String query = "query foo { allLinks { url } }"
      + " "
      + "query bar($secure: Boolean) { allLinks(secureOnly: $secure) { url } }";
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
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
  public void testSimpleGetWithVariable() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setGraphQLQuery("query($secure: Boolean) { allLinks(secureOnly: $secure) { url } }")
      .addVariable("secure", true);
    JsonObject body = request.send(webClient);
    List<String> expected = testData.urls().stream()
      .filter(url -> url.startsWith("https://"))
      .collect(toList());
    assertTrue(testData.checkLinkUrls(expected, body), body.toString());
  }

  @Test
  public void testSimpleGetWithInitialValue() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setGraphQLQuery("query { allLinks { description } }")
      .setInitialValueAsParam(true)
      .setInitialValue("100");
    JsonObject body = request.send(webClient);
    String[] descriptions = new String[testData.links.size()];
    Arrays.fill(descriptions, "100");
    List<String> expected = Arrays.asList(descriptions);
    assertTrue(testData.checkLinkDescriptions(expected, body), body.toString());
  }

  @Test
  public void testSimpleGetNoInitialValue() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setGraphQLQuery("query { allLinks { description } }");
    JsonObject body = request.send(webClient);
    assertTrue(testData.checkLinkDescriptions(testData.descriptions(), body), body.toString());
  }

  @Test
  public void testGetNoQuery() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET);
    request.send(webClient, 400);
  }

  @Test
  public void testGetInvalidVariable() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setHttpQueryString("query=" + encode("query { allLinks { url } }") + "&variables=" + encode("[1,2,3]"));
    request.send(webClient, 400);
  }
}
