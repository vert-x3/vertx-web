package io.vertx.ext.web.handler.graphql.it;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.ext.web.handler.graphql.it.TestUtils.peek;
import static io.vertx.ext.web.handler.graphql.it.TestUtils.sendQuery;

@ExtendWith(VertxExtension.class)
public class ValidationTest extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationTest.class);

  @BeforeAll
  public static void deploy(Vertx vertx, VertxTestContext context) {
    vertx.deployVerticle(BorderServer.class.getName(), context.succeedingThenComplete());
  }

  @Test
  public void validString() {
    final Result result = send(createQuery("text", "valid"));
    final String data = result.json().getString("data.text");
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    Assertions.assertEquals("hello", data);
  }

  @Test
  public void nullString() {
    Result response = send(createQuery("text", "null"));
    Assertions.assertTrue(response.hasError());
    Assertions.assertFalse(response.hasData());
    LOGGER.debug("Error received: '{}'", response.getErrorReason());
  }

  @Test
  public void eolString() {
    Result result = send(createQuery("text", "eol"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    final String text = result.json().getString("data.text");
    Assertions.assertEquals(12, text.length());
    Assertions.assertEquals("a\nb\r\nc\0d e\tf", text);
  }


  @Test
  public void emptyString() {
    Result result = send(createQuery("text", "empty"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    Assertions.assertEquals("", result.json().getString("data.text"));
  }

  @Test
  public void jsonString() {
    Result result = send(createQuery("text", "brokenjson"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    Assertions.assertEquals("}", result.json().getString("data.text"));
  }

  @Test
  public void i18nString() {
    Result result = send(createQuery("text", "non-ascii"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    Assertions.assertEquals("今日は přítel, как дела?", result.json().getString("data.text"));
  }

  @Test
  public void longString() {
    Result result = send(createQuery("text", "long"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    final String string = result.json().getString("data.text");
    Assertions.assertEquals(110_116, string.length());
  }

  @Test
  public void number() {
    Result result = send(createQuery("number", "positive"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(10, result.json().getInt("data.number"));
  }

  @Test
  public void negativeNumber() {
    Result result = send(createQuery("number", "negative"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(-10, result.json().getInt("data.number"));
  }

  @Test
  public void maxNumber() {
    Result result = send(createQuery("number", "max"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(Integer.MAX_VALUE, result.json().getInt("data.number"));
  }

  @Test
  public void minNumber() {
    Result result = send(createQuery("number", "min"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(Integer.MIN_VALUE, result.json().getInt("data.number"));
  }

  @Test
  public void zero() {
    Result result = send(createQuery("number", "zero"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(0, result.json().getInt("data.number"));
  }

  @Test
  public void tooBigNumber() {
    Result result = send(createQuery("number", "huge"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void tooSmallNumber() {
    Result result = send(createQuery("number", "tiny"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void wayTooBigNumber() {
    Result result = send(createQuery("number", "overwhelming"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void nullNumber() {
    Result result = send(createQuery("number", "null"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void notInteger() {
    Result result = send(createQuery("number", "float"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void notNumber() {
    Result result = send(createQuery("number", "string"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void floating() {
    Result result = send(createQuery("floating", "valid"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(3.14, result.json().getFloat("data.floating"), 0.01);
  }

  @Test
  public void nullFloat() {
    Result response = send(createQuery("floating", "null"));
    Assertions.assertTrue(response.hasError());
    Assertions.assertFalse(response.hasData());
    LOGGER.debug("Error received: '{}'", response.getErrorReason());
  }

  @Test
  public void boolTrue() {
    Result result = send(createQuery("bool", "yes"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertTrue(result.json().getBoolean("data.bool"));
  }

  @Test
  public void boolFalse() {
    Result result = send(createQuery("bool", "no"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.json().getBoolean("data.bool"));
  }

  @Test
  public void boolNull() {
    Result result = send(createQuery("bool", "null"));
    Assertions.assertTrue(result.hasError());
    Assertions.assertFalse(result.hasData());
    LOGGER.debug("Error received: '{}'", result.getErrorReason());
  }

  @Test
  public void listValid() {
    Result result = send(createQuery("list", "valid"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.list");
    Assertions.assertEquals(Arrays.asList("one", "two"),
                            list);
  }

  @Test
  public void arrayValid() {
    Result result = send(createQuery("array", "valid"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.array");
    Assertions.assertEquals(Arrays.asList("one", "two"),
                            list);
  }

  @Test
  public void listJava() {
    Result result = send(createQuery("list", "object"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.list");
    Assertions.assertEquals(Arrays.asList("one", "two"),
                            list);
  }

  @Test
  public void arrayJava() {
    Result result = send(createQuery("array", "object"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.array");
    Assertions.assertEquals(Arrays.asList("one", "two"),
                            list);
  }

  @Test
  public void listEmpty() {
    Result result = send(createQuery("list", "empty"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.list");
    Assertions.assertTrue(list.isEmpty());
  }

  @Test
  public void arrayEmpty() {
    Result result = send(createQuery("array", "empty"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.array");
    Assertions.assertTrue(list.isEmpty());
  }

  @Test
  public void listNull() {
    Result result = send(createQuery("list", "null"));
    Assertions.assertTrue(result.hasError());
    Assertions.assertFalse(result.hasData());
    LOGGER.debug("Error received: '{}'", result.getErrorReason());
  }

  @Test
  public void arrayNull() {
    Result result = send(createQuery("array", "null"));
    Assertions.assertFalse(result.hasError());
    Assertions.assertTrue(result.hasData());
    Assertions.assertNull(result.json().get("data.array"));
  }

  @Test
  public void listWithNulls() {
    Result result = send(createQuery("list", "nullvalues"));
    Assertions.assertFalse(result.hasError());
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.list");
    Assertions.assertNotNull(list);
    Assertions.assertEquals(2, list.size());
    Assertions.assertNull(list.get(0));
    Assertions.assertNull(list.get(1));
  }

  @Test
  public void arrayWithNulls() {
    Result result = send(createQuery("array", "nullvalues"));
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("Error received: '{}'", result.getErrorReason());
  }

  @Test
  public void listScalar() {
    Result result = send(createQuery("list", "scalar"));
    Assertions.assertTrue(result.hasError());
    Assertions.assertFalse(result.hasData());
    Assertions.assertNull(result.json().get("data.list"));
  }

  @Test
  public void arrayScalar() {
    Result result = send(createQuery("array", "scalar"));
    Assertions.assertTrue(result.hasError());
    Assertions.assertNull(result.json().get("data.array"));
  }

  private Result send(String query) {
    return new Result(sendQuery(query));
  }

  private String createQuery(String field, String type) {
    final String graphQuery = MessageFormat.format(
      "{0}(type: \\\"{1}\\\")",
      field,
      type);
    return peek(TestUtils.createQuery(graphQuery));
  }
}

class Result {
  private final JsonPath source;

  Result(Response source) {
    this.source = source.jsonPath();
  }

  public boolean hasData() {
    final String data = source.getString("data");
    return data != null;
  }

  public boolean hasError() {
    final String errors = source.getString("errors");
    return errors != null;
  }

  public String getErrorReason() {
    return source.getString("errors[0].message");
  }

  @Override
  public String toString() {
    return json().prettyPrint();
  }

  public JsonPath json() {
    return source;
  }
}
