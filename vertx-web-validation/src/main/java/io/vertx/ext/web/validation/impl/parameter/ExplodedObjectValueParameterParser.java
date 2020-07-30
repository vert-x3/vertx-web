package io.vertx.ext.web.validation.impl.parameter;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.MalformedValueException;
import io.vertx.ext.web.validation.impl.parser.ObjectParser;
import io.vertx.ext.web.validation.impl.parser.ValueParser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ExplodedObjectValueParameterParser extends ObjectParser<String> implements ParameterParser {

  String parameterName;

  public ExplodedObjectValueParameterParser(Map<String, ValueParser<String>> propertiesParsers, Map<Pattern, ValueParser<String>> patternPropertiesParsers, ValueParser<String> additionalPropertiesParsers, String parameterName) {
    super(propertiesParsers, patternPropertiesParsers, additionalPropertiesParsers);
    this.parameterName = parameterName;
  }

  @Override
  public @Nullable Object parseParameter(Map<String, List<String>> parameters) throws MalformedValueException {
    JsonObject obj = new JsonObject();
    Iterator<Map.Entry<String, List<String>>> it = parameters.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, List<String>> e = it.next();
      String key = e.getKey();
      Map.Entry<String, Object> parsed = parseField(key, e.getValue().get(0));
      if (parsed != null) {
        it.remove();
        obj.put(parsed.getKey(), parsed.getValue());
      }
    }
    return obj.isEmpty() ? null : obj;
  }

  @Override
  protected ValueParser<String> getAdditionalPropertiesParserIfRequired() {
    return this.additionalPropertiesParser; // if no additional properties parser, the prop should be ignored
  }

  @Override
  protected boolean mustNullateValue(String serialized, ValueParser<String> parser) {
    return serialized == null || (serialized.isEmpty() && parser != ValueParser.NOOP_PARSER);
  }

  @Override
  public int compareTo(ParameterParser o) {
    if (isExplodedObjectValueParameterParserWithAdditionalProperties(this)) return 1;
    if (isExplodedObjectValueParameterParserWithAdditionalProperties(o)) return -1;
    return 0;
  }

  public static boolean isExplodedObjectValueParameterParserWithAdditionalProperties(ParameterParser o) {
    return o instanceof ExplodedObjectValueParameterParser && ((ExplodedObjectValueParameterParser)o).additionalPropertiesParser != null;
  }
}
