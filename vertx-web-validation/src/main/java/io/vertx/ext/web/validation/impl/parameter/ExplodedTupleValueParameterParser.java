package io.vertx.ext.web.validation.impl.parameter;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parser.TupleParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import static io.vertx.ext.web.validation.impl.parameter.ExplodedObjectValueParameterParser.isExplodedObjectValueParameterParserWithAdditionalProperties;

public class ExplodedTupleValueParameterParser extends TupleParser implements ParameterParser {

  String parameterName;

  public ExplodedTupleValueParameterParser(List<ValueParser<String>> itemsParser, ValueParser<String> additionalItemsParser, String parameterName) {
    super(itemsParser, additionalItemsParser);
    this.parameterName = parameterName;
  }

  @Override
  public @Nullable Object parseParameter(Map<String, List<String>> parameters) throws MalformedValueException {
    List<String> values = parameters.remove(parameterName);
    if (values == null) return null;
    return IntStream
      .range(0, values.size())
      .mapToObj(i -> parseItem(i, values.get(i)))
      .flatMap(Function.identity())
      .collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::addAll));
  }

  @Override
  protected boolean mustNullateValue(String serialized, ValueParser<String> parser) {
    return serialized.isEmpty() && parser != ValueParser.NOOP_PARSER;
  }

  @Override
  public int compareTo(ParameterParser o) {
    if (isExplodedObjectValueParameterParserWithAdditionalProperties(o)) return -1;
    return 0;
  }
}
