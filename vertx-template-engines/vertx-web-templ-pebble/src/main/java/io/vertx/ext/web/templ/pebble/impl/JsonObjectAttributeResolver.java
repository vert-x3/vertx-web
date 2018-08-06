package io.vertx.ext.web.templ.pebble.impl;

import com.mitchellbosecke.pebble.attributes.AttributeResolver;
import com.mitchellbosecke.pebble.attributes.ResolvedAttribute;
import com.mitchellbosecke.pebble.node.ArgumentsNode;
import com.mitchellbosecke.pebble.template.EvaluationContextImpl;

import io.vertx.core.json.JsonObject;

public class JsonObjectAttributeResolver implements AttributeResolver {

  @Override
  public ResolvedAttribute resolve(Object instance, Object attributeNameValue, Object[] argumentValues, ArgumentsNode args,
    EvaluationContextImpl context, String filename, int lineNumber) {
    if (instance instanceof JsonObject) {
      JsonObject json = (JsonObject) instance;
      if (attributeNameValue instanceof String) {
        String name = (String) attributeNameValue;
        // We only return found values. Resolved null values will be returned by the default resolvers of pebble
        if (json.containsKey(name)) {
          Object value = json.getValue(name);
          return new ResolvedAttribute(value);
        }
      }
    }
    return null;
  }

}
