package io.vertx.ext.web.handler.graphql.tests;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static graphql.schema.idl.RuntimeWiring.*;
import static io.vertx.core.http.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationTest extends GraphQLTestBase {

  public static String randomAlphaString(int length) {
    StringBuilder builder = new StringBuilder(length);

    for(int i = 0; i < length; ++i) {
      char c = (char)((int)(65.0 + 25.0 * Math.random()));
      builder.append(c);
    }

    return builder.toString();
  }

  private final static String VERY_LONG = randomAlphaString(110_116);

  @Override
  protected GraphQL graphQL() {
    String schema = vertx.fileSystem().readFileBlocking("borders.graphqls").toString();
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder
        .dataFetcher("text", new TextFetcher())
        .dataFetcher("number", new IntegerFetcher())
        .dataFetcher("floating", new FloatFetcher())
        .dataFetcher("bool", new BoolFetcher())
        .dataFetcher("list", new ListFetcher())
        .dataFetcher("array", new ListFetcher()))
      .build();


    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema)
      .build();
  }


  private String getType(DataFetchingEnvironment environment) {
    return environment.getArgument("type");
  }

  class InvalidTypeException extends IllegalArgumentException {
    public InvalidTypeException(DataFetchingEnvironment env) {
      super("Unexpected value: " + getType(env));
    }
  }

  private class TextFetcher implements DataFetcher<String> {

    @Override
    public String get(DataFetchingEnvironment environment) throws Exception {
      switch (getType(environment)) {
        case "valid":
          return "hello";
        case "null":
          return null;
        case "eol":
          return "a\nb\r\nc\0d e\tf";
        case "non-ascii":
          return "今日は přítel, как дела?";
        case "empty":
          return "";
        case "brokenjson":
          return "}";
        case "long":
          return VERY_LONG;
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }

  private class IntegerFetcher implements DataFetcher<Object> {
    @Override
    public Object get(DataFetchingEnvironment environment) {
      switch (getType(environment)) {
        case "positive":
          return 10;
        case "negative":
          return -10;
        case "max":
          return Integer.MAX_VALUE;
        case "min":
          return Integer.MIN_VALUE;
        case "huge":
          return 1L + Integer.MAX_VALUE;
        case "tiny":
          return -1L + Integer.MIN_VALUE;
        case "zero":
          return 0;
        case "overwhelming":
          return BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);
        case "float":
          return 3.14f;
        case "string":
          return "hi";
        case "null":
          return null;
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }

  private class FloatFetcher implements DataFetcher<Float> {
    @Override
    public Float get(DataFetchingEnvironment environment) {
      switch (getType(environment)) {
        case "valid":
          return 3.14f;
        case "null":
          return null;
        case "nan":
          return Float.NaN;
        case "infinity":
          return Float.POSITIVE_INFINITY;
        case "infinity_neg":
          return Float.NEGATIVE_INFINITY;
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }

  private class BoolFetcher implements DataFetcher<Boolean> {
    @Override
    public Boolean get(DataFetchingEnvironment environment) {
      switch (getType(environment)) {
        case "yes":
          return true;
        case "null":
          return null;
        case "no":
          return false;
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }

  private class ListFetcher implements DataFetcher<Object> {
    @Override
    public Object get(DataFetchingEnvironment environment) {
      switch (getType(environment)) {
        case "valid":
          return new String[]{"one", "two"};
        case "object":
          return Arrays.asList("one", "two");
        case "empty":
          return new String[]{};
        case "null":
          return null;
        case "nullvalues":
          return new String[]{null, null};
        case "scalar":
          return "three";
        default:
          throw new InvalidTypeException(environment);
      }
    }
  }

  @Test
  public void validString() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"valid\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals("hello", result.data().getString("text"));
  }

  @Test
  public void nullString() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"null\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasError());
    assertFalse(result.hasData());
  }

  @Test
  public void eolString() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"eol\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    String text = result.data().getString("text");
    assertEquals(12, text.length());
    assertEquals("a\nb\r\nc\0d e\tf", text);
  }

  @Test
  public void emptyString() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"empty\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertFalse(result.hasError());
    assertTrue(result.hasData());
    assertTrue(result.data().getString("text").isEmpty());
  }

  @Test
  public void jsonString() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"brokenjson\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals("}", result.data().getString("text"));
  }

  @Test
  public void i18nString() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"non-ascii\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals("今日は přítel, как дела?", result.data().getString("text"));
  }

  @Test
  public void longString() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"long\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals(VERY_LONG, result.data().getString("text"));
  }

  @Test
  public void number() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"positive\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals(Integer.valueOf(10), result.data().getInteger("number"));
  }

  @Test
  public void negativeNumber() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"negative\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals(Integer.valueOf(-10), result.data().getInteger("number"));
  }

  @Test
  public void maxNumber() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"max\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals(Integer.valueOf(Integer.MAX_VALUE), result.data().getInteger("number"));
  }

  @Test
  public void minNumber() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"min\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals(Integer.valueOf(Integer.MIN_VALUE), result.data().getInteger("number"));
  }

  @Test
  public void zero() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"zero\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals(Integer.valueOf(0), result.data().getInteger("number"));
  }

  @Test
  public void tooBigNumber() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"huge\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertFalse(result.hasData());
    assertTrue(result.hasError());
  }

  @Test
  public void tooSmallNumber() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"tiny\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertFalse(result.hasData());
    assertTrue(result.hasError());
  }

  @Test
  public void wayTooBigNumber() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"overwhelming\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertFalse(result.hasData());
    assertTrue(result.hasError());
  }

  @Test
  public void nullNumber() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"null\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertFalse(result.hasData());
    assertTrue(result.hasError());
  }

  @Test
  public void notInteger() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"float\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertFalse(result.hasData());
    assertTrue(result.hasError());
  }

  @Test
  public void notNumber() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"string\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertFalse(result.hasData());
    assertTrue(result.hasError());
  }

  @Test
  public void floating() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { floating(type: \"valid\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertEquals(Float.valueOf(3.14F), result.data().getFloat("floating"));
  }

  @Test
  public void nullFloat() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { floating(type: \"null\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertFalse(result.hasData());
    assertTrue(result.hasError());
  }

  @Test
  public void boolTrue() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { bool(type: \"yes\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertTrue(result.data().getBoolean("bool"));
  }

  @Test
  public void boolFalse() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { bool(type: \"no\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertFalse(result.data().getBoolean("bool"));
  }

  @Test
  public void boolNull() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { bool(type: \"null\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertFalse(result.hasData());
    assertTrue(result.hasError());
  }

  @Test
  public void listValid() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"valid\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    JsonArray list = result.data().getJsonArray("list");
    assertEquals(JsonArray.of("one", "two"), list);
  }

  @Test
  public void arrayValid() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"valid\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    JsonArray list = result.data().getJsonArray("array");
    assertEquals(JsonArray.of("one", "two"), list);
  }

  @Test
  public void listJava() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"object\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    JsonArray list = result.data().getJsonArray("list");
    assertEquals(JsonArray.of("one", "two"), list);
  }

  @Test
  public void arrayJava() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"object\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    JsonArray list = result.data().getJsonArray("array");
    assertEquals(JsonArray.of("one", "two"), list);
  }

  @Test
  public void listEmpty() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"empty\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    JsonArray list = result.data().getJsonArray("list");
    assertTrue(list.isEmpty());
  }

  @Test
  public void arrayEmpty() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"empty\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    JsonArray list = result.data().getJsonArray("array");
    assertTrue(list.isEmpty());
  }

  @Test
  public void listNull() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"null\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasError());
    assertFalse(result.hasData());
  }

  @Test
  public void arrayNull() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"null\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    assertNull(result.data().getValue("array"));
  }

  @Test
  public void listWithNulls() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"nullvalues\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasData());
    assertFalse(result.hasError());
    JsonArray list = result.data().getJsonArray("list");
    assertNotNull(list);
    assertEquals(2, list.size());
    assertNull(list.getValue(0));
    assertNull(list.getValue(1));
  }

  @Test
  public void arrayWithNulls() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"nullvalues\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasError());
  }

  @Test
  public void listScalar() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"scalar\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasError());
    assertFalse(result.hasData());
  }

  @Test
  public void arrayScalar() {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"scalar\") }");
    JsonObject body = request.send(webClient);
    ValidationResult result = new ValidationResult(body);
    assertTrue(result.hasError());
    assertNull(result.data().getValue("array"));
  }

  private static class ValidationResult {
    final JsonObject source;

    ValidationResult(JsonObject source) {
      this.source = source;
    }

    boolean hasData() {
      return source.getValue("data") != null;
    }

    JsonObject data() {
      return source.getJsonObject("data");
    }

    boolean hasError() {
      return source.containsKey("errors");
    }

    @Override
    public String toString() {
      return source.encodePrettily();
    }
  }
}
