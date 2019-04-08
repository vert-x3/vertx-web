package io.vertx.ext.web.validation.testutils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestParsers {

  public static final Map<String, ValueParser<String>> SAMPLE_PROPERTIES_PARSERS =
    Stream.of(
      new SimpleImmutableEntry<>("prop1", ValueParser.LONG_PARSER),
      new SimpleImmutableEntry<>("prop2", ValueParser.DOUBLE_PARSER)
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  public static final Map<Pattern, ValueParser<String>> SAMPLE_PATTERN_PROPERTIES_PARSERS =
    Stream.of(
      new SimpleImmutableEntry<>(Pattern.compile("prop3"), ValueParser.NOOP_PARSER),
      new SimpleImmutableEntry<>(Pattern.compile("prop4"), ValueParser.BOOLEAN_PARSER)
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  public static final JsonObject SAMPLE_OBJECT =
    new JsonObject()
      .put("prop1", 1L)
      .put("prop2", 2.1d)
      .put("prop3", "aaa")
      .put("prop4", true);

  public static final List<ValueParser<String>> SAMPLE_TUPLE_ITEMS_PARSERS =
    Arrays.asList(
      ValueParser.DOUBLE_PARSER,
      ValueParser.NOOP_PARSER,
      ValueParser.DOUBLE_PARSER
    );

  public static final JsonArray SAMPLE_TUPLE =
    new JsonArray().add(1d).add("hello").add(2d);

}
