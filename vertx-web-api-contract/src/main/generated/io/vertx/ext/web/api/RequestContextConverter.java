package io.vertx.ext.web.api;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link io.vertx.ext.web.api.RequestContext}.
 * NOTE: This class has been automatically generated from the {@link "io.vertx.ext.web.api.RequestContext} original class using Vert.x codegen.
 */
 class RequestContextConverter {

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, RequestContext obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "params":
          if (member.getValue() instanceof JsonObject) {
            obj.setParams(((JsonObject)member.getValue()).copy());
          }
          break;
      }
    }
  }

   static void toJson(RequestContext obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(RequestContext obj, java.util.Map<String, Object> json) {
    if (obj.getParams() != null) {
      json.put("params", obj.getParams());
    }
  }
}
