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

package examples;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.WiringFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher;
import io.vertx.ext.web.handler.graphql.schema.VertxPropertyDataFetcher;
import io.vertx.ext.web.handler.graphql.*;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.BatchLoaderWithContext;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Thomas SEGISMONT
 */
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
      return MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
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

  class Link {
  }

  private void completionStageDataFetcher() {
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

  private void vertxDataFetcher() {
    VertxDataFetcher<List<Link>> dataFetcher = VertxDataFetcher.create(this::retrieveLinksFromBackend);

    RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("allLinks", dataFetcher))
      .build();
  }

  class User {
  }

  private void routingContextInDataFetchingEnvironment() {
    VertxDataFetcher<List<Link>> dataFetcher = VertxDataFetcher.create((environment, future) -> {

      RoutingContext routingContext = environment.getContext();

      User user = routingContext.get("user");

      retrieveLinksPostedBy(user, future);

    });
  }

  private void retrieveLinksPostedBy(User user, Handler<AsyncResult<List<Link>>> future) {
  }

  private void customContextInDataFetchingEnvironment(Router router) {
    VertxDataFetcher<List<Link>> dataFetcher = VertxDataFetcher.create((environment, future) -> {

      // User as custom context object
      User user = environment.getContext();

      retrieveLinksPostedBy(user, future);

    });

    GraphQL graphQL = setupGraphQLJava(dataFetcher);

    // Customize the query context object when setting up the handler
    GraphQLHandler handler = GraphQLHandler.create(graphQL).queryContext(routingContext -> {

      return routingContext.get("user");

    });

    router.route("/graphql").handler(handler);
  }

  private GraphQL setupGraphQLJava(VertxDataFetcher<List<Link>> dataFetcher) {
    return null;
  }

  private void jsonData() {
    RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

    builder.wiringFactory(new WiringFactory() {

      @Override
      public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {

        return VertxPropertyDataFetcher.create(environment.getFieldDefinition().getName());

      }
    });
  }

  public void createBatchLoader() {
    BatchLoaderWithContext<String, Link> linksBatchLoader = (keys, environment) -> {

      return retrieveLinksFromBackend(keys, environment);

    };
  }

  private CompletionStage<List<Link>> retrieveLinksFromBackend(List<String> ids, BatchLoaderEnvironment environment) {
    return null;
  }

  public void dataLoaderRegistry(GraphQL graphQL, BatchLoaderWithContext<String, Link> linksBatchLoader) {
    GraphQLHandler handler = GraphQLHandler.create(graphQL).dataLoaderRegistry(rc -> {

      DataLoader<String, Link> linkDataLoader = DataLoader.newDataLoader(linksBatchLoader);

      return new DataLoaderRegistry().register("link", linkDataLoader);

    });
  }

  public void addApolloWsHandlerToRouter(Router router) {
    GraphQL graphQL = setupGraphQLJava();

    router.route("/graphql").handler(ApolloWSHandler.create(graphQL));
  }

  public void configureServerForApolloWs(Vertx vertx, Router router) {
    HttpServerOptions httpServerOptions = new HttpServerOptions()
      .setWebsocketSubProtocols("graphql-ws");
    vertx.createHttpServer(httpServerOptions)
      .requestHandler(router)
      .listen(8080);
  }

  public void configureWebSocketLinkAndHttpLinkSamePath(Router router) {
    GraphQL graphQL = setupGraphQLJava();

    router.route("/graphql").handler(ApolloWSHandler.create(graphQL));
    router.route("/graphql").handler(GraphQLHandler.create(graphQL));
  }

}
