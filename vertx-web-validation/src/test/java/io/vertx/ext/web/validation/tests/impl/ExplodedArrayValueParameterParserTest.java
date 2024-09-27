package io.vertx.ext.web.validation.tests.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parameter.ExplodedArrayValueParameterParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(VertxExtension.class)
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
