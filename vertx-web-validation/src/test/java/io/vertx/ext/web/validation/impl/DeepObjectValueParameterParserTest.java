package io.vertx.ext.web.validation.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parameter.DeepObjectValueParameterParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import io.vertx.ext.web.validation.testutils.TestParsers;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(VertxExtension.class)
public class DeepObjectValueParameterParserTest {

  SchemaRouter router;
  SchemaParser parser;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(router);
  }

  @Test
  public void testValid() {
    DeepObjectValueParameterParser parser = new DeepObjectValueParameterParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      "bla"
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("bla[prop1]", singletonList("1"));
    map.put("bla[prop2]", singletonList("2.1"));
    map.put("bla[prop3]", singletonList("aaa"));
    map.put("bla[prop4]", singletonList("true"));
    map.put("bla[other]", singletonList("hello"));
    map.put("other", singletonList("francesco"));
    map.put("other[bla]", singletonList("world"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().put("other", "hello"))
      );

    assertThat(map)
      .containsKeys("other", "other[bla]");
  }

  @Test
  public void testNoAdditionalProperties() {
    DeepObjectValueParameterParser parser = new DeepObjectValueParameterParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      null,
      "bla"
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("bla[prop1]", singletonList("1"));
    map.put("bla[prop2]", singletonList("2.1"));
    map.put("bla[prop3]", singletonList("aaa"));
    map.put("bla[prop4]", singletonList("true"));
    map.put("bla[other]", singletonList("hello"));
    map.put("other", singletonList("francesco"));
    map.put("other[bla]", singletonList("world"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().put("other", "hello"))
      );

    assertThat(map)
      .containsKeys("other", "other[bla]");
  }

  @Test
  public void testNull() {
    DeepObjectValueParameterParser parser = new DeepObjectValueParameterParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      "bla"
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("bla[prop1]", singletonList("1"));
    map.put("bla[prop2]", singletonList(""));
    map.put("bla[prop3]", singletonList(null));
    map.put("bla[prop4]", singletonList("true"));
    map.put("bla[other]", singletonList("hello"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().put("other", "hello").putNull("prop2").putNull("prop3"))
      );

    assertThat(map)
      .isEmpty();
  }

  @Test
  public void testEmptyString() {
    DeepObjectValueParameterParser parser = new DeepObjectValueParameterParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      "bla"
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("bla[prop1]", singletonList("1"));
    map.put("bla[prop2]", singletonList("2.1"));
    map.put("bla[prop3]", singletonList(""));
    map.put("bla[prop4]", singletonList("true"));
    map.put("bla[other]", singletonList("hello"));

    Object result = parser.parseParameter(map);

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().put("other", "hello").put("prop3", ""))
      );

    assertThat(map)
      .isEmpty();
  }

  @Test
  public void testMissingProp() {
    DeepObjectValueParameterParser parser = new DeepObjectValueParameterParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      "bla"
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("bla[prop1]", singletonList("1"));
    map.put("bla[prop3]", singletonList("aaa"));

    Object result = parser.parseParameter(map);

    JsonObject expected = TestParsers.SAMPLE_OBJECT.copy();
    expected.remove("prop2");
    expected.remove("prop4");

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
    DeepObjectValueParameterParser parser = new DeepObjectValueParameterParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      "bla"
    );

    Map<String, List<String>> map = new HashMap<>();
    map.put("bla[prop1]", singletonList("1"));
    map.put("bla[prop2]", singletonList("bla"));
    map.put("bla[prop3]", singletonList("aaa"));
    map.put("bla[prop4]", singletonList("true"));
    map.put("bla[other]", singletonList("hello"));
    map.put("other", singletonList("francesco"));
    map.put("other[bla]", singletonList("world"));

    assertThatExceptionOfType(MalformedValueException.class)
      .isThrownBy(() -> parser.parseParameter(map));
  }

}
