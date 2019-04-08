package io.vertx.ext.web.validation.impl.parser;

public abstract class ArrayParser {

  protected final ValueParser<String> itemsParser;

  public ArrayParser(ValueParser<String> itemsParser) {
    this.itemsParser = itemsParser;
  }

  protected Object parseValue(String v) {
    return mustNullateValue(v) ? null : itemsParser.parse(v);
  }

  protected abstract boolean mustNullateValue(String serialized);
}
