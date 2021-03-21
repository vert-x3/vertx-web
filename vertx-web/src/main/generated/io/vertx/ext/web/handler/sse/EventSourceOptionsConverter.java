package io.vertx.ext.web.handler.sse;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.ext.web.handler.sse.EventSourceOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.sse.EventSourceOptions} original class using Vert.x codegen.
 */
public class EventSourceOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, EventSourceOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "retryPeriod":
          if (member.getValue() instanceof Number) {
            obj.setRetryPeriod(((Number)member.getValue()).longValue());
          }
          break;
      }
    }
  }

  public static void toJson(EventSourceOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(EventSourceOptions obj, java.util.Map<String, Object> json) {
    json.put("retryPeriod", obj.getRetryPeriod());
  }
}
