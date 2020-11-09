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
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.reactivestreams.Publisher;

import java.util.*;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static java.util.stream.Collectors.toList;

/**
 * Backend for the Apollo Link compatibility tests.
 *
 * @author Thomas Segismont
 */
public class ApolloTestsServer extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new ApolloTestsServer());
  }

  private final TestData testData = new TestData();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    router.route().handler(CorsHandler.create("*").allowedMethod(GET).allowedMethod(POST));
    router.route().handler(BodyHandler.create());
    router.route("/graphql").handler(ApolloWSHandler.create(setupWsGraphQL())
      .connectionInitHandler(connectionInitEvent -> {
        JsonObject payload = connectionInitEvent.message().content().getJsonObject("payload");
        if (payload != null && payload.containsKey("rejectMessage")) {
          connectionInitEvent.fail(payload.getString("rejectMessage"));
          return;
        }
        connectionInitEvent.complete(payload);
    }));

    GraphQLHandlerOptions graphQLHandlerOptions = new GraphQLHandlerOptions()
      .setRequestBatchingEnabled(true)
      .setRequestMultipartEnabled(true);
    router.route("/graphql").handler(GraphQLHandler.create(setupGraphQL(), graphQLHandlerOptions));

    HttpServerOptions httpServerOptions = new HttpServerOptions().addWebSocketSubProtocol("graphql-ws");
    vertx.createHttpServer(httpServerOptions)
      .requestHandler(router)
      .listen(8080)
      .<Void>mapEmpty()
      .onComplete(ar -> {
        if (ar.succeeded()) {
          System.out.println("Apollo tests server started");
        }
        startPromise.handle(ar);
      });

  }

  private GraphQL setupGraphQL() {
    String schema = vertx.fileSystem().readFileBlocking("links.graphqls").toString();
    String uploadSchema = vertx.fileSystem().readFileBlocking("upload.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema)
      .merge(schemaParser.parse(uploadSchema));

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .scalar(UploadScalar.build())
      .type("Query", builder -> builder.dataFetcher("allLinks", this::getAllLinks))
      .type("Mutation", builder -> builder.dataFetcher("singleUpload", this::singleUpload))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private GraphQL setupWsGraphQL() {
    String schema = vertx.fileSystem().readFileBlocking("counter.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("staticCounter", this::staticCounter))
      .type("Subscription", builder -> builder.dataFetcher("counter", this::counter))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private Object getAllLinks(DataFetchingEnvironment env) {
    boolean secureOnly = env.getArgument("secureOnly");
    return testData.links.stream()
      .filter(link -> !secureOnly || link.getUrl().startsWith("https://"))
      .collect(toList());
  }

  private Object staticCounter(DataFetchingEnvironment env) {
    int count = env.getArgument("num");
    Map<String, Object> counter = new HashMap<>();
    counter.put("count", count);
    return counter;
  }

  private Publisher<Object> counter(DataFetchingEnvironment env) {
    return subscriber -> {
      ApolloWSMessage message = env.getContext();
      JsonObject connectionParams = message.connectionParams() == null
        ? new JsonObject()
        : (JsonObject) message.connectionParams();
      Map<String, Object> counter = new HashMap<>();
      counter.put("count", connectionParams.getInteger("count", 1));

      subscriber.onNext(counter);
      subscriber.onComplete();
    };
  }

  private Object singleUpload(DataFetchingEnvironment env) {
    final FileUpload file = env.getArgument("file");
    return new Result(file.fileName());
  }
}
