package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parameter.ExplodedArrayValueParameterParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ExplodedArrayValueParameterParserTest {

  @Test
  public void testValid() {
    ExplodedArrayValueParameterParser parser = new ExplodedArrayValueParameterParser(
      "bla", ValueParser.BOOLEAN_PARSER
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("bla", Arrays.asList("true", "false"));
    map.put("other", Collections.singletonList("aaa"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonArray.class, ja ->
        assertThat(ja)
          .containsOnly(true, false)
      );

    assertThat(map)
      .containsKey("other")
      .doesNotContainKey("bla");
  }

  @Test
  public void testNull() {
    ExplodedArrayValueParameterParser parser = new ExplodedArrayValueParameterParser(
      "bla", ValueParser.BOOLEAN_PARSER
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("bla", Arrays.asList("true", "", null, "false"));
    map.put("other", Collections.singletonList("aaa"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonArray.class, ja ->
        assertThat(ja)
          .containsOnly(true, null, null, false)
      );

    assertThat(map)
      .containsKey("other")
      .doesNotContainKey("bla");
  }

  @Test
  public void testInvalid() {
    ExplodedArrayValueParameterParser parser = new ExplodedArrayValueParameterParser(
      "bla", ValueParser.BOOLEAN_PARSER
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("bla", Arrays.asList("true", "notBoolean"));
    map.put("other", Collections.singletonList("aaa"));

    assertThatExceptionOfType(MalformedValueException.class)
      .isThrownBy(() -> parser.parseParameter(map));

    assertThat(map)
      .containsKey("other")
      .doesNotContainKey("bla");
  }
}
