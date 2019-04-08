package io.vertx.ext.web.validation.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parser.SplitterCharObjectParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import io.vertx.ext.web.validation.testutils.TestParsers;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(VertxExtension.class)
public class SplitterCharObjectValueParserTest {

  SchemaRouter router;
  SchemaParser parser;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(router);
  }

  @Test
  public void testValid() {
    SplitterCharObjectParser parser = new SplitterCharObjectParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      ","
    );

    Object result = parser.parse("prop1,1,prop2,2.1,prop3,aaa,prop4,true,other,hello");

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().put("other", "hello"))
      );
  }

  @Test
  public void testNoAdditionalProperties() {
    SplitterCharObjectParser parser = new SplitterCharObjectParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      null,
      ","
    );

    Object result = parser.parse("prop1,1,prop2,2.1,prop3,aaa,prop4,true,other,hello");

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().put("other", "hello"))
      );
  }

  @Test
  public void testInvalidNumber() {
    SplitterCharObjectParser parser = new SplitterCharObjectParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      ","
    );

    assertThatExceptionOfType(MalformedValueException.class)
      .isThrownBy(() -> parser.parse("true,hello,1"));
  }

  @Test
  public void testInvalidValueType() {
    SplitterCharObjectParser parser = new SplitterCharObjectParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      ValueParser.NOOP_PARSER,
      ","
    );

    assertThatExceptionOfType(MalformedValueException.class)
      .isThrownBy(() -> parser.parse("prop1,bla"));
  }

  @Test
  public void testMissingProps() {
    SplitterCharObjectParser parser = new SplitterCharObjectParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      null,
      ","
    );

    Object result = parser.parse("prop1,1");

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(new JsonObject().put("prop1", 1L))
      );
  }

  @Test
  public void testNullAndEmptyString() {
    SplitterCharObjectParser parser = new SplitterCharObjectParser(
      TestParsers.SAMPLE_PROPERTIES_PARSERS,
      TestParsers.SAMPLE_PATTERN_PROPERTIES_PARSERS,
      null,
      ","
    );

    Object result = parser.parse("prop1,,prop2,2.1,prop3,,prop4,true");

    assertThat(result)
      .isInstanceOfSatisfying(JsonObject.class, jo ->
        assertThat(jo)
          .isEqualTo(TestParsers.SAMPLE_OBJECT.copy().putNull("prop1").put("prop3", ""))
      );
  }
}
