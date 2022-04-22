package io.vertx.ext.web.validation.impl.parser;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class ObjectParser<X> {

  private final Map<String, ValueParser<X>> propertiesParsers;
  private final Map<Pattern, ValueParser<X>> patternPropertiesParsers;
  protected final ValueParser<X> additionalPropertiesParser;

  public ObjectParser(Map<String, ValueParser<X>> propertiesParsers, Map<Pattern, ValueParser<X>> patternPropertiesParsers, ValueParser<X> additionalPropertiesParser) {
    this.propertiesParsers = propertiesParsers;
    this.patternPropertiesParsers = patternPropertiesParsers;
    this.additionalPropertiesParser = additionalPropertiesParser;
  }

  protected Map.Entry<String, Object> parseField(String key, X serialized) {
    ValueParser<X> valueParser = null;
    if (propertiesParsers != null && propertiesParsers.containsKey(key))
      valueParser = propertiesParsers.get(key);
    else if (patternPropertiesParsers != null) {
      valueParser = patternPropertiesParsers
        .entrySet()
        .stream()
        .filter(e -> e.getKey().matcher(key).find())
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(null);
    }
    if (valueParser == null) {
      valueParser = getAdditionalPropertiesParserIfRequired();
    }
    if (valueParser == null) return null;
    if (mustNullateValue(serialized, valueParser)) return new SimpleImmutableEntry<>(key, null);
    return new SimpleImmutableEntry<>(key, valueParser.parse(serialized));
  }

  protected abstract ValueParser<X> getAdditionalPropertiesParserIfRequired();

  protected abstract boolean mustNullateValue(X serialized, ValueParser<X> parser);

}
