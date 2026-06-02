package io.vertx.ext.web.handler;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter and mapper for {@link io.vertx.ext.web.handler.AltSvcOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.AltSvcOptions} original class using Vert.x codegen.
 */
public class AltSvcOptionsConverter {

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, AltSvcOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "origins":
          if (member.getValue() instanceof JsonObject) {
            ((Iterable<java.util.Map.Entry<String, Object>>)member.getValue()).forEach(entry -> {
              if (entry.getValue() instanceof String)
                obj.addOrigin(entry.getKey(), (String)entry.getValue());
            });
          }
          break;
      }
    }
  }

   static void toJson(AltSvcOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(AltSvcOptions obj, java.util.Map<String, Object> json) {
    if (obj.getOrigins() != null) {
      JsonObject map = new JsonObject();
      obj.getOrigins().forEach((key, value) -> map.put(key, value));
      json.put("origins", map);
    }
  }
}
