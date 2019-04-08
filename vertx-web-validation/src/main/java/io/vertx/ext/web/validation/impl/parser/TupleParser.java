package io.vertx.ext.web.validation.impl.parser;

import java.util.List;
import java.util.stream.Stream;

public abstract class TupleParser {

  private final ValueParser<String>[] itemsParser;
  private final ValueParser<String> additionalItemsParser;

  @SuppressWarnings("unchecked")
  public TupleParser(List<ValueParser<String>> itemsParser, ValueParser<String> additionalItemsParser) {
    this.itemsParser = itemsParser.toArray(new ValueParser[0]);
    this.additionalItemsParser = additionalItemsParser != null ? additionalItemsParser : ValueParser.NOOP_PARSER;
  }

  protected Stream<Object> parseItem(int i, String serialized) {
    if (i < itemsParser.length)
      return Stream.of(parseValue(serialized, itemsParser[i]));
    else if (additionalItemsParser != null)
      return Stream.of(parseValue(serialized, additionalItemsParser));
    else
      return null;
  }

  private Object parseValue(String v, ValueParser<String> parser) {
    return mustNullateValue(v, parser) ? null : parser.parse(v);
  }

  protected abstract boolean mustNullateValue(String serialized, ValueParser<String> parser);
}
