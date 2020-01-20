package io.vertx.ext.web.validation.builder;

import io.vertx.ext.web.validation.impl.parser.SplitterCharArrayParser;
import io.vertx.ext.web.validation.impl.parser.SplitterCharObjectParser;
import io.vertx.ext.web.validation.impl.parser.SplitterCharTupleParser;

/**
 * In this interface you can find all available {@link ArrayParserFactory}, {@link ObjectParserFactory} & {@link TupleParserFactory}
 */
public interface Parsers {

  static ArrayParserFactory commaSeparatedArrayParser() {
    return itemsParser -> new SplitterCharArrayParser(itemsParser, ",");
  }

  static ObjectParserFactory commaSeparatedObjectParser() {
    return (propertiesParser, patternPropertiesParser, additionalPropertiesParser) -> new SplitterCharObjectParser(propertiesParser, patternPropertiesParser, additionalPropertiesParser, ",");
  }

  static TupleParserFactory commaSeparatedTupleParser() {
    return (itemsParser, additionalItemsParser) -> new SplitterCharTupleParser(itemsParser, additionalItemsParser, ",");
  }

}
