package io.vertx.ext.web.api.service;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.ext.web.api.service.ServiceRequest}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.api.service.ServiceRequest} original class using Vert.x codegen.
 */
public class ServiceRequestConverter {


   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, ServiceRequest obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "extra":
          if (member.getValue() instanceof JsonObject) {
            obj.setExtra(((JsonObject)member.getValue()).copy());
          }
          break;
        case "params":
          if (member.getValue() instanceof JsonObject) {
            obj.setParams(((JsonObject)member.getValue()).copy());
          }
          break;
        case "user":
          if (member.getValue() instanceof JsonObject) {
            obj.setUser(((JsonObject)member.getValue()).copy());
          }
          break;
      }
    }
  }

   static void toJson(ServiceRequest obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(ServiceRequest obj, java.util.Map<String, Object> json) {
    if (obj.getExtra() != null) {
      json.put("extra", obj.getExtra());
    }
    if (obj.getParams() != null) {
      json.put("params", obj.getParams());
    }
    if (obj.getUser() != null) {
      json.put("user", obj.getUser());
    }
  }
}
