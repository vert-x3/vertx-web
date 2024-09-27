package io.vertx.ext.web.validation.tests.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.impl.ValueParserInferenceUtils;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import io.vertx.junit5.VertxExtension;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.regex.Pattern;

import static io.vertx.json.schema.common.dsl.Schemas.arraySchema;
import static io.vertx.json.schema.common.dsl.Schemas.booleanSchema;
import static io.vertx.json.schema.common.dsl.Schemas.intSchema;
import static io.vertx.json.schema.common.dsl.Schemas.numberSchema;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;
import static io.vertx.json.schema.common.dsl.Schemas.tupleSchema;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@ExtendWith(VertxExtension.class)
public class ValueParserInferenceUtilsTest {

  @Test
  public void testPrimitiveInference() {
    Assertions.assertThat(ValueParserInferenceUtils.infeerPrimitiveParser(new JsonObject().put("type", "integer")))
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
    JsonObject schemaJson = objectSchema()
      .property("simpleProp", stringSchema())
      .patternProperty(Pattern.compile("a*"), intSchema())
      .additionalProperties(booleanSchema()).toJson();

    assertThat(ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(schemaJson))
      .containsOnly(
        entry("simpleProp", ValueParser.NOOP_PARSER)
      );
    assertThat(
      ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(schemaJson)
    ).hasEntrySatisfying(
      new Condition<>(p -> p.toString().equals("a*"), "Must have a* as key"),
      new Condition<>(vp -> vp == ValueParser.LONG_PARSER, "Must have LONG_PARSER as value")
    ).hasSize(1);
    assertThat(ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(schemaJson))
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
    JsonObject schemaJson = arraySchema()
      .items(intSchema())
      .toJson();

    assertThat(ValueParserInferenceUtils.infeerItemsParserForArraySchema(schemaJson))
      .isSameAs(ValueParser.LONG_PARSER);
  }

  @Test
  public void testTupleInference() {
    JsonObject schemaJson = tupleSchema()
      .item(intSchema())
      .item(numberSchema())
      .additionalItems(booleanSchema())
      .toJson();

    assertThat(ValueParserInferenceUtils.infeerTupleParsersForArraySchema(schemaJson))
      .containsExactly(
        ValueParser.LONG_PARSER,
        ValueParser.DOUBLE_PARSER
      );
    assertThat(ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(schemaJson))
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
