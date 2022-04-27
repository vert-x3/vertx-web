package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parameter.ExplodedObjectValueParameterParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import io.vertx.ext.web.validation.testutils.TestParsers;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ExplodedObjectValueParameterParserTest {

  @Test
  public void testValid() {
    ExplodedObjectValueParameterParser parser = new ExplodedObjectValueParameterParser(
      "bla", TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("prop1", singletonList("1"));
    map.put("prop2", singletonList("2.1"));
    map.put("prop3", singletonList("aaa"));
    map.put("prop4", singletonList("true"));
    map.put("other", singletonList("hello"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().put("other", "hello"))
      );

    assertThat(map)
      .isEmpty();
  }

  @Test
  public void testNoAdditionalProperties() {
    ExplodedObjectValueParameterParser parser = new ExplodedObjectValueParameterParser(
      "bla", TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      null
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("prop1", singletonList("1"));
    map.put("prop2", singletonList("2.1"));
    map.put("prop3", singletonList("aaa"));
    map.put("prop4", singletonList("true"));
    map.put("other", singletonList("hello"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT)
      );

    assertThat(map)
      .containsKey("other");
  }

  @Test
  public void testNull() {
    ExplodedObjectValueParameterParser parser = new ExplodedObjectValueParameterParser(
      "bla", TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("prop1", singletonList("1"));
    map.put("prop2", singletonList(""));
    map.put("prop3", singletonList(null));
    map.put("prop4", singletonList("true"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().putNull("prop2").putNull("prop3"))
      );

    assertThat(map)
      .isEmpty();
  }

  @Test
  public void testEmptyString() {
    ExplodedObjectValueParameterParser parser = new ExplodedObjectValueParameterParser(
      "bla", TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("prop1", singletonList("1"));
    map.put("prop2", singletonList(""));
    map.put("prop3", singletonList(""));
    map.put("prop4", singletonList("true"));
    map.put("other", singletonList("hello"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().putNull("prop2").put("prop3", "").put("other", "hello"))
      );

    assertThat(map)
      .isEmpty();
  }

  @Test
  public void testMissingProp() {
    ExplodedObjectValueParameterParser parser = new ExplodedObjectValueParameterParser(
      "bla", TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("prop1", singletonList("1"));
    map.put("prop3", singletonList("aaa"));
    map.put("other", singletonList("hello"));

    Object result = parser.parseParameter(map);

    JsonObject expected = TestParsers.SAMPLE_OBJECT.copy();
    expected.remove("prop2");
    expected.remove("prop4");
    expected.put("other", "hello");

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(expected)
      );

    assertThat(map)
      .isEmpty();
  }

  @Test
  public void testInvalid() {
    ExplodedObjectValueParameterParser parser = new ExplodedObjectValueParameterParser(
      "bla", TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("prop1", singletonList("1"));
    map.put("prop2", singletonList("2.1"));
    map.put("prop3", singletonList("aaa"));
    map.put("prop4", singletonList("hello"));
    map.put("other", singletonList("hello"));

    assertThatExceptionOfType(MalformedValueException.class)
      .isThrownBy(() -> parser.parseParameter(map));
  }

}
