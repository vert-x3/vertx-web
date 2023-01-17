package io.vertx.ext.web.handler.graphql;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.ext.web.handler.graphql.TestUtils.peek;
import static io.vertx.ext.web.handler.graphql.TestUtils.sendQueryValidation;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertNull;

public class ValidationTest extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationTest.class);


  private Vertx vertx = Vertx.vertx();

  @Before
  public void deploy() {
    vertx.deployVerticle(BorderServer.class.getName());
  }

  @Test
  public void validString() {
    final ValidationResult result = send(createQuery("text", "valid"));
    final String data = result.json().getString("data.text");
    assertThat(result.hasData(), is(true));
    assertThat(result.hasError(), is(false));
    assertThat(data, equalTo("hello"));
  }

  @Test
  public void nullString() {
    ValidationResult response = send(createQuery("text", "null"));
    assertThat(response.hasError(), is(true));
    assertThat(response.hasData(), is(false));
    LOGGER.debug("Error received: '{}'", response.getErrorReason());
  }

  @Test
  public void eolString() {
    ValidationResult result = send(createQuery("text", "eol"));
    assertThat(result.hasData(), is(true));
    assertThat(result.hasError(), is(false));
    final String text = result.json().getString("data.text");
    assertThat(text.length(), equalTo(12));
    assertThat(text, equalTo("a\nb\r\nc\0d e\tf"));
  }


  @Test
  public void emptyString() {
    ValidationResult result = send(createQuery("text", "empty"));
    assertThat(result.hasError(), is(false));
    assertThat(result.hasData(), is(true));
    assertThat(result.json().getString("data.text"), equalTo(""));
  }

  @Test
  public void jsonString() {
    ValidationResult result = send(createQuery("text", "brokenjson"));
    assertThat(result.hasData(), is(true));
    assertThat(result.hasError(), is(false));
    assertThat(result.json().getString("data.text"), equalTo("}"));
  }

  @Test
  public void i18nString() {
    ValidationResult result = send(createQuery("text", "non-ascii"));
    assertThat(result.hasData(), is(true));
    assertThat(result.hasError(), is(false));
    assertThat(result.json().getString("data.text"), equalTo("今日は přítel, как дела?"));
  }

  @Test
  public void longString() {
    ValidationResult result = send(createQuery("text", "long"));
    assertThat(result.hasData(), is(true));
    assertThat(result.hasError(), is(false));
    final String string = result.json().getString("data.text");
    assertThat(string.length(), equalTo(110_116));
  }

  @Test
  public void number() {
    ValidationResult result = send(createQuery("number", "positive"));
    assertThat(result.hasData(), is(true));
    assertThat(result.json().getInt("data.number"), equalTo(10));
  }

  @Test
  public void negativeNumber() {
    ValidationResult result = send(createQuery("number", "negative"));
    assertThat(result.hasData(), is(true));
    assertThat(result.json().getInt("data.number"), equalTo(-10));
  }

  @Test
  public void maxNumber() {
    ValidationResult result = send(createQuery("number", "max"));
    assertThat(result.hasData(), is(true));
    assertThat(result.json().getInt("data.number"), equalTo(Integer.MAX_VALUE));
  }

  @Test
  public void minNumber() {
    ValidationResult result = send(createQuery("number", "min"));
    assertThat(result.hasData(), is(true));
    assertThat(result.json().getInt("data.number"), equalTo(Integer.MIN_VALUE));
  }

  @Test
  public void zero() {
    ValidationResult result = send(createQuery("number", "zero"));
    assertThat(result.hasData(), is(true));
    assertThat(result.json().getInt("data.number"), equalTo(0));
  }

  @Test
  public void tooBigNumber() {
    ValidationResult result = send(createQuery("number", "huge"));
    assertThat(result.hasData(), is(false));
    assertThat(result.hasError(), is(true));
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void tooSmallNumber() {
    ValidationResult result = send(createQuery("number", "tiny"));
    assertThat(result.hasData(), is(false));
    assertThat(result.hasError(), is(true));
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void wayTooBigNumber() {
    ValidationResult result = send(createQuery("number", "overwhelming"));
    assertThat(result.hasData(), is(false));
    assertThat(result.hasError(), is(true));
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void nullNumber() {
    ValidationResult result = send(createQuery("number", "null"));
    assertThat(result.hasData(), is(false));
    assertThat(result.hasError(), is(true));
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void notInteger() {
    ValidationResult result = send(createQuery("number", "float"));
    assertThat(result.hasData(), is(false));
    assertThat(result.hasError(), is(true));
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void notNumber() {
    ValidationResult result = send(createQuery("number", "string"));
    assertThat(result.hasData(), is(false));
    assertThat(result.hasError(), is(true));
    LOGGER.debug("result:'{}'", result.getErrorReason());
  }

  @Test
  public void floating() {
    ValidationResult result = send(createQuery("floating", "valid"));
    assertThat(result.hasData(), is(true));
    assertThat((double) result.json().getFloat("data.floating"), closeTo(3.14, 0.01));
  }

  @Test
  public void nullFloat() {
    ValidationResult response = send(createQuery("floating", "null"));
    assertThat(response.hasData(), is(false));
    assertThat(response.hasError(), is(true));
    LOGGER.debug("Error received: '{}'", response.getErrorReason());
  }

  @Test
  public void boolTrue() {
    ValidationResult result = send(createQuery("bool", "yes"));
    assertThat(result.hasData(), is(true));
    assertThat(result.json().getBoolean("data.bool"), is(true));
  }

  @Test
  public void boolFalse() {
    ValidationResult result = send(createQuery("bool", "no"));
    assertThat(result.hasData(), is(true));
    assertThat(result.json().getBoolean("data.bool"), is(false));
  }

  @Test
  public void boolNull() {
    ValidationResult result = send(createQuery("bool", "null"));
    assertThat(result.hasData(), is(false));
    assertThat(result.hasError(), is(true));
    LOGGER.debug("Error received: '{}'", result.getErrorReason());
  }

  @Test
  public void listValid() {
    ValidationResult result = send(createQuery("list", "valid"));
    assertThat(result.hasData(), is(true));
    final List<String> list = result.json().getList("data.list");
    assertThat(list, equalTo(Arrays.asList("one", "two")));
  }

  @Test
  public void arrayValid() {
    ValidationResult result = send(createQuery("array", "valid"));
    assertThat(result.hasData(), is(true));
    final List<String> list = result.json().getList("data.array");
    assertThat(list, equalTo(Arrays.asList("one", "two")));
  }

  @Test
  public void listJava() {
    ValidationResult result = send(createQuery("list", "object"));
    assertThat(result.hasData(), is(true));
    final List<String> list = result.json().getList("data.list");
    assertThat(list, equalTo(Arrays.asList("one", "two")));
  }

  @Test
  public void arrayJava() {
    ValidationResult result = send(createQuery("array", "object"));
    assertThat(result.hasData(), is(true));
    final List<String> list = result.json().getList("data.array");
    assertThat(list, equalTo(Arrays.asList("one", "two")));
  }

  @Test
  public void listEmpty() {
    ValidationResult result = send(createQuery("list", "empty"));
    assertThat(result.hasData(), is(true));
    final List<String> list = result.json().getList("data.list");
    assertThat(list.isEmpty(), is(true));
  }

  @Test
  public void arrayEmpty() {
    ValidationResult result = send(createQuery("array", "empty"));
    assertThat(result.hasData(), is(true));
    final List<String> list = result.json().getList("data.array");
    assertThat(list.isEmpty(), is(true));
  }

  @Test
  public void listNull() {
    ValidationResult result = send(createQuery("list", "null"));
    assertThat(result.hasError(), is(true));
    assertThat(result.hasData(), is(false));
    LOGGER.debug("Error received: '{}'", result.getErrorReason());
  }

  @Test
  public void arrayNull() {
    ValidationResult result = send(createQuery("array", "null"));
    assertThat(result.hasData(), is(true));
    assertThat(result.hasError(), is(false));
    assertNull(result.json().get("data.array"));
  }

  @Test
  public void listWithNulls() {
    ValidationResult result = send(createQuery("list", "nullvalues"));
    assertThat(result.hasData(), is(true));
    assertThat(result.hasError(), is(false));
    final List<String> list = result.json().getList("data.list");
    assertThat(list, notNullValue());
    assertThat(list.size(), equalTo(2));
    assertNull(list.get(0));
    assertNull(list.get(1));
  }

  @Test
  public void arrayWithNulls() {
    ValidationResult result = send(createQuery("array", "nullvalues"));
    assertThat(result.hasError(), is(true));
    LOGGER.debug("Error received: '{}'", result.getErrorReason());
  }

  @Test
  public void listScalar() {
    ValidationResult result = send(createQuery("list", "scalar"));
    assertThat(result.hasError(), is(true));
    assertThat(result.hasData(), is(false));
    assertNull(result.json().get("data.list"));
  }

  @Test
  public void arrayScalar() {
    ValidationResult result = send(createQuery("array", "scalar"));
    assertThat(result.hasError(), is(true));
    assertNull(result.json().get("data.array"));
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
