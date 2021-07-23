package io.vertx.ext.web.validation.builder;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.List;

/**
 * This interface is used to create {@link ValueParser} able to parse serialized object structures. <br/>
 *
 * Look at {@link Parsers} for all available factories
 */
@VertxGen
@FunctionalInterface
public interface TupleParserFactory {

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  ValueParser<String> newTupleParser(List<ValueParser<String>> itemsParser, ValueParser<String> additionalItemsParser);

}
