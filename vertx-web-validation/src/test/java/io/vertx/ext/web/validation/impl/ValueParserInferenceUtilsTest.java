package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static io.vertx.json.schema.draft7.dsl.Schemas.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class ValueParserInferenceUtilsTest {

  @Test
  public void testPrimitiveInference() {
    assertThat(ValueParserInferenceUtils.infeerPrimitiveParser(new JsonObject().put("type", "integer")))
      .isSameAs(ValueParser.LONG_PARSER);
    assertThat(ValueParserInferenceUtils.infeerPrimitiveParser(new JsonObject().put("type", "number")))
      .isSameAs(ValueParser.DOUBLE_PARSER);
    assertThat(ValueParserInferenceUtils.infeerPrimitiveParser(new JsonObject().put("type", "string")))
      .isSameAs(ValueParser.NOOP_PARSER);
    assertThat(ValueParserInferenceUtils.infeerPrimitiveParser(new JsonObject().put("type", "boolean")))
      .isSameAs(ValueParser.BOOLEAN_PARSER);
  }

  @Test
  public void testObjectInference() {
    JsonObject s = objectSchema()
      .property("simpleProp", stringSchema())
      .patternProperty(Pattern.compile("a*"), intSchema())
      .additionalProperties(booleanSchema())
      .toJson();

    assertThat(ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(s))
      .containsOnly(
        entry("simpleProp", ValueParser.NOOP_PARSER)
      );
    assertThat(
      ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(s)
    ).hasEntrySatisfying(
      new Condition<>(p -> p.toString().equals("a*"), "Must have a* as key"),
      new Condition<>(vp -> vp == ValueParser.LONG_PARSER, "Must have LONG_PARSER as value")
    ).hasSize(1);
    assertThat(ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(s))
      .isSameAs(ValueParser.BOOLEAN_PARSER);
  }

  @Test
  public void testAdditionalPropertiesObjectInference() {
    JsonObject schema = new JsonObject().put("additionalProperties", true);

    assertThat(ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(schema))
      .isSameAs(ValueParser.NOOP_PARSER);
  }

  @Test
  public void testNoAdditionalPropertiesObjectInference() {
    JsonObject schema = new JsonObject().put("additionalProperties", false);

    assertThat(ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(schema))
      .isNull();
  }

  @Test
  public void testArrayInference() {
    JsonObject s = arraySchema()
      .items(intSchema())
      .toJson();

    assertThat(ValueParserInferenceUtils.infeerItemsParserForArraySchema(s))
      .isSameAs(ValueParser.LONG_PARSER);
  }

  @Test
  public void testTupleInference() {
    JsonObject s = tupleSchema()
      .item(intSchema())
      .item(numberSchema())
      .additionalItems(booleanSchema())
      .toJson();

    assertThat(ValueParserInferenceUtils.infeerTupleParsersForArraySchema(s))
      .containsExactly(
        ValueParser.LONG_PARSER,
        ValueParser.DOUBLE_PARSER
      );
    assertThat(ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(s))
      .isSameAs(ValueParser.BOOLEAN_PARSER);
  }

  @Test
  public void testAdditionalItemsInference() {
    JsonObject schema = new JsonObject().put("additionalItems", true);

    assertThat(ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(schema))
      .isSameAs(ValueParser.NOOP_PARSER);
  }

  @Test
  public void testNoAdditionalItemsInference() {
    JsonObject schema = new JsonObject().put("additionalItems", false);

    assertThat(ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(schema))
      .isNull();
  }

}
