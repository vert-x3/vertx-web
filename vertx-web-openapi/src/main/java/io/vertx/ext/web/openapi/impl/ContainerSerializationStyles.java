package io.vertx.ext.web.openapi.impl;

import io.vertx.ext.web.validation.builder.ArrayParserFactory;
import io.vertx.ext.web.validation.builder.ObjectParserFactory;
import io.vertx.ext.web.validation.impl.parser.SplitterCharArrayParser;
import io.vertx.ext.web.validation.impl.parser.SplitterCharObjectParser;

public enum ContainerSerializationStyles {
  CSV(
    itemsParser -> new SplitterCharArrayParser(itemsParser, "\\,"),
    (propertiesParser, patternPropertiesParser, additionalPropertiesParser) -> new SplitterCharObjectParser(propertiesParser, patternPropertiesParser, additionalPropertiesParser, "\\,")
  ),
  DSV(
    itemsParser -> new SplitterCharArrayParser(itemsParser, "\\."),
    (propertiesParser, patternPropertiesParser, additionalPropertiesParser) -> new SplitterCharObjectParser(propertiesParser, patternPropertiesParser, additionalPropertiesParser, "\\.")
  ),
  SSV(
      itemsParser -> new SplitterCharArrayParser(itemsParser, "\\s"),
    (propertiesParser, patternPropertiesParser, additionalPropertiesParser) -> new SplitterCharObjectParser(propertiesParser, patternPropertiesParser, additionalPropertiesParser, "\\s")
  ),
  PSV(
    itemsParser -> new SplitterCharArrayParser(itemsParser, "\\|"),
    (propertiesParser, patternPropertiesParser, additionalPropertiesParser) -> new SplitterCharObjectParser(propertiesParser, patternPropertiesParser, additionalPropertiesParser, "\\|")
  );

  private final ArrayParserFactory arrayFactory;
  private final ObjectParserFactory objectFactory;

  ContainerSerializationStyles(ArrayParserFactory arrayFactory, ObjectParserFactory objectFactory) {
    this.arrayFactory = arrayFactory;
    this.objectFactory = objectFactory;
  }

  public ArrayParserFactory getArrayFactory() {
    return arrayFactory;
  }

  public ObjectParserFactory getObjectFactory() {
    return objectFactory;
  }

  public static ContainerSerializationStyles resolve(String style) {
    switch (style) {
      case "form":
      case "simple":
      case "matrix":
        return CSV;
      case "label":
        return DSV;
      case "spaceDelimited":
        return SSV;
      case "pipeDelimited":
        return PSV;
      default:
        throw new IllegalArgumentException("Cannot find deserializer for style " + style);
    }
  }
}
