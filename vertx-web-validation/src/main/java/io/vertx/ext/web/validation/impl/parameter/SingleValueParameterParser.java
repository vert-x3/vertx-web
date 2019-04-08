package io.vertx.ext.web.validation.impl.parameter;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;
import java.util.Map;

import static io.vertx.ext.web.validation.impl.parameter.ExplodedObjectValueParameterParser.isExplodedObjectValueParameterParserWithAdditionalProperties;

public class SingleValueParameterParser implements ParameterParser  {

  String parameterName;
  ValueParser<String> valueParser;

  public SingleValueParameterParser(String parameterName, ValueParser<String> valueParser) {
    this.parameterName = parameterName;
    this.valueParser = valueParser;
  }

  @Override
  public @Nullable Object parseParameter(Map<String, List<String>> parameterValue) throws MalformedValueException {
    List<String> extractedList = parameterValue.remove(parameterName);
    if (extractedList == null) return null;
    String extracted = extractedList.get(0);
    return extracted != null ? valueParser.parse(extracted) : null;
  }

  @Override
  public int compareTo(ParameterParser o) {
    if (isExplodedObjectValueParameterParserWithAdditionalProperties(o)) return -1;
    return 0;
  }
}
