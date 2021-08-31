/*
 * Copyright 2021 Red Hat, Inc.
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
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.graphql.ws.GraphQLWSHandler;
import org.reactivestreams.Publisher;

import java.util.stream.Stream;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

/**
 * Backend for the GraphQLWS compatibility tests.
 */
public class GraphQLWSTestsServer extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new GraphQLWSTestsServer());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    router.route().handler(CorsHandler.create("*").allowedMethod(GET).allowedMethod(POST));
    router.route().handler(BodyHandler.create());

    GraphQL graphQL = setupGraphQL();

    router.route("/graphql").handler(GraphQLWSHandler.create(graphQL)
      .connectionInitHandler(connectionInitEvent -> {
        JsonObject payload = connectionInitEvent.message().message().getJsonObject("payload");
        if (payload != null && payload.containsKey("rejectMessage")) {
          connectionInitEvent.fail(payload.getString("rejectMessage"));
          return;
        }
        connectionInitEvent.complete(payload);
      }));

    HttpServerOptions httpServerOptions = new HttpServerOptions().addWebSocketSubProtocol("graphql-transport-ws");
    vertx.createHttpServer(httpServerOptions)
      .requestHandler(router)
      .listen(8080)
      .<Void>mapEmpty()
      .onComplete(ar -> {
        if (ar.succeeded()) {
          System.out.println("GraphQLWS tests server started");
        }
        startPromise.handle(ar);
      });

  }

  private GraphQL setupGraphQL() {
    String schema = vertx.fileSystem().readFileBlocking("hello.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("hello", this::hello))
      .type("Subscription", builder -> builder.dataFetcher("greetings", this::greetings))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private String hello(DataFetchingEnvironment env) {
    return "Hello World!";
  }

  private Publisher<String> greetings(DataFetchingEnvironment env) {
    return subscriber -> {
      Stream.of("Hi", "Bonjour", "Hola", "Ciao", "Zdravo").forEach(subscriber::onNext);
      subscriber.onComplete();
    };
  }
}
