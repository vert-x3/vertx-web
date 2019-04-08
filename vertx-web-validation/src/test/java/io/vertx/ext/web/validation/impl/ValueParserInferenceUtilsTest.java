package io.vertx.ext.web.validation.impl;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.json.schema.Schema;
import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.json.schema.SchemaRouter;
import io.vertx.ext.json.schema.SchemaRouterOptions;
import io.vertx.ext.json.schema.draft7.Draft7SchemaParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;
import io.vertx.junit5.VertxExtension;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.regex.Pattern;

import static io.vertx.ext.json.schema.draft7.dsl.Schemas.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@ExtendWith(VertxExtension.class)
public class ValueParserInferenceUtilsTest {

  SchemaRouter router;
  SchemaParser parser;

  @BeforeEach
  public void setUp(Vertx vertx) {
    router = SchemaRouter.create(vertx, new SchemaRouterOptions());
    parser = Draft7SchemaParser.create(router);
  }

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
    Schema s = objectSchema()
      .property("simpleProp", stringSchema())
      .patternProperty(Pattern.compile("a*"), intSchema())
      .additionalProperties(booleanSchema())
      .build(parser);

    assertThat(ValueParserInferenceUtils.infeerPropertiesParsersForObjectSchema(s.getJson()))
      .containsOnly(
        entry("simpleProp", ValueParser.NOOP_PARSER)
      );
    assertThat(
      ValueParserInferenceUtils.infeerPatternPropertiesParsersForObjectSchema(s.getJson())
    ).hasEntrySatisfying(
      new Condition<>(p -> p.toString().equals("a*"), "Must have a* as key"),
      new Condition<>(vp -> vp == ValueParser.LONG_PARSER, "Must have LONG_PARSER as value")
    ).hasSize(1);
    assertThat(ValueParserInferenceUtils.infeerAdditionalPropertiesParserForObjectSchema(s.getJson()))
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
    Schema s = arraySchema()
      .items(intSchema())
      .build(parser);

    assertThat(ValueParserInferenceUtils.infeerItemsParserForArraySchema(s.getJson()))
      .isSameAs(ValueParser.LONG_PARSER);
  }

  @Test
  public void testTupleInference() {
    Schema s = tupleSchema()
      .item(intSchema())
      .item(numberSchema())
      .additionalItems(booleanSchema())
      .build(parser);

    assertThat(ValueParserInferenceUtils.infeerTupleParsersForArraySchema(s.getJson()))
      .containsExactly(
        ValueParser.LONG_PARSER,
        ValueParser.DOUBLE_PARSER
      );
    assertThat(ValueParserInferenceUtils.infeerAdditionalItemsParserForArraySchema(s.getJson()))
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
