package io.vertx.ext.web.validation.impl.body;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;

public class FormValueParser implements ValueParser<List<String>> {

  private final boolean expectedArray;
  private final ValueParser<String> innerValueParser;

  public FormValueParser(boolean expectedArray, ValueParser<String> innerValueParser) {
    this.expectedArray = expectedArray;
    this.innerValueParser = innerValueParser;
  }

  public Object parse(List<String> values) {
    if (expectedArray)
      return values.stream().map(innerValueParser::parse).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    else
      return innerValueParser.parse(values.get(0));
  }

}
