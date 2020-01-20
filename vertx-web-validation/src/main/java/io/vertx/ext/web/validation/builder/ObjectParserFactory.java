package io.vertx.ext.web.validation.builder;

import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * This interface is used to create {@link ValueParser} able to parse serialized object structures. <br/>
 *
 * Look at {@link Parsers} for all available factories
 */
@FunctionalInterface
public interface ObjectParserFactory {

  ValueParser<String> newObjectParser(Map<String, ValueParser<String>> propertiesParser, Map<Pattern, ValueParser<String>> patternPropertiesParser, ValueParser<String> additionalPropertiesParser);

}
