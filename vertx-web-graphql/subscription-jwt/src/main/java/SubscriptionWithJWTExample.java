/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

import graphql.GraphQL;
import graphql.schema.*;
import io.reactivex.Flowable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.graphql.ApolloWSHandler;
import io.vertx.ext.web.handler.graphql.ApolloWSMessage;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static graphql.Scalars.GraphQLString;

public class SubscriptionWithJWTExample extends AbstractVerticle {
  public static void main(String[] args) {
    Launcher.executeCommand(
      "run", SubscriptionWithJWTExample.class.getName());
  }

  @Override
  public void start(Promise<Void> startPromise) {

    GraphQLObjectType.Builder queryBuilder =
      GraphQLObjectType.newObject().name("Query");

    GraphQLObjectType.Builder subscriptionBuilder =
      GraphQLObjectType.newObject().name("Subscription");

    GraphQLObjectType graphQLObjectType =
      GraphQLObjectType.newObject().name("data")
        .field(GraphQLFieldDefinition.newFieldDefinition()
          .name("data")
          .type(GraphQLString)
          .build()
        ).build();

    queryBuilder
      .field(GraphQLFieldDefinition.newFieldDefinition()
        .name("default")
        .type(graphQLObjectType)
        .build()
      );

    subscriptionBuilder
      .field(GraphQLFieldDefinition.newFieldDefinition()
        .name("default")
        .type(graphQLObjectType)
        .build()
      );

    VertxDataFetcher<Map<String, String>> queryDataFetcher = new VertxDataFetcher<>((env, promise) -> {
        RoutingContext routingContext = env.getContext();
        User user = routingContext.user();
        JsonObject principal = user.principal();
        Map<String, String> map = new HashMap<>();
        map.put("data", "<user id: " + principal.getInteger("id") + ">");
        promise.complete(map);
      }
    );

    DataFetcher<Flowable<Map<String, String>>> subscriptionDataFetcher = env -> {
      ApolloWSMessage apolloWSMessage = env.getContext();
      User user = (User) apolloWSMessage.connectionParams();
      JsonObject principal = user.principal();
      return Flowable.interval(1, TimeUnit.SECONDS).map(timeout -> {
        Map<String, String> map = new HashMap<>();
        map.put("data", "<user id: " + principal.getInteger("id") + ", timeout: " + timeout + ">");
        return map;
      });
    };

    GraphQLCodeRegistry graphQLCodeRegistry = GraphQLCodeRegistry.newCodeRegistry()
      .dataFetcher(FieldCoordinates.coordinates("Query", "default"), queryDataFetcher)
      .dataFetcher(FieldCoordinates.coordinates("Subscription", "default"), subscriptionDataFetcher)
      .build();

    GraphQLSchema graphQLSchema = GraphQLSchema.newSchema()
      .query(queryBuilder)
      .subscription(subscriptionBuilder)
      .codeRegistry(graphQLCodeRegistry)
      .build();

    GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();

    JWTAuthOptions jwtAuthOptions = new JWTAuthOptions().addPubSecKey(
      new PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setPublicKey("random")
        .setSymmetric(true)
    );

    JWTAuth jwtAuth = JWTAuth.create(vertx, jwtAuthOptions);

    JWTAuthHandler jwtAuthHandler = JWTAuthHandler.create(jwtAuth);
    GraphQLHandler graphQLHandler = GraphQLHandler.create(graphQL);
    ApolloWSHandler apolloWSHandler = ApolloWSHandler.create(graphQL)
      .connectionParamsHandler((jsonObject, promise) -> {
        String authorization = jsonObject.getJsonObject("headers").getString("authorization");
        try {
          int idx = authorization.indexOf(' ');
          if (idx < 0) {
            promise.fail("wrong authorization format");
            return;
          }
          if (!authorization.substring(0, idx).equals("Bearer")) {
            promise.fail("only Bearer authorization supported");
            return;
          }

          jwtAuth.authenticate(new JsonObject().put("jwt", authorization.substring(idx + 1)), userAsyncResult -> {
            if (userAsyncResult.succeeded()) {
              promise.complete(userAsyncResult.result());
            } else {
              promise.fail(userAsyncResult.cause());
            }
          });
        } catch (RuntimeException e) {
          promise.fail(e);
        }
      });

    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create("*")
      .allowedMethod(io.vertx.core.http.HttpMethod.GET)
      .allowedMethod(io.vertx.core.http.HttpMethod.POST)
      .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
      .allowedHeader("Access-Control-Request-Method")
      .allowedHeader("Access-Control-Allow-Credentials")
      .allowedHeader("Access-Control-Allow-Origin")
      .allowedHeader("Access-Control-Allow-Headers")
      .allowedHeader("Authorization")
      .allowedHeader("Content-Type"));
    router.route("/v1/token").handler(ctx -> {
      JsonObject userData = new JsonObject().put("id", 123);
      String token = jwtAuth.generateToken(userData, new JWTOptions().setAlgorithm("HS256"));
      ctx.response().end(token);
    });
    router.route("/v1/graphql").handler(apolloWSHandler);
    router.route("/v1/*").handler(jwtAuthHandler);
    router.route("/v1/graphql").handler(graphQLHandler);
    vertx.createHttpServer(new HttpServerOptions().setWebSocketSubProtocols(Collections.singletonList("graphql-ws")))
      .requestHandler(router).listen(8080, httpServerAsyncResult -> {
      if (httpServerAsyncResult.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(httpServerAsyncResult.cause());
      }
    });
  }
}
