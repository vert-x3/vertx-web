package io.vertx.ext.web.handler.graphql;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.ext.web.handler.graphql.TestUtils.peek;
import static io.vertx.ext.web.handler.graphql.TestUtils.sendQueryValidation;

@ExtendWith(VertxExtension.class)
public class ValidationTest extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationTest.class);

  private VertxTestContext vertxTestContext;

  private Vertx vertx = Vertx.vertx();

  @Before
  public void deploy() {
    vertxTestContext = new VertxTestContext();
    vertx.deployVerticle(BorderServer.class.getName(), vertxTestContext.succeedingThenComplete());
  }

  @Test
  public void validString() {
    final ValidationResult result = send(createQuery("text", "valid"));
    final String data = result.json().getString("data.text");
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    Assertions.assertEquals("hello", data);
  }

  @Test
  public void nullString() {
    ValidationResult response = send(createQuery("text", "null"));
    Assertions.assertTrue(response.hasError());
    Assertions.assertFalse(response.hasData());
    LOGGER.debug("Error received: '{}'", response.getErrorReason());
  }

  @Test
  public void eolString() {
    ValidationResult result = send(createQuery("text", "eol"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    final String text = result.json().getString("data.text");
    Assertions.assertEquals(12, text.length());
    Assertions.assertEquals("a\nb\r\nc\0d e\tf", text);
  }


  @Test
  public void emptyString() {
    ValidationResult result = send(createQuery("text", "empty"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    Assertions.assertEquals("", result.json().getString("data.text"));
  }

  @Test
  public void jsonString() {
    ValidationResult result = send(createQuery("text", "brokenjson"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    Assertions.assertEquals("}", result.json().getString("data.text"));
  }

  @Test
  public void i18nString() {
    ValidationResult result = send(createQuery("text", "non-ascii"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    Assertions.assertEquals("今日は přítel, как дела?", result.json().getString("data.text"));
  }

  @Test
  public void longString() {
    ValidationResult result = send(createQuery("text", "long"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.hasError());
    final String string = result.json().getString("data.text");
    Assertions.assertEquals(110_116, string.length());
  }

  @Test
  public void number() {
    ValidationResult result = send(createQuery("number", "positive"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(10, result.json().getInt("data.number"));
  }

  @Test
  public void negativeNumber() {
    ValidationResult result = send(createQuery("number", "negative"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(-10, result.json().getInt("data.number"));
  }

  @Test
  public void maxNumber() {
    ValidationResult result = send(createQuery("number", "max"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(Integer.MAX_VALUE, result.json().getInt("data.number"));
  }

  @Test
  public void minNumber() {
    ValidationResult result = send(createQuery("number", "min"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(Integer.MIN_VALUE, result.json().getInt("data.number"));
  }

  @Test
  public void zero() {
    ValidationResult result = send(createQuery("number", "zero"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(0, result.json().getInt("data.number"));
  }

  @Test
  public void tooBigNumber() {
    ValidationResult result = send(createQuery("number", "huge"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void tooSmallNumber() {
    ValidationResult result = send(createQuery("number", "tiny"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void wayTooBigNumber() {
    ValidationResult result = send(createQuery("number", "overwhelming"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void nullNumber() {
    ValidationResult result = send(createQuery("number", "null"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void notInteger() {
    ValidationResult result = send(createQuery("number", "float"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void notNumber() {
    ValidationResult result = send(createQuery("number", "string"));
    Assertions.assertFalse(result.hasData());
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void floating() {
    ValidationResult result = send(createQuery("floating", "valid"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertEquals(3.14, result.json().getFloat("data.floating"), 0.01);
  }

  @Test
  public void nullFloat() {
    ValidationResult response = send(createQuery("floating", "null"));
    Assertions.assertTrue(response.hasError());
    Assertions.assertFalse(response.hasData());
    LOGGER.debug("Error received: '{}'", response.getErrorReason());
  }

  @Test
  public void boolTrue() {
    ValidationResult result = send(createQuery("bool", "yes"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertTrue(result.json().getBoolean("data.bool"));
  }

  @Test
  public void boolFalse() {
    ValidationResult result = send(createQuery("bool", "no"));
    Assertions.assertTrue(result.hasData());
    Assertions.assertFalse(result.json().getBoolean("data.bool"));
  }

  @Test
  public void boolNull() {
    ValidationResult result = send(createQuery("bool", "null"));
    Assertions.assertTrue(result.hasError());
    Assertions.assertFalse(result.hasData());
    LOGGER.debug("Error received: '{}'", result.getErrorReason());
  }

  @Test
  public void listValid() {
    ValidationResult result = send(createQuery("list", "valid"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.list");
    Assertions.assertEquals(Arrays.asList("one", "two"),
                            list);
  }

  @Test
  public void arrayValid() {
    ValidationResult result = send(createQuery("array", "valid"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.array");
    Assertions.assertEquals(Arrays.asList("one", "two"),
                            list);
  }

  @Test
  public void listJava() {
    ValidationResult result = send(createQuery("list", "object"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.list");
    Assertions.assertEquals(Arrays.asList("one", "two"),
                            list);
  }

  @Test
  public void arrayJava() {
    ValidationResult result = send(createQuery("array", "object"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.array");
    Assertions.assertEquals(Arrays.asList("one", "two"),
                            list);
  }

  @Test
  public void listEmpty() {
    ValidationResult result = send(createQuery("list", "empty"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.list");
    Assertions.assertTrue(list.isEmpty());
  }

  @Test
  public void arrayEmpty() {
    ValidationResult result = send(createQuery("array", "empty"));
    Assertions.assertTrue(result.hasData());
    final List<String> list = result.json().getList("data.array");
    Assertions.assertTrue(list.isEmpty());
  }

  @Test
  public void listNull() {
    ValidationResult result = send(createQuery("list", "null"));
    Assertions.assertTrue(result.hasError());
    Assertions.assertFalse(result.hasData());
    LOGGER.debug("Error received: '{}'", result.getErrorReason());
  }

  @Test
  public void arrayNull() {
    ValidationResult result = send(createQuery("array", "null"));
    Assertions.assertFalse(result.hasError());
    Assertions.assertTrue(result.hasData());
    Assertions.assertNull(result.json().get("data.array"));
  }

  @Test
  public void listWithNulls() {
    ValidationResult result = send(createQuery("list", "nullvalues"));
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
    ValidationResult result = send(createQuery("array", "nullvalues"));
    Assertions.assertTrue(result.hasError());
    LOGGER.debug("Error received: '{}'", result.getErrorReason());
  }

  @Test
  public void listScalar() {
    ValidationResult result = send(createQuery("list", "scalar"));
    Assertions.assertTrue(result.hasError());
    Assertions.assertFalse(result.hasData());
    Assertions.assertNull(result.json().get("data.list"));
  }

  @Test
  public void arrayScalar() {
    ValidationResult result = send(createQuery("array", "scalar"));
    Assertions.assertTrue(result.hasError());
    Assertions.assertNull(result.json().get("data.array"));
  }

  private ValidationResult send(String query) {
    return new ValidationResult(sendQueryValidation(query));
  }

  private String createQuery(String field, String type) {
    final String graphQuery = MessageFormat.format(
      "{0}(type: \"{1}\")",
      field,
      type);
    return peek(String.valueOf(TestUtils.createQuery(graphQuery)));
  }
}

class ValidationResult {
  private final JsonPath source;

  ValidationResult(Response source) {
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
