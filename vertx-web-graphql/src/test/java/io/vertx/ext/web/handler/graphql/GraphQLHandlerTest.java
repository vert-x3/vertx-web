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

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.WebTestBase;
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.PUT;
import static io.vertx.ext.web.handler.graphql.GraphQLRequest.GRAPHQL;
import static io.vertx.ext.web.handler.graphql.GraphQLRequest.encode;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Thomas Segismont
 */
public class GraphQLHandlerTest extends WebTestBase {

  private Map<String, String> links = new HashMap<>();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    createData();
    router.route("/graphql").order(100).handler(GraphQLHandler.create(graphQL()));
  }

  private void createData() {
    links.put("https://vertx.io", "Vert.x project");
    links.put("https://www.eclipse.org", "Eclipse Foundation");
    links.put("http://reactivex.io", "ReactiveX libraries");
    links.put("https://www.graphql-java.com", "GraphQL Java implementation");
  }

  private GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("links.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("allLinks", this::getAllLinks))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private List<Link> getAllLinks(DataFetchingEnvironment env) {
    boolean secureOnly = env.getArgument("secureOnly");
    return links.entrySet().stream()
      .filter(e -> !secureOnly || e.getKey().startsWith("https://"))
      .map(e -> new Link(e.getKey(), e.getValue()))
      .collect(toList());
  }

  @Test
  public void testSimpleGet() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setGraphQLQuery("query { allLinks { url } }");
    request.send(client, onSuccess(body -> {
      checkLinkUrls(links.keySet(), body);
      testComplete();
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
      Set<String> expected = links.keySet().stream()
        .filter(url -> url.startsWith("https://"))
        .collect(toSet());
      checkLinkUrls(expected, body);
      testComplete();
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

  @Test
  public void testSimplePost() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }");
    request.send(client, onSuccess(body -> {
      checkLinkUrls(links.keySet(), body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSimplePostNoContentType() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setContentType(null);
    request.send(client, onSuccess(body -> {
      checkLinkUrls(links.keySet(), body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSimplePostWithBodyHandler() throws Exception {
    router.route("/graphql").order(99).handler(BodyHandler.create());
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }");
    request.send(client, onSuccess(body -> {
      checkLinkUrls(links.keySet(), body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSimplePostQueryInParam() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setGraphQLQueryAsParam(true);
    request.send(client, onSuccess(body -> {
      checkLinkUrls(links.keySet(), body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSimplePostQueryAsBody() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url } }")
      .setContentType(GRAPHQL);
    request.send(client, onSuccess(body -> {
      checkLinkUrls(links.keySet(), body);
      testComplete();
    }));
    await();
  }

  @Test
  public void testSimplePostWithVariable() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query($secure: Boolean) { allLinks(secureOnly: $secure) { url } }");
    request.getVariables().put("secure", "true");
    request.send(client, onSuccess(body -> {
      Set<String> expected = links.keySet().stream()
        .filter(url -> url.startsWith("https://"))
        .collect(toSet());
      checkLinkUrls(expected, body);
      testComplete();
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
  public void testUnsupportedMethod() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(PUT)
      .setGraphQLQuery("query { allLinks { url } }");
    request.send(client, 405, onSuccess(v -> {
      testComplete();
    }));
    await();
  }

  @Test
  public void testUnsupportedMediaType() throws Exception {
    client.post("/graphql")
      .putHeader(HttpHeaders.CONTENT_TYPE, "text/html")
      .handler(onSuccess(response -> {
        assertEquals(415, response.statusCode());
        testComplete();
      })).end("<h1>Hello world!</h1>");
    await();
  }

  private void checkLinkUrls(Set<String> expected, JsonObject body) {
    String bodyAsString = body.toString();
    assertFalse(bodyAsString, body.containsKey("errors"));
    JsonObject data = body.getJsonObject("data");
    List<String> urls = data.getJsonArray("allLinks").stream()
      .map(JsonObject.class::cast)
      .map(json -> json.getString("url"))
      .collect(toList());
    assertTrue(bodyAsString, urls.containsAll(expected) && expected.containsAll(urls));
  }
}
