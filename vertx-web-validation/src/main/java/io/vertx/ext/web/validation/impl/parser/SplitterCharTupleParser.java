package io.vertx.ext.web.validation.impl.parser;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.validation.MalformedValueException;

import java.util.List;

public class SplitterCharTupleParser extends TupleParser implements ValueParser<String> {

  private final String separator;

  public SplitterCharTupleParser(List<ValueParser<String>> itemsParser, ValueParser<String> additionalItemsParser, String separator) {
    super(itemsParser, additionalItemsParser);
    this.separator = separator;
  }

  @Override
  public JsonArray parse(String serialized) throws MalformedValueException {
    JsonArray result = new JsonArray();
    String[] splitted = serialized.split(separator, -1);
    for (int i = 0; i < splitted.length; i++) {
      parseItem(i, splitted[i]).forEach(result::add);
    }
    return result;
  }

  @Override
  protected boolean mustNullateValue(String serialized, ValueParser<String> parser) {
    return serialized.isEmpty() && parser != ValueParser.NOOP_PARSER;
  }
}
