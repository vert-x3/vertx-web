package io.vertx.ext.web.api;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonMapper;

/**
 * Converter and mapper for {@link io.vertx.ext.web.api.OperationRequest}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.api.OperationRequest} original class using Vert.x codegen.
 */
public class OperationRequestConverter implements JsonMapper<OperationRequest, JsonObject> {

  public static final OperationRequestConverter INSTANCE = new OperationRequestConverter();

  @Override public JsonObject serialize(OperationRequest value) { return (value != null) ? value.toJson() : null; }

  @Override public OperationRequest deserialize(JsonObject value) { return (value != null) ? new OperationRequest(value) : null; }

  @Override public Class<OperationRequest> getTargetClass() { return OperationRequest.class; }

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, OperationRequest obj) {
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

   static void toJson(OperationRequest obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(OperationRequest obj, java.util.Map<String, Object> json) {
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
