package io.vertx.ext.web.validation.impl.parser;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.MalformedValueException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SplitterCharObjectParser extends ObjectParser<String> implements ValueParser<String> {

  private final String separator;

  public SplitterCharObjectParser(Map<String, ValueParser<String>> propertiesParsers, Map<Pattern, ValueParser<String>> patternPropertiesParsers, ValueParser<String> additionalPropertiesParsers, String separator) {
    super(propertiesParsers, patternPropertiesParsers, additionalPropertiesParsers);
    this.separator = separator;
  }

  @Override
  public JsonObject parse(String serialized) throws MalformedValueException {
    Map<String, Object> result = new HashMap<>();
    String[] values = serialized.split(separator, -1);
    // Key value pairs -> odd length not allowed
    if (values.length % 2 != 0)
      throw new MalformedValueException("Key value pair Object must have odd number of deserialized values");
    for (int i = 0; i < values.length; i += 2) {
      // empty key not allowed!
      if (values[i].length() == 0) {
        throw new MalformedValueException("Empty key not allowed");
      } else {
        Map.Entry<String, Object> parsed = parseField(values[i], values[i + 1]);
        if (parsed != null) result.put(parsed.getKey(), parsed.getValue());
      }
    }
    return new JsonObject(result);
  }

  @Override
  protected ValueParser<String> getAdditionalPropertiesParserIfRequired() {
    return (this.additionalPropertiesParser != null) ? this.additionalPropertiesParser : ValueParser.NOOP_PARSER;
  }

  @Override
  protected boolean mustNullateValue(String serialized, ValueParser<String> parser) {
    return serialized == null || (serialized.isEmpty() && parser != ValueParser.NOOP_PARSER);
  }
}
