/*
 * Copyright 2023 Red Hat, Inc.
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
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.ext.web.handler.graphql.instrumentation.VertxFutureAdapter;
import org.junit.Test;

import static graphql.schema.idl.RuntimeWiring.*;
import static io.vertx.core.http.HttpMethod.*;

public class VertxFutureInstrumentationTest extends GraphQLTestBase {

  @Override
  protected GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("links.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> {
        DataFetcher<Object> dataFetcher = env -> Future.succeededFuture(getAllLinks(env));
        return builder.dataFetcher("allLinks", dataFetcher);
      })
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .instrumentation(VertxFutureAdapter.create())
      .build();
  }

  @Test
  public void testSimpleGet() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
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
}
