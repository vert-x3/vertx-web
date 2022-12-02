package io.vertx.ext.web.handler.graphql.it;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.ext.web.handler.graphql.it.TestUtils.createQuery;
import static io.vertx.ext.web.handler.graphql.it.TestUtils.peek;
import static io.vertx.ext.web.handler.graphql.it.TestUtils.sendQuery;

@ExtendWith(VertxExtension.class)
public class BasicIT {

  private static Logger LOGGER = LoggerFactory.getLogger(BasicIT.class);

  @BeforeAll
  public static void deploy(Vertx vertx, VertxTestContext context) {
    vertx.deployVerticle(HelloGraphQLServer.class.getName(), context.succeedingThenComplete());
  }

  @Test
  public void helloWorld() {
    String result = "{\"data\":{\"hello\":\"Hello World!\"}}";
    Response response = sendQuery("{\"query\":\"{hello}\"}");
    LOGGER.debug("{}", response.asString());
    Assertions.assertEquals(result, response.getBody().asString());
  }

  @Test
  public void integerNumber() {
    Response response = sendQuery(createQuery("number"));
    final int data = response.jsonPath().getInt("data.number");
    Assertions.assertEquals(130, data);
  }

  @Test
  public void floatingPointNumber() {
    Response response = sendQuery(createQuery("floating"));
    final float data = response.jsonPath().getFloat("data.floating");
    Assertions.assertEquals(3.14, data, 0.01);
  }

  @Test
  public void bool() {
    Response response = sendQuery(createQuery("bool"));
    final boolean data = response.jsonPath().getBoolean("data.bool");
    Assertions.assertTrue(data);
  }

  @Test
  public void id() {
    Response response = sendQuery(createQuery("id"));
    final String data = response.jsonPath().getString("data.id");
    Assertions.assertEquals("1001", data);
  }

  @Test
  public void enumeration() {
    Response response = sendQuery(createQuery("enum"));
    final String data = response.jsonPath().getString("data.enum");
    Assertions.assertEquals(Musketeer.ATHOS.toString(), data);
  }

  @Test
  public void list() {
    Response response = sendQuery(createQuery("list"));
    final List<String> data = response.jsonPath().getList("data.list");
    Assertions.assertEquals(3, data.size());
    Collections.sort(data);
    Assertions.assertEquals(Arrays.asList("apples", "carrots", "eggs"), data);
  }

  @Test
  public void alias() {
    Response response = sendQuery(createQuery("arr: array"));
    final List<String> data = response.jsonPath().getList("data.arr");
    Assertions.assertEquals(3, data.size());
    Collections.sort(data);
    Assertions.assertEquals(Arrays.asList("apples", "carrots", "eggs"), data);
  }

  @Test
  public void userDefined() {
    Response response = sendQuery(createQuery("when"));
    final String data = response.jsonPath().getString("data.when");
    final LocalDateTime localDateTime = new DatetimeCoercion().parseValue(data);
    final LocalDateTime linuxAnnouncement = LocalDateTime.of(LocalDate.of(1991, 8, 25),
                                                             LocalTime.of(22, 57, 8));
    Assertions.assertEquals(linuxAnnouncement, localDateTime);
  }

  @Test
  public void functionDefault() {
    Response response = sendQuery(createQuery("answer"));
    final String data = response.jsonPath().getString("data.answer");
    Assertions.assertEquals("Hello, someone!", data);
  }

  @Test
  public void function() {
    Response response = sendQuery(peek(createQuery("answer(name:\\\"world\\\")")));
    LOGGER.debug("{}", response.asString());
    final String data = response.jsonPath().getString("data.answer");
    Assertions.assertEquals("Hello, world!", data);
  }

  @Test
  public void cached() {
    final String query = createQuery("changing");
    final String first = sendQuery(query).jsonPath().getString("data.changing");
    final String second = sendQuery(query).jsonPath().getString("data.changing");
    LOGGER.debug("first is '{}' and second is '{}'", first, second);
    Assertions.assertNotEquals(first, second);
  }

  @Test
  public void recursive() {
    final String query = peek(createQuery("persons{name,friend{name,friend{name}}}"));
    final Response response = sendQuery(query);
    final JsonPath json = response.jsonPath();
    Assertions.assertEquals("Plato", json.getString("data.persons[0].name"));
    Assertions.assertEquals("Aristotle", json.getString("data.persons[0].friend.name"));
    Assertions.assertEquals("Plato", json.getString("data.persons[0].friend.friend.name"));
  }
}
