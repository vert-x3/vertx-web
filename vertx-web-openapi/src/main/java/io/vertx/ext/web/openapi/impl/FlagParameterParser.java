package io.vertx.ext.web.openapi.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parameter.ParameterParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;
import java.util.Map;

import static io.vertx.ext.web.validation.impl.parameter.ExplodedObjectValueParameterParser.isExplodedObjectValueParameterParserWithAdditionalProperties;

public class FlagParameterParser implements ParameterParser {

  String parameterName;

  public FlagParameterParser(String parameterName) {
    this.parameterName = parameterName;
  }

  @Override
  public @Nullable Object parseParameter(Map<String, List<String>> parameterValue) throws MalformedValueException {
    List<String> extractedList = parameterValue.remove(parameterName);
    if (extractedList == null) return null;
    String extracted = extractedList.get(0);
    if (extracted.isEmpty()) return null;
    else return ValueParser.BOOLEAN_PARSER.parse(extracted);
  }

  @Override
  public int compareTo(ParameterParser o) {
    if (isExplodedObjectValueParameterParserWithAdditionalProperties(o)) return -1;
    return 0;
  }
}
