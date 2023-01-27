package io.vertx.ext.web.handler.graphql;

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
import io.vertx.test.core.TestUtils;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static graphql.schema.idl.RuntimeWiring.*;
import static io.vertx.core.http.HttpMethod.*;

public class ValidationTest extends GraphQLTestBase {

  private final static String VERY_LONG = TestUtils.randomAlphaString(110_116);

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
  public void validString() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"valid\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals("hello", result.data().getString("text"));
      testComplete();
    }));
    await();
  }

  @Test
  public void nullString() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"null\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasError());
      assertFalse(result.hasData());
      testComplete();
    }));
    await();
  }

  @Test
  public void eolString() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"eol\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      String text = result.data().getString("text");
      assertEquals(12, text.length());
      assertEquals("a\nb\r\nc\0d e\tf", text);
      testComplete();
    }));
    await();
  }

  @Test
  public void emptyString() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"empty\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertFalse(result.hasError());
      assertTrue(result.hasData());
      assertTrue(result.data().getString("text").isEmpty());
      testComplete();
    }));
    await();
  }

  @Test
  public void jsonString() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"brokenjson\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals("}", result.data().getString("text"));
      testComplete();
    }));
    await();
  }

  @Test
  public void i18nString() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"non-ascii\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals("今日は přítel, как дела?", result.data().getString("text"));
      testComplete();
    }));
    await();
  }

  @Test
  public void longString() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { text(type: \"long\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals(VERY_LONG, result.data().getString("text"));
      testComplete();
    }));
    await();
  }

  @Test
  public void number() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"positive\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals(Integer.valueOf(10), result.data().getInteger("number"));
      testComplete();
    }));
    await();
  }

  @Test
  public void negativeNumber() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"negative\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals(Integer.valueOf(-10), result.data().getInteger("number"));
      testComplete();
    }));
    await();
  }

  @Test
  public void maxNumber() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"max\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals(Integer.valueOf(Integer.MAX_VALUE), result.data().getInteger("number"));
      testComplete();
    }));
    await();
  }

  @Test
  public void minNumber() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"min\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals(Integer.valueOf(Integer.MIN_VALUE), result.data().getInteger("number"));
      testComplete();
    }));
    await();
  }

  @Test
  public void zero() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"zero\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals(Integer.valueOf(0), result.data().getInteger("number"));
      testComplete();
    }));
    await();
  }

  @Test
  public void tooBigNumber() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"huge\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertFalse(result.hasData());
      assertTrue(result.hasError());
      testComplete();
    }));
    await();
  }

  @Test
  public void tooSmallNumber() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"tiny\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertFalse(result.hasData());
      assertTrue(result.hasError());
      testComplete();
    }));
    await();
  }

  @Test
  public void wayTooBigNumber() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"overwhelming\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertFalse(result.hasData());
      assertTrue(result.hasError());
      testComplete();
    }));
    await();
  }

  @Test
  public void nullNumber() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"null\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertFalse(result.hasData());
      assertTrue(result.hasError());
      testComplete();
    }));
    await();
  }

  @Test
  public void notInteger() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"float\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertFalse(result.hasData());
      assertTrue(result.hasError());
      testComplete();
    }));
    await();
  }

  @Test
  public void notNumber() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { number(type: \"string\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertFalse(result.hasData());
      assertTrue(result.hasError());
      testComplete();
    }));
    await();
  }

  @Test
  public void floating() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { floating(type: \"valid\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertEquals(Float.valueOf(3.14F), result.data().getFloat("floating"));
      testComplete();
    }));
    await();
  }

  @Test
  public void nullFloat() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { floating(type: \"null\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertFalse(result.hasData());
      assertTrue(result.hasError());
      testComplete();
    }));
    await();
  }

  @Test
  public void boolTrue() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { bool(type: \"yes\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertTrue(result.data().getBoolean("bool"));
      testComplete();
    }));
    await();
  }

  @Test
  public void boolFalse() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { bool(type: \"no\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertFalse(result.data().getBoolean("bool"));
      testComplete();
    }));
    await();
  }

  @Test
  public void boolNull() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { bool(type: \"null\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertFalse(result.hasData());
      assertTrue(result.hasError());
      testComplete();
    }));
    await();
  }

  @Test
  public void listValid() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"valid\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      JsonArray list = result.data().getJsonArray("list");
      assertEquals(JsonArray.of("one", "two"), list);
      testComplete();
    }));
    await();
  }

  @Test
  public void arrayValid() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"valid\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      JsonArray list = result.data().getJsonArray("array");
      assertEquals(JsonArray.of("one", "two"), list);
      testComplete();
    }));
    await();
  }

  @Test
  public void listJava() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"object\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      JsonArray list = result.data().getJsonArray("list");
      assertEquals(JsonArray.of("one", "two"), list);
      testComplete();
    }));
    await();
  }

  @Test
  public void arrayJava() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"object\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      JsonArray list = result.data().getJsonArray("array");
      assertEquals(JsonArray.of("one", "two"), list);
      testComplete();
    }));
    await();
  }

  @Test
  public void listEmpty() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"empty\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      JsonArray list = result.data().getJsonArray("list");
      assertTrue(list.isEmpty());
      testComplete();
    }));
    await();
  }

  @Test
  public void arrayEmpty() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"empty\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      JsonArray list = result.data().getJsonArray("array");
      assertTrue(list.isEmpty());
      testComplete();
    }));
    await();
  }

  @Test
  public void listNull() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"null\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasError());
      assertFalse(result.hasData());
      testComplete();
    }));
    await();
  }

  @Test
  public void arrayNull() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"null\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      assertNull(result.data().getValue("array"));
      testComplete();
    }));
    await();
  }

  @Test
  public void listWithNulls() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"nullvalues\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasData());
      assertFalse(result.hasError());
      JsonArray list = result.data().getJsonArray("list");
      assertNotNull(list);
      assertEquals(2, list.size());
      assertNull(list.getValue(0));
      assertNull(list.getValue(1));
      testComplete();
    }));
    await();
  }

  @Test
  public void arrayWithNulls() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"nullvalues\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasError());
      testComplete();
    }));
    await();
  }

  @Test
  public void listScalar() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { list(type: \"scalar\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasError());
      assertFalse(result.hasData());
      testComplete();
    }));
    await();
  }

  @Test
  public void arrayScalar() throws Exception {
    GraphQLRequest request = new GraphQLRequest()
      .setMethod(POST)
      .setGraphQLQuery("query { array(type: \"scalar\") }");
    request.send(client, onSuccess(body -> {
      ValidationResult result = new ValidationResult(body);
      assertTrue(result.hasError());
      assertNull(result.data().getValue("array"));
      testComplete();
    }));
    await();
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
