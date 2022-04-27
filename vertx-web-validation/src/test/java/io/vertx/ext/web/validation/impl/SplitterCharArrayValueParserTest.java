package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parser.SplitterCharArrayParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SplitterCharArrayValueParserTest {

  @Test
  public void testValid() {
    SplitterCharArrayParser parser = new SplitterCharArrayParser(
      ValueParser.BOOLEAN_PARSER, ","
    );

    Object result = parser.parse("true,false");

    assertThat(result)
      .isInstanceOfSatisfying(JsonArray.class, ja ->
        assertThat(ja)
          .containsOnly(true, false)
      );
  }

  @Test
  public void testNull() {
    SplitterCharArrayParser parser = new SplitterCharArrayParser(
      ValueParser.BOOLEAN_PARSER, ","
    );

    Object result = parser.parse("true,,false");

    assertThat(result)
      .isInstanceOfSatisfying(JsonArray.class, ja ->
        assertThat(ja)
          .containsOnly(true, null, false)
      );
  }

  @Test
  public void testEmptyString() {
    SplitterCharArrayParser parser = new SplitterCharArrayParser(
      ValueParser.NOOP_PARSER, ","
    );

    Object result = parser.parse(",,bla");

    assertThat(result)
      .isInstanceOfSatisfying(JsonArray.class, ja ->
        assertThat(ja)
          .containsOnly("", "", "bla")
      );
  }

  @Test
  public void testInvalid() {
    SplitterCharArrayParser parser = new SplitterCharArrayParser(
      ValueParser.BOOLEAN_PARSER, ","
    );

    assertThatExceptionOfType(MalformedValueException.class)
      .isThrownBy(() -> parser.parse("true,hello"));
  }
}
