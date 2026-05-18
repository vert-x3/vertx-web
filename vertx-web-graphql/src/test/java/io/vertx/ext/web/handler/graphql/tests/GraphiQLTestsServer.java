package io.vertx.ext.web.handler.graphql.tests;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

/**
 * Backend for the GraphiQL smoke tests.
 */
public class GraphiQLTestsServer extends VerticleBase {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new GraphiQLTestsServer()).await();
    System.out.println("GraphiQL tests server started");
  }

  @Override
  public Future<?> start() {
    Router router = Router.router(vertx);

    router.route().handler(CorsHandler.create().addOrigin("*").allowedMethod(GET).allowedMethod(POST));
    router.route().handler(BodyHandler.create());

    GraphQL graphQL = setupGraphQL();

    router.route("/graphql").handler(GraphQLHandler.create(graphQL));

    GraphiQLHandlerOptions graphiQLOptions = new GraphiQLHandlerOptions()
      .setEnabled(true)
      .setGraphQLUri("/graphql")
      .setGraphQLWSEnabled(false);
    router.route("/graphiql/*").subRouter(GraphiQLHandler.create(vertx, graphiQLOptions).router());

    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080);
  }

  private GraphQL setupGraphQL() {
    String schema = vertx.fileSystem().readFileBlocking("hello.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("hello", this::hello))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private String hello(DataFetchingEnvironment env) {
    return "Hello World!";
  }
}
