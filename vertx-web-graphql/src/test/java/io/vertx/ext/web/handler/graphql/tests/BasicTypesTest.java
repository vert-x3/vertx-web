package io.vertx.ext.web.handler.graphql.tests;

import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.Value;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.core.http.HttpMethod.POST;
import static org.junit.jupiter.api.Assertions.*;

public class BasicTypesTest extends GraphQLTestBase {

  private final Counter counter = new Counter();
  private final Person[] philosophers;

  public BasicTypesTest() {
    Person plato = new Person("Plato");
    Person aristotle = new Person("Aristotle");
    plato.setFriend(aristotle);
    aristotle.setFriend(plato);
    philosophers = new Person[]{plato, aristotle};
  }

  @Override
  protected GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("types.graphqls").toString();
    final GraphQLScalarType datetime = GraphQLScalarType.newScalar()
      .name("Datetime")
      .coercing(new DatetimeCoercion())
      .build();
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .scalar(datetime)
      .type("Query", builder -> builder
        .dataFetcher("hello", env -> "Hello World!")
        .dataFetcher("number", env -> 130)
        .dataFetcher("changing", counter)
        .dataFetcher("persons", env -> philosophers)
        .dataFetcher("floating", env -> 3.14f)
        .dataFetcher("bool", env -> true)
        .dataFetcher("id", env -> "1001")
        .dataFetcher("enum", env -> Musketeer.ATHOS)
        .dataFetcher("when", env -> LocalDateTime.of(1991, 8, 25, 22, 57, 8))
        .dataFetcher("answer", env -> "Hello, " + env.getArgument("name") + "!")
        .dataFetcher("array", env -> new String[]{"apples", "eggs", "carrots"})
        .dataFetcher("list", env -> Arrays.asList("apples", "eggs", "carrots")))
      .build();


    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  @Test
  public void helloWorld() {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("hello", "Hello World!"));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { hello }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void integerNumber() {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("number", 130));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void floatingPointNumber() {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("floating", 3.14));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { floating }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void bool() {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("bool", true));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { bool }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void id() {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("id", "1001"));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { id }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void enumeration() {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("enum", Musketeer.ATHOS.toString()));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { enum }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void list() {
    JsonObject result = new JsonObject().put("data", new JsonObject()
      .put("list", new JsonArray()
        .add("apples")
        .add("eggs")
        .add("carrots")));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void alias() {
    JsonObject result = new JsonObject().put("data", new JsonObject()
      .put("arr", new JsonArray()
        .add("apples")
        .add("eggs")
        .add("carrots")));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { arr: array }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void userDefined() {
    LocalDateTime ldt = LocalDateTime.of(LocalDate.of(1991, 8, 25), LocalTime.of(22, 57, 8));
    String when = new DatetimeCoercion().serialize(ldt, null, null);
    JsonObject result = new JsonObject().put("data", new JsonObject().put("when", when));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { when }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void functionDefault() {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("answer", "Hello, someone!"));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { answer }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void function() {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("answer", "Hello, world!"));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { answer(name: \"world\") }");
    JsonObject body = request.send(webClient);
    assertEquals(result, body);
  }

  @Test
  public void cached() {
    GraphQLRequest request1 = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { changing }");
    JsonObject body1 = request1.send(webClient);

    GraphQLRequest request2 = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { changing }");
    JsonObject body2 = request2.send(webClient);

    Integer value1 = body1.getJsonObject("data").getInteger("changing");
    Integer value2 = body2.getJsonObject("data").getInteger("changing");
    assertNotEquals(value1, value2);
  }

  @Test
  public void recursive() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { persons { name , friend { name, friend { name } } } }");
    JsonObject body = request.send(webClient);
    JsonObject person = body.getJsonObject("data").getJsonArray("persons").getJsonObject(0);
    assertEquals("Plato", person.getString("name"));
    JsonObject friend = person.getJsonObject("friend");
    assertEquals("Aristotle", friend.getString("name"));
    JsonObject friendOfFriend = friend.getJsonObject("friend");
    assertEquals("Plato", friendOfFriend.getString("name"));
  }

  private enum Musketeer {
    ATHOS,
    PORTHOS,
    ARAMIS
  }

  private static class Counter implements DataFetcher<Integer> {
    final AtomicInteger order = new AtomicInteger(0);

    @Override
    public Integer get(DataFetchingEnvironment environment) {
      return order.getAndIncrement();
    }
  }

  private static class DatetimeCoercion implements Coercing<LocalDateTime, String> {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public String serialize(Object dataFetcherResult, GraphQLContext graphQLContext, Locale locale) throws CoercingSerializeException {
      if (dataFetcherResult == null)
        return null;
      if (!(dataFetcherResult instanceof LocalDateTime))
        throw new CoercingSerializeException(dataFetcherResult.getClass().getCanonicalName() + "is not a date!");
      LocalDateTime localDateTime = (LocalDateTime) dataFetcherResult;
      return dateTimeFormatter.format(localDateTime);
    }

    @Override
    public LocalDateTime parseValue(Object input, GraphQLContext graphQLContext, Locale locale) throws CoercingParseValueException {
      String source = input.toString();
      try {
        final TemporalAccessor temporalAccessorParsed = dateTimeFormatter.parse(source);
        return LocalDateTime.from(temporalAccessorParsed);
      } catch (DateTimeParseException dateTimeParseException) {
        throw new CoercingParseValueException(dateTimeParseException);
      }
    }

    @Override
    public LocalDateTime parseLiteral(Value<?> input, CoercedVariables variables, GraphQLContext graphQLContext, Locale locale) throws CoercingParseLiteralException {
      return parseValue(input, graphQLContext, locale);
    }
  }

  private static class Person {
    public String name;
    private Person friend;

    public Person(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setFriend(Person friend) {
      this.friend = friend;
    }

    public Person getFriend() {
      return friend;
    }
  }
}
