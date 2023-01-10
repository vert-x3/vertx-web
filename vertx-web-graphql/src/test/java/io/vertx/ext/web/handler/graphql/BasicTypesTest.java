package io.vertx.ext.web.handler.graphql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.vertx.core.http.HttpServerOptions;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static io.vertx.ext.web.handler.graphql.TestUtils.createQuery;
import static io.vertx.ext.web.handler.graphql.TestUtils.peek;
import static io.vertx.ext.web.handler.graphql.TestUtils.sendQueryBasicTypes;

public class BasicTypesTest extends GraphQLTestBase {

  private static Logger LOGGER = LoggerFactory.getLogger(BasicTypesTest.class);

  private final Counter counter = new Counter();

  private final Person[] philosophers;

  public BasicTypesTest() {
    final Person plato = new Person("Plato");
    final Person aristotle = new Person("Aristotle");
    plato.setFriend(aristotle);
    aristotle.setFriend(plato);
    philosophers = new Person[]{plato, aristotle};
  }

  @Override
  protected HttpServerOptions getHttpServerOptions() {
    return new HttpServerOptions().setPort(8082).setHost("localhost");
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
      .type("Query", builder -> {
        final HashMap<String, DataFetcher> fetchersMap = new HashMap<>();
        fetchersMap.put("floating", env -> 3.14f);
        fetchersMap.put("bool", env -> true);
        fetchersMap.put("id", env -> "1001");
        fetchersMap.put("enum", env -> Musketeer.ATHOS);
        fetchersMap.put("when", env -> LocalDateTime.of(1991, 8, 25, 22, 57, 8));
        fetchersMap.put("answer", env -> "Hello, " + env.getArgument("name") + "!");
        fetchersMap.put("array", env -> new String[]{"apples", "eggs", "carrots"});
        fetchersMap.put("list", env -> Arrays.asList("apples", "eggs", "carrots"));
        return builder.dataFetcher("hello", env -> "Hello World!")
          .dataFetcher("number", env -> 130)
          .dataFetcher("changing", counter)
          .dataFetcher("persons", env -> philosophers)
          .dataFetchers(fetchersMap);
      })
      .build();


    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }

  @Test
  public void helloWorld() {
    String result = "{\"data\":{\"hello\":\"Hello World!\"}}";
    Response response = sendQueryBasicTypes("{\"query\":\"{hello}\"}");
    LOGGER.debug("{}", response.asString());
    Assertions.assertEquals(result, response.getBody().asString());
  }

  @Test
  public void integerNumber() {
    Response response = sendQueryBasicTypes(createQuery("number").toString());
    final int data = response.jsonPath().getInt("data.number");
    Assertions.assertEquals(130, data);
  }

  @Test
  public void floatingPointNumber() {
    Response response = sendQueryBasicTypes(createQuery("floating").toString());
    final float data = response.jsonPath().getFloat("data.floating");
    Assertions.assertEquals(3.14, data, 0.01);
  }

  @Test
  public void bool() {
    Response response = sendQueryBasicTypes(createQuery("bool").toString());
    final boolean data = response.jsonPath().getBoolean("data.bool");
    Assertions.assertTrue(data);
  }

  @Test
  public void id() {
    Response response = sendQueryBasicTypes(createQuery("id").toString());
    final String data = response.jsonPath().getString("data.id");
    Assertions.assertEquals("1001", data);
  }

  @Test
  public void enumeration() {
    Response response = sendQueryBasicTypes(createQuery("enum").toString());
    final String data = response.jsonPath().getString("data.enum");
    Assertions.assertEquals(Musketeer.ATHOS.toString(), data);
  }

  @Test
  public void list() {
    Response response = sendQueryBasicTypes(createQuery("list").toString());
    final List<String> data = response.jsonPath().getList("data.list");
    Assertions.assertEquals(3, data.size());
    Collections.sort(data);
    Assertions.assertEquals(Arrays.asList("apples", "carrots", "eggs"), data);
  }

  @Test
  public void alias() {
    Response response = sendQueryBasicTypes(createQuery("arr: array").toString());
    final List<String> data = response.jsonPath().getList("data.arr");
    Assertions.assertEquals(3, data.size());
    Collections.sort(data);
    Assertions.assertEquals(Arrays.asList("apples", "carrots", "eggs"), data);
  }

  @Test
  public void userDefined() {
    Response response = sendQueryBasicTypes(createQuery("when").toString());
    final String data = response.jsonPath().getString("data.when");
    final LocalDateTime localDateTime = new DatetimeCoercion().parseValue(data);
    final LocalDateTime linuxAnnouncement = LocalDateTime.of(LocalDate.of(1991, 8, 25),
                                                             LocalTime.of(22, 57, 8));
    Assertions.assertEquals(linuxAnnouncement, localDateTime);
  }

  @Test
  public void functionDefault() {
    Response response = sendQueryBasicTypes(String.valueOf(createQuery("answer")));
    final String data = response.jsonPath().getString("data.answer");
    Assertions.assertEquals("Hello, someone!", data);
  }

  @Test
  public void function() {
    Response response = sendQueryBasicTypes(peek(String.valueOf(createQuery("answer(name:\"world\")"))));
    LOGGER.debug("{}", response.asString());
    final String data = response.jsonPath().getString("data.answer");
    Assertions.assertEquals("Hello, world!", data);
  }

  @Test
  public void cached() {
    final String query = createQuery("changing").toString();
    final String first = sendQueryBasicTypes(query).jsonPath().getString("data.changing");
    final String second = sendQueryBasicTypes(query).jsonPath().getString("data.changing");
    LOGGER.debug("first is '{}' and second is '{}'", first, second);
    Assertions.assertNotEquals(first, second);
  }

  @Test
  public void recursive() {
    final String query = peek(createQuery("persons{name,friend{name,friend{name}}}").toString());
    final Response response = sendQueryBasicTypes(query);
    final JsonPath json = response.jsonPath();
    Assertions.assertEquals("Plato", json.getString("data.persons[0].name"));
    Assertions.assertEquals("Aristotle", json.getString("data.persons[0].friend.name"));
    Assertions.assertEquals("Plato", json.getString("data.persons[0].friend.friend.name"));
  }

  class Counter implements DataFetcher<Integer> {
    private final AtomicInteger order = new AtomicInteger(0);

    @Override
    public Integer get(DataFetchingEnvironment environment) {
      return order.getAndIncrement();
    }
  }
}
