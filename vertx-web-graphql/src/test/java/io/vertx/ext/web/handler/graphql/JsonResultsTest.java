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
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;

/**
 * @author Thomas Segismont
 */
public class JsonResultsTest extends WebTestBase {

  private TestData testData = new TestData();
  private GraphQLHandler graphQLHandler;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    graphQLHandler = GraphQLHandler.create(graphQL());
    router.route("/graphql").handler(graphQLHandler);
  }

  private GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("links.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .wiringFactory(new WiringFactory() {
        @Override
        public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
          return new VertxPropertyDataFetcher(environment.getFieldDefinition().getName());
        }
      })
      .type("Query", builder -> builder.dataFetcher("allLinks", this::getAllLinks))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private JsonArray getAllLinks(DataFetchingEnvironment env) {
    boolean secureOnly = env.getArgument("secureOnly");
    return testData.links.entrySet().stream()
      .filter(e -> !secureOnly || e.getKey().startsWith("https://"))
      .map(e -> new JsonObject().put("url", e.getKey()).put("description", e.getValue()))
      .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
  }

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
  public void testSimplePost() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
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

}