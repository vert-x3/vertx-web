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

package examples;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.WiringFactory;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.*;
import io.vertx.ext.web.handler.graphql.dataloader.VertxBatchLoader;
import io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher;
import io.vertx.ext.web.handler.graphql.schema.VertxPropertyDataFetcher;
import io.vertx.ext.web.handler.graphql.ws.GraphQLWSHandler;
import org.dataloader.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Thomas SEGISMONT
 */
@SuppressWarnings("unused")
public class GraphQLExamples {

  public void handlerSetup(Router router) {
    GraphQL graphQL = setupGraphQLJava();

    router.route("/graphql").handler(GraphQLHandler.create(graphQL));
  }

  public void handlerSetupPost(Router router) {
    GraphQL graphQL = setupGraphQLJava();

    router.post("/graphql").handler(GraphQLHandler.create(graphQL));
  }

  public void handlerSetupGraphiQL(GraphQL graphQL, Router router) {
    GraphiQLHandlerOptions options = new GraphiQLHandlerOptions()
      .setEnabled(true);

    router.route("/graphiql/*").handler(GraphiQLHandler.create(options));
  }

  public void handlerSetupGraphiQLAuthn(GraphiQLHandler graphiQLHandler, Router router) {
    graphiQLHandler.graphiQLRequestHeaders(rc -> {
      String token = rc.get("token");
      return MultiMap.caseInsensitiveMultiMap().add("Authorization", "Bearer " + token);
    });

    router.route("/graphiql/*").handler(graphiQLHandler);
  }

  public void handlerSetupBatching(GraphQL graphQL) {
    GraphQLHandlerOptions options = new GraphQLHandlerOptions()
      .setRequestBatchingEnabled(true);

    GraphQLHandler handler = GraphQLHandler.create(graphQL, options);
  }

  public void setupGraphQLHandlerMultipart(Vertx vertx) {
    GraphQLHandler graphQLHandler = GraphQLHandler.create(
      setupGraphQLJava(),
      new GraphQLHandlerOptions().setRequestMultipartEnabled(true)
    );

    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());
    router.route("/graphql").handler(graphQLHandler);
  }

  public void setRuntimeScalar() {
    RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring().scalar(UploadScalar.build()).build();
  }

  public void getFileUpload(DataFetchingEnvironment environment) {
    FileUpload file = environment.getArgument("myFile");
  }

  private GraphQL setupGraphQLJava() {
    return null;
  }

  static class Link {
  }

  public void completionStageDataFetcher() {
    DataFetcher<CompletionStage<List<Link>>> dataFetcher = environment -> {

      CompletableFuture<List<Link>> completableFuture = new CompletableFuture<>();

      retrieveLinksFromBackend(environment, ar -> {
        if (ar.succeeded()) {
          completableFuture.complete(ar.result());
        } else {
          completableFuture.completeExceptionally(ar.cause());
        }
      });

      return completableFuture;
    };

    RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("allLinks", dataFetcher))
      .build();
  }

  private void retrieveLinksFromBackend(DataFetchingEnvironment environment, Handler<AsyncResult<List<Link>>> handler) {
  }

  public void callbackDataFetcher() {
    VertxDataFetcher<List<Link>> dataFetcher = VertxDataFetcher.create((env, promise) -> {
      retrieveLinksFromBackend(env, promise);
    });

    RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("allLinks", dataFetcher))
      .build();
  }

  public void futureDataFetcher() {
    VertxDataFetcher<List<Link>> dataFetcher = VertxDataFetcher.create(environment -> {
      Future<List<Link>> future = retrieveLinksFromBackend(environment);
      return future;
    });

    RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("allLinks", dataFetcher))
      .build();
  }

  private Future<List<Link>> retrieveLinksFromBackend(DataFetchingEnvironment environment) {
    return null;
  }

  static class User {
  }

  private void routingContextInDataFetchingEnvironment() {
    VertxDataFetcher<List<Link>> dataFetcher = VertxDataFetcher.create((environment, promise) -> {

      RoutingContext routingContext = GraphQLHandler.getRoutingContext(environment.getGraphQlContext());

      User user = routingContext.get("user");

      retrieveLinksPostedBy(user, promise);

    });
  }

  private void retrieveLinksPostedBy(User user, Handler<AsyncResult<List<Link>>> handler) {
  }

  private GraphQL setupGraphQLJava(VertxDataFetcher<List<Link>> dataFetcher) {
    return null;
  }

  public void jsonData() {
    RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

    builder.wiringFactory(new WiringFactory() {

      @Override
      public DataFetcher<Object> getDefaultDataFetcher(FieldWiringEnvironment environment) {

        return VertxPropertyDataFetcher.create(environment.getFieldDefinition().getName());

      }
    });
  }

  public void createBatchLoader() {
    BatchLoaderWithContext<String, Link> linksBatchLoader = (ids, env) -> {
      // retrieveLinksFromBackend takes a list of ids and returns a CompletionStage for a list of links
      return retrieveLinksFromBackend(ids, env);
    };
  }

  private CompletionStage<List<Link>> retrieveLinksFromBackend(List<String> ids, BatchLoaderEnvironment environment) {
    return null;
  }

  public void dataLoaderRegistry(GraphQL graphQL, BatchLoaderWithContext<String, Link> linksBatchLoader) {
    GraphQLHandler handler = GraphQLHandler.create(graphQL).beforeExecute(builderWithContext -> {

      DataLoader<String, Link> linkDataLoader = DataLoaderFactory.newDataLoader(linksBatchLoader);

      DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry().register("link", linkDataLoader);

      builderWithContext.builder().dataLoaderRegistry(dataLoaderRegistry);

    });
  }

  public void createVertxBatchLoader() {
    BatchLoaderWithContext<Long, String> commentsBatchLoader = VertxBatchLoader.create((ids, env) -> {
      // findComments takes a list of ids and returns a Future for a list of links
      return findComments(ids, env);
    });
  }

  private Future<List<String>> findComments(List<Long> ids, BatchLoaderEnvironment env) {
    return null;
  }

  public void addApolloWsHandlerToRouter(Router router) {
    GraphQL graphQL = setupGraphQLJava();

    router.route("/graphql").handler(ApolloWSHandler.create(graphQL));
  }

  public void configureServerForApolloWs(Vertx vertx, Router router) {
    HttpServerOptions httpServerOptions = new HttpServerOptions()
      .addWebSocketSubProtocol("graphql-ws");
    vertx.createHttpServer(httpServerOptions)
      .requestHandler(router)
      .listen(8080);
  }

  public void configureWebSocketLinkAndHttpLinkSamePath(Router router) {
    GraphQL graphQL = setupGraphQLJava();

    router.route("/graphql").handler(ApolloWSHandler.create(graphQL));
    router.route("/graphql").handler(GraphQLHandler.create(graphQL));
  }

  public void addGraphQLWSHandlerToRouter(Router router, GraphQL graphQL) {
    router.route("/graphql").handler(GraphQLWSHandler.create(graphQL));
  }

  public void configureServerForGraphQLWS() {
    HttpServerOptions httpServerOptions = new HttpServerOptions()
      .addWebSocketSubProtocol("graphql-transport-ws");
  }

  public void configureGraphQLWSAndHttpOnSamePath(Router router, GraphQL graphQL) {
    router.route("/graphql")
      .handler(GraphQLWSHandler.create(graphQL))
      .handler(GraphQLHandler.create(graphQL));
  }
}
