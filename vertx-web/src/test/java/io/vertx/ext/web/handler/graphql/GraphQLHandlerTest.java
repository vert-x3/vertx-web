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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.util.stream.Collectors.toList;

/**
 * @author Thomas Segismont
 */
public class GraphQLHandlerTest extends WebTestBase {

  private Map<String, String> links = new HashMap<>();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    createData();
    router.route("/graphql").handler(GraphQLHandler.create(graphQL()));
  }

  private void createData() {
    links.put("https://vertx.io", "Vert.x project");
    links.put("https://www.eclipse.org", "Eclipse Foundation");
    links.put("http://reactivex.io", "ReactiveX libraries");
  }

  @Test
  public void testSimpleGet() throws Exception {
    client.get("/graphql?query=" + URLEncoder.encode("query { allLinks { url } }", "UTF-8"))
      .handler(onSuccess(response -> {
        assertEquals(200, response.statusCode());
        response.bodyHandler(buffer -> {
          JsonObject body = new JsonObject(buffer);
          String bodyAsString = body.toString();
          assertFalse(bodyAsString, body.containsKey("errors"));
          JsonObject data = body.getJsonObject("data");
          List<String> urls = data.getJsonArray("allLinks").stream()
            .map(JsonObject.class::cast)
            .map(json -> json.getString("url"))
            .collect(toList());
          assertTrue(bodyAsString, urls.containsAll(links.keySet()) && links.keySet().containsAll(urls));
          testComplete();
        });
      }))
      .end();
    await();
  }

  @Test
  public void testGetNoQuery() throws Exception {
    client.get("/graphql")
      .handler(onSuccess(response -> {
        assertEquals(400, response.statusCode());
        testComplete();
      }))
      .end();
    await();
  }

  private GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("io/vertx/ext/web/handler/graphql/schema.graphqls").toString();

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

  private List<Link> getAllLinks(DataFetchingEnvironment dataFetchingEnvironment) {
    return links.entrySet().stream()
      .map(e -> new Link(e.getKey(), e.getValue()))
      .collect(toList());
  }
}
