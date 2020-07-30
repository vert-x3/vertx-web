package io.vertx.ext.web.openapi.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parameter.ParameterParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;
import java.util.Map;

import static io.vertx.ext.web.validation.impl.parameter.ExplodedObjectValueParameterParser.isExplodedObjectValueParameterParserWithAdditionalProperties;

public class AnyOfOneOfSingleParameterParser implements ParameterParser {

  String parameterName;
  List<ValueParser<String>> valueParsers;

  public AnyOfOneOfSingleParameterParser(String parameterName, List<ValueParser<String>> valueParsers) {
    this.parameterName = parameterName;
    this.valueParsers = valueParsers;
  }

  @Override
  public @Nullable Object parseParameter(Map<String, List<String>> parameterValue) throws MalformedValueException {
    List<String> extractedList = parameterValue.remove(parameterName);
    if (extractedList == null) return null;
    String extracted = extractedList.get(0);
    MalformedValueException lastException = new MalformedValueException("Cannot deserialize parameter " + parameterName + " with value " + extracted);
    for (ValueParser<String> p : valueParsers) {
      try {
        return p.parse(extracted);
      } catch (MalformedValueException e) {
        lastException = e;
      }
    }
    throw lastException;
  }

  @Override
  public int compareTo(ParameterParser o) {
    if (isExplodedObjectValueParameterParserWithAdditionalProperties(o)) return -1;
    return 0;
  }
}
