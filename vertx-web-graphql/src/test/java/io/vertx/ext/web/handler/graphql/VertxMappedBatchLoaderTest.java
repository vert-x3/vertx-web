/*
 * Copyright 2020 Red Hat, Inc.
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
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.dataloader.MappedBatchLoaderWithContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.util.stream.Collectors.toMap;

public class VertxMappedBatchLoaderTest extends GraphQLTestBase {

  private AtomicBoolean batchloaderInvoked = new AtomicBoolean();

  @Override
  public void setUp() throws Exception {
    super.setUp();

    MappedBatchLoaderWithContext<String, User> userBatchLoader = new VertxMappedBatchLoader<>(
      (keys, environment, mapPromise) -> {
        if (batchloaderInvoked.compareAndSet(false, true)) {
          mapPromise.complete(keys
            .stream()
            .map(testData.users::get)
            .collect(toMap(u -> u.getId(), Function.identity()))
          );
        } else {
          mapPromise.fail(new IllegalStateException());
        }
      }
    );

    graphQLHandler.dataLoaderRegistry(rc -> {
      DataLoader<String, User> userDataLoader = DataLoader.newMappedDataLoader(userBatchLoader);
      return new DataLoaderRegistry().register("user", userDataLoader);
    });
  }

  @Override
  protected GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("links.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("allLinks", this::getAllLinks))
      .type("Link", builder -> builder.dataFetcher("postedBy", this::getLinkPostedBy))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    DataLoaderDispatcherInstrumentation dispatcherInstrumentation = new DataLoaderDispatcherInstrumentation();

    return GraphQL.newGraphQL(graphQLSchema)
      .instrumentation(dispatcherInstrumentation)
      .build();
  }

  private Object getLinkPostedBy(DataFetchingEnvironment env) {
    Link link = env.getSource();
    DataLoader<String, User> user = env.getDataLoader("user");
    return user.load(link.getUserId());
  }

  @Test
  public void testSimplePost() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setGraphQLQuery("query { allLinks { url, postedBy { name } } }");

    request.send(client, onSuccess(body -> {
      if (testData.checkLinkPosters(testData.posters(), body)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));

    await();
  }
}
