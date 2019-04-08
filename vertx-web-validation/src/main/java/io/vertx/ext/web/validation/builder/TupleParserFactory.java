package io.vertx.ext.web.validation.builder;

import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;

/**
 * This interface is used to create {@link ValueParser} able to parse serialized object structures. <br/>
 *
 * Look at {@link Parsers} for all available factories
 */
@FunctionalInterface
public interface TupleParserFactory {

  ValueParser<String> newTupleParser(List<ValueParser<String>> itemsParser, ValueParser<String> additionalItemsParser);

}
