package io.vertx.ext.web.api;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Converter and Codec for {@link io.vertx.ext.web.api.OperationResponse}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.api.OperationResponse} original class using Vert.x codegen.
 */
public class OperationResponseConverter implements JsonCodec<OperationResponse, JsonObject> {

  public static final OperationResponseConverter INSTANCE = new OperationResponseConverter();

  @Override public JsonObject encode(OperationResponse value) { return (value != null) ? value.toJson() : null; }

  @Override public OperationResponse decode(JsonObject value) { return (value != null) ? new OperationResponse(value) : null; }

  @Override public Class<OperationResponse> getTargetClass() { return OperationResponse.class; }

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, OperationResponse obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "payload":
          if (member.getValue() instanceof String) {
            obj.setPayload(io.vertx.core.buffer.Buffer.buffer(java.util.Base64.getDecoder().decode((String)member.getValue())));
          }
          break;
        case "statusCode":
          if (member.getValue() instanceof Number) {
            obj.setStatusCode(((Number)member.getValue()).intValue());
          }
          break;
        case "statusMessage":
          if (member.getValue() instanceof String) {
            obj.setStatusMessage((String)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(OperationResponse obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(OperationResponse obj, java.util.Map<String, Object> json) {
    if (obj.getPayload() != null) {
      json.put("payload", java.util.Base64.getEncoder().encodeToString(obj.getPayload().getBytes()));
    }
    if (obj.getStatusCode() != null) {
      json.put("statusCode", obj.getStatusCode());
    }
    if (obj.getStatusMessage() != null) {
      json.put("statusMessage", obj.getStatusMessage());
    }
  }
}
