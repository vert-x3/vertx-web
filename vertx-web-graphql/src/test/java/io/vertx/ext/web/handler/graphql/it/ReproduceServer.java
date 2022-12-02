package io.vertx.ext.web.handler.graphql.it;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class ReproduceServer extends AbstractVerticle {

  public static void main(String[] args) {
    Launcher.executeCommand("run", ReproduceServer.class.getName());
  }
  @Override
  public void start() {
    GraphQL graphQL = createGraphQL();
    Router router = Router.router(getVertx());
    router.route("/graphql").handler(GraphQLHandler.create(graphQL));
    getVertx().createHttpServer()
      .requestHandler(router)
      .listen(8080);
  }
  private GraphQL createGraphQL() {
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry registry = schemaParser.parse("type Query {nan:Float!\ninfinity: Float\ninfinity_neg: Float}");

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    final RuntimeWiring wiring = newRuntimeWiring()
      .type("Query",
            builder -> builder
              .dataFetcher("nan", env -> Float.NaN)
              .dataFetcher("infinity", env -> Float.POSITIVE_INFINITY)
              .dataFetcher("infinity_neg", env -> Float.NEGATIVE_INFINITY)
      )
      .build();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(registry, wiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }
}
