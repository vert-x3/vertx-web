package io.vertx.ext.web.validation.impl.parameter;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parser.ArrayParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static io.vertx.ext.web.validation.impl.parameter.ExplodedObjectValueParameterParser.isExplodedObjectValueParameterParserWithAdditionalProperties;

public class ExplodedArrayValueParameterParser extends ArrayParser implements ParameterParser {

  final String parameterName;

  public ExplodedArrayValueParameterParser(String parameterName, ValueParser<String> itemsParser) {
    super(itemsParser);
    this.parameterName = parameterName;
  }

  @Override
  public @Nullable Object parseParameter(Map<String, List<String>> parameters) throws MalformedValueException {
    return parameters.containsKey(parameterName) ? parameters
      .remove(parameterName)
      .stream()
      .map(this::parseValue)
      .collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::addAll)) : null;
  }

  @Override
  protected boolean mustNullateValue(String serialized) {
    return serialized == null || (serialized.isEmpty() && itemsParser != ValueParser.NOOP_PARSER);
  }

  @Override
  public int compareTo(ParameterParser o) {
    if (isExplodedObjectValueParameterParserWithAdditionalProperties(o)) return -1;
    return 0;
  }
}
