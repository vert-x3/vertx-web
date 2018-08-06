package io.vertx.ext.web.templ.pebble.impl;

import com.mitchellbosecke.pebble.attributes.AttributeResolver;
import com.mitchellbosecke.pebble.attributes.ResolvedAttribute;
import com.mitchellbosecke.pebble.node.ArgumentsNode;
import com.mitchellbosecke.pebble.template.EvaluationContextImpl;

import io.vertx.core.json.JsonArray;

public class JsonArrayAttributeResolver implements AttributeResolver {

  @Override
  public ResolvedAttribute resolve(Object instance, Object attributeNameValue, Object[] argumentValues, ArgumentsNode args,
    EvaluationContextImpl context, String filename, int lineNumber) {
    // Only handle fields. Don't handle method calls with arguments
    if (instance instanceof JsonArray && argumentValues == null) {
      JsonArray jsonArray = ((JsonArray) instance);
      if (attributeNameValue instanceof String) {
        String name = (String) attributeNameValue;
        if ("length".equals(name) || "size".equals(name)) {
          return new ResolvedAttribute(jsonArray.size());
        }
      } else if (attributeNameValue instanceof Number) {
        Number num = (Number) attributeNameValue;
        int idx = num.intValue();
        if (idx > jsonArray.size()) {
          return new ResolvedAttribute(null);
        }
        Object value = jsonArray.getValue(idx);
        if (value != null) {
          return new ResolvedAttribute(value);
        }
      }
    }
    return null;
  }

}
