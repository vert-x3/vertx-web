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

import org.junit.Test;

import java.util.Set;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.ext.web.handler.graphql.GraphQLRequest.encode;
import static java.util.stream.Collectors.toSet;

/**
 * @author Thomas Segismont
 */
public class GetRequestsTest extends GraphQLTestBase {

  @Test
  public void testSimpleGet() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setGraphQLQuery("query { allLinks { url } }");
    request.send(client, onSuccess(body -> {
      if (testData.checkLinkUrls(testData.links.keySet(), body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testSimpleGetWithVariable() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setGraphQLQuery("query($secure: Boolean) { allLinks(secureOnly: $secure) { url } }");
    request.getVariables().put("secure", "true");
    request.send(client, onSuccess(body -> {
      Set<String> expected = testData.links.keySet().stream()
        .filter(url -> url.startsWith("https://"))
        .collect(toSet());
      if (testData.checkLinkUrls(expected, body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testGetNoQuery() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET);
    request.send(client, 400, onSuccess(v -> {
      testComplete();
    }));
    await();
  }

  @Test
  public void testGetInvalidVariable() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setHttpQueryString("query=" + encode("query { allLinks { url } }") + "&variables=" + encode("[1,2,3]"));
    request.send(client, 400, onSuccess(v -> {
      testComplete();
    }));
    await();
  }
}
