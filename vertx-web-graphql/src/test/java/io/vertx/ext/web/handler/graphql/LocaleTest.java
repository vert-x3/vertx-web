package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.WebTestBase;
import org.junit.Test;

import java.util.Locale;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.GET;

public class LocaleTest extends WebTestBase {

  private static final String LOCALE = "el-CY";


  protected TestData testData = new TestData();
  protected GraphQLHandler graphQLHandler;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    graphQLHandler = GraphQLHandler.create(graphQL()).locale(rc -> {
      for (LanguageHeader acceptableLocale : rc.acceptableLanguages()) {
        try {
          return Locale.forLanguageTag(acceptableLocale.value());
        } catch (RuntimeException ignored) {
        }
      }
      return null;
    });
    router.route("/graphql").order(100).handler(graphQLHandler);
  }

  protected GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("locale.graphqls").toString();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.dataFetcher("locale", this::getLocale))
      .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  private String getLocale(DataFetchingEnvironment env) {
    Locale locale = env.getLocale();
    if (locale == null)
      return null;
    return locale.toLanguageTag();
  }

  @Test
  public void testLocale() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale(LOCALE)
      .setGraphQLQuery("query { locale }");
    request.send(client, onSuccess(body -> {
      if (body.getJsonObject("data").getString("locale").equals(LOCALE)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testWrongLocale() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale("")
      .setGraphQLQuery("query { locale }");
    request.send(client, onSuccess(body -> {
      if (body.getJsonObject("data").getString("locale") != null) {
        fail(body.toString());
      } else {
        testComplete();
      }
    }));
    await();
  }

  @Test
  public void testMultipleLocale() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale(LOCALE + ",en-GB")
      .setGraphQLQuery("query { locale }");
    request.send(client, onSuccess(body -> {
      if (body.getJsonObject("data").getString("locale").equals(LOCALE)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }

  @Test
  public void testMultipleWrongLocales() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(GET)
      .setLocale(",,,," + LOCALE)
      .setGraphQLQuery("query { locale }");
    request.send(client, onSuccess(body -> {
      if (body.getJsonObject("data").getString("locale").equals(LOCALE)) {
        testComplete();
      } else {
        fail(body.toString());
      }
    }));
    await();
  }
}
