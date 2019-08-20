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
import io.vertx.ext.web.handler.graphql.impl.ApolloWSMessageType;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;
import static java.util.stream.Collectors.toList;

/**
 * @author Rogelio Orts
 */
public class ApolloWSHandlerTest extends WebTestBase {

  private static final int MAX_COUNT = 4;
  private static final int STATIC_COUNT = 5;

  private ApolloWSOptions apolloWSOptions = new ApolloWSOptions();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    GraphQL graphQL = graphQL();
    router.route("/graphql").handler(ApolloWSHandler.create(graphQL, apolloWSOptions));
    router.route("/graphql").handler(GraphQLHandler.create(graphQL));
  }

  protected GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("counter.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("staticCounter", this::getStaticCounter))
      .type("Subscription", builder -> builder.dataFetcher("counter", this::getCounter))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private Map<String, Object> getStaticCounter(DataFetchingEnvironment env) {
    int count = env.getArgument("num");
    Map<String, Object> counter = new HashMap<>();
    counter.put("count", count);
    return counter;
  }

  private Publisher<Map<String, Object>> getCounter(DataFetchingEnvironment env) {
    return subscriber -> IntStream.range(0, 5).forEach(num -> {
      Map<String, Object> counter = new HashMap<>();
      counter.put("count", num);

      subscriber.onNext(counter);
    });
  }

  @Test
  public void testSubscriptionWsCall() {
    client.webSocket("/graphql", onSuccess(websocket -> {
      websocket.exceptionHandler(this::fail);

      AtomicInteger counter = new AtomicInteger(0);
      websocket.handler(buffer -> {
        try {
          JsonObject obj = buffer.toJsonObject();
          int num = obj.getJsonObject("payload")
            .getJsonObject("data")
            .getJsonObject("counter")
            .getInteger("count");

          assertEquals(counter.getAndIncrement(), num);
          if (num == MAX_COUNT) {
            complete();
          }
        } catch (Exception e) {
          fail(e);
        }
      });

      JsonObject message = new JsonObject()
        .put("payload", new JsonObject()
          .put("query", "subscription Subscription { counter { count } }"))
        .put("type", "start")
        .put("id", "1");
      websocket.write(message.toBuffer());
    }));
    await();
  }

  @Test
  public void testQueryWsCall() {
    client.webSocket("/graphql", onSuccess(websocket -> {
      websocket.exceptionHandler(this::fail);

      websocket.handler(buffer -> {
        try {
          JsonObject obj = buffer.toJsonObject();
          int num = obj.getJsonObject("payload")
            .getJsonObject("data")
            .getJsonObject("staticCounter")
            .getInteger("count");

          assertEquals(STATIC_COUNT, num);
          complete();
        } catch (Exception e) {
          fail(e);
        }
      });

      JsonObject message = new JsonObject()
        .put("payload", new JsonObject()
          .put("query", "query Query { staticCounter { count } }"))
        .put("type", "start")
        .put("id", "1");
      websocket.write(message.toBuffer());
    }));
    await();
  }

  @Test
  public void testQueryHttpCall() throws Exception {
    String query = "query Query { staticCounter { count } }";
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setGraphQLQuery(query);
    request.send(client, onSuccess(body -> {
      int count = body.getJsonObject("data")
        .getJsonObject("staticCounter")
        .getInteger("count");
      assertEquals(STATIC_COUNT, count);
      complete();
    }));
    await();
  }

  @Test
  public void testWsKeepAlive() {
    apolloWSOptions.setKeepAlive(100L);

    client.webSocket("/graphql", onSuccess(websocket -> {
      websocket.exceptionHandler(this::fail);

      AtomicInteger counter = new AtomicInteger(0);
      websocket.handler(buffer -> {
        try {
          JsonObject obj = buffer.toJsonObject();

          if (counter.getAndIncrement() == 0) {
            assertEquals(ApolloWSMessageType.CONNECTION_ACK.getText(), obj.getString("type"));
          } else {
            assertEquals(ApolloWSMessageType.CONNECTION_KEEP_ALIVE.getText(), obj.getString("type"));
            complete();
          }
        } catch (Exception e) {
          fail(e);
        }
      });

      JsonObject message = new JsonObject()
        .put("type", "connection_init");
      websocket.write(message.toBuffer());
    }));
    await();
  }

}
