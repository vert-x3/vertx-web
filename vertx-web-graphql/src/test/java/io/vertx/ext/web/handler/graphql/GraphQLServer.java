package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;

public abstract class GraphQLServer extends AbstractVerticle {
  public abstract String getSchema();

  public abstract RuntimeWiring getWiring();

  @Override
  public void start(Promise<Void> startPromise) {
    GraphQL graphQL = createGraphQL();
    Router router = Router.router(getVertx());
    router.route().handler(BodyHandler.create());
    router.route("/graphql").handler(GraphQLHandler.create(graphQL));
    router.get("/health").handler(routingContext -> routingContext.response().end("OK"));
    vertx.createHttpServer().requestHandler(router).listen(8080, httpServerAsyncResult -> {
      if (httpServerAsyncResult.succeeded()) {
        startPromise.complete();
      } else {
        startPromise.fail(httpServerAsyncResult.cause());
      }
    });
  }

  protected GraphQL createGraphQL() {
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry registry = schemaParser.parse(getSchema());

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(registry, getWiring());

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }
}
