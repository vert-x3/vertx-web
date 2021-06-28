package io.vertx.ext.web.validation.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.impl.body.FormValueParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.vertx.ext.web.validation.impl.parser.ValueParser.*;

public class ValueParserInferenceUtils {

  public static ValueParser<String> infeerPrimitiveParser(Object schema) {
    if (schema == null) return null;
    if (schema instanceof Boolean) {
      return (boolean) schema ? NOOP_PARSER : null;
    }
    String type = ((JsonObject) schema).getString("type");
    if (type == null) {
      return NOOP_PARSER;
    }
    switch (type) {
      case "integer":
        return LONG_PARSER;
      case "number":
        return DOUBLE_PARSER;
      case "boolean":
        return BOOLEAN_PARSER;
      default:
        return NOOP_PARSER;
    }
  }

  public static Map<String, ValueParser<String>> infeerPropertiesParsersForObjectSchema(Object s) {
    return jsonObjectSchemaToMapOfValueParser(
      s,
      "properties",
      str -> str,
      ValueParserInferenceUtils::infeerPrimitiveParser
    );
  }

  public static Map<String, ValueParser<String>> infeerPropertiesParsersForObjectSchema(Object s, Function<String,
    String> keyMapper) {
    return jsonObjectSchemaToMapOfValueParser(
      s,
      "properties",
      keyMapper,
      ValueParserInferenceUtils::infeerPrimitiveParser
    );
  }

  public static Map<Pattern, ValueParser<String>> infeerPatternPropertiesParsersForObjectSchema(Object s) {
    return jsonObjectSchemaToMapOfValueParser(
      s,
      "patternProperties",
      Pattern::compile,
      ValueParserInferenceUtils::infeerPrimitiveParser
    );
  }

  public static ValueParser<String> infeerAdditionalPropertiesParserForObjectSchema(Object s) {
    return infeerPrimitiveParserFromSchemaProperty(s, "additionalProperties");
  }

  public static ValueParser<String> infeerItemsParserForArraySchema(Object s) {
    return infeerPrimitiveParserFromSchemaProperty(s, "items");
  }

  public static List<ValueParser<String>> infeerTupleParsersForArraySchema(Object s) {
    try {
      return ((JsonObject) s)
        .getJsonArray("items")
        .stream()
        .map(ValueParserInferenceUtils::infeerPrimitiveParser)
        .collect(Collectors.toList());
    } catch (ClassCastException | NullPointerException e) {
      return null;
    }
  }

  public static ValueParser<String> infeerAdditionalItemsParserForArraySchema(Object s) {
    return infeerPrimitiveParserFromSchemaProperty(s, "additionalItems");
  }

  private static ValueParser<String> infeerPrimitiveParserFromSchemaProperty(Object s, String keySchemaProp) {
    try {
      return infeerPrimitiveParser(((JsonObject)s).getValue(keySchemaProp));
    } catch (ClassCastException | NullPointerException e) {
      return null;
    }
  }

  public static Map<String, ValueParser<List<String>>> infeerPropertiesFormValueParserForObjectSchema(Object s) {
    return jsonObjectSchemaToMapOfValueParser(s, "properties", Function.identity(), ValueParserInferenceUtils::mapSchemaToFormValueParser);
  }

  public static Map<Pattern, ValueParser<List<String>>> infeerPatternPropertiesFormValueParserForObjectSchema(Object s) {
    return jsonObjectSchemaToMapOfValueParser(s, "patternProperties", Pattern::compile, ValueParserInferenceUtils::mapSchemaToFormValueParser);
  }

  public static ValueParser<List<String>> infeerAdditionalPropertiesFormValueParserForObjectSchema(Object s) {
    try {
      return mapSchemaToFormValueParser(((JsonObject)s).getValue("additionalProperties"));
    } catch (ClassCastException | NullPointerException e) {
      return null;
    }
  }

  private static <K, V> Map<K, V> jsonObjectSchemaToMapOfValueParser(Object s, String keyword, Function<String, K> keyMapper, Function<Object, V> valueMapper) {
    try {
      JsonObject schema = (JsonObject) s;
      return schema.getJsonObject(keyword)
        .stream()
        .map(e -> new SimpleImmutableEntry<>(
            keyMapper.apply(e.getKey()),
            valueMapper.apply(e.getValue())
          )
        )
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (ClassCastException | NullPointerException e) {
      return null;
    }
  }

  private static FormValueParser mapSchemaToFormValueParser(Object o) {
    if (!(o instanceof JsonObject))
      return new FormValueParser(false, NOOP_PARSER);
    JsonObject inner = (JsonObject) o;
    if ("array".equals(inner.getString("type")))
      return new FormValueParser(true, infeerPrimitiveParser(inner.getJsonObject("items")));
    return new FormValueParser(false, infeerPrimitiveParser(inner));
  }

}
