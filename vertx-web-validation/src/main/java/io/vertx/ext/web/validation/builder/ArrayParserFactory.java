package io.vertx.ext.web.validation.builder;

import io.vertx.ext.web.validation.impl.parser.ValueParser;

/**
 * This interface is used to create {@link ValueParser} able to parse serialized array structures. <br/>
 *
 * Look at {@link Parsers} for all available factories
 */
@FunctionalInterface
public interface ArrayParserFactory {

  ValueParser<String> newArrayParser(ValueParser<String> itemsParser);

}
