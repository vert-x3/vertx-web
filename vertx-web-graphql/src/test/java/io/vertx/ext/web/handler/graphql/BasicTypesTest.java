package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static graphql.schema.idl.RuntimeWiring.*;
import static io.vertx.core.http.HttpMethod.*;

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
  public void helloWorld() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("hello", "Hello World!"));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { hello }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void integerNumber() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("number", 130));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void floatingPointNumber() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("floating", 3.14));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { floating }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void bool() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("bool", true));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { bool }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void id() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("id", "1001"));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { id }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void enumeration() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("enum", Musketeer.ATHOS.toString()));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { enum }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void list() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject()
      .put("list", new JsonArray()
        .add("apples")
        .add("eggs")
        .add("carrots")));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void alias() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject()
      .put("arr", new JsonArray()
        .add("apples")
        .add("eggs")
        .add("carrots")));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { arr: array }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void userDefined() throws Exception {
    String when = new DatetimeCoercion().serialize(LocalDateTime.of(LocalDate.of(1991, 8, 25), LocalTime.of(22, 57, 8)));
    JsonObject result = new JsonObject().put("data", new JsonObject().put("when", when));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { when }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void functionDefault() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("answer", "Hello, someone!"));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { answer }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void function() throws Exception {
    JsonObject result = new JsonObject().put("data", new JsonObject().put("answer", "Hello, world!"));
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { answer(name: \"world\") }");
    request.send(client, onSuccess(body -> {
      assertEquals(result, body);
      testComplete();
    }));
    await();
  }

  @Test
  public void cached() throws Exception {
    Promise<JsonObject> promise1 = Promise.promise();
    GraphQLRequest request1 = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { changing }");
    request1.send(client, promise1);

    Promise<JsonObject> promise2 = Promise.promise();
    GraphQLRequest request2 = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { changing }");
    request2.send(client, promise2);

    CompositeFuture.all(promise1.future(), promise2.future()).onComplete(onSuccess(compositeFuture -> {
      List<JsonObject> values = compositeFuture.list();
      Integer value1 = values.get(0).getJsonObject("data").getInteger("changing");
      Integer value2 = values.get(1).getJsonObject("data").getInteger("changing");
      assertFalse(value1.equals(value2));
      testComplete();
    }));

    await();
  }

  @Test
  public void recursive() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { persons { name , friend { name, friend { name } } } }");
    request.send(client, onSuccess(body -> {
      JsonObject person = body.getJsonObject("data").getJsonArray("persons").getJsonObject(0);
      assertEquals("Plato", person.getString("name"));
      JsonObject friend = person.getJsonObject("friend");
      assertEquals("Aristotle", friend.getString("name"));
      JsonObject friendOfFriend = friend.getJsonObject("friend");
      assertEquals("Plato", friendOfFriend.getString("name"));
      testComplete();
    }));
    await();
  }

  private enum Musketeer {
    ATHOS,
    PORTHOS,
    ARAMIS;
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
    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
      if (dataFetcherResult == null)
        return null;
      if (!(dataFetcherResult instanceof LocalDateTime))
        throw new CoercingSerializeException(dataFetcherResult.getClass().getCanonicalName() + "is not a date!");
      LocalDateTime localDateTime = (LocalDateTime) dataFetcherResult;
      return dateTimeFormatter.format(localDateTime);
    }

    @Override
    public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
      String source = input.toString();
      try {
        final TemporalAccessor temporalAccessorParsed = dateTimeFormatter.parse(source);
        return LocalDateTime.from(temporalAccessorParsed);
      } catch (DateTimeParseException dateTimeParseException) {
        throw new CoercingParseValueException(dateTimeParseException);
      }
    }

    @Override
    public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
      return parseValue(input);
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
