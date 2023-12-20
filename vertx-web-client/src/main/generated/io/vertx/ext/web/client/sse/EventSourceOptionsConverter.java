package io.vertx.ext.web.client.sse;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.ext.web.client.sse.EventSourceOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.client.sse.EventSourceOptions} original class using Vert.x codegen.
 */
public class EventSourceOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, EventSourceOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "reconnectInterval":
          if (member.getValue() instanceof Number) {
            obj.setReconnectInterval(((Number)member.getValue()).longValue());
          }
          break;
        case "url":
          if (member.getValue() instanceof String) {
            obj.setUrl((String)member.getValue());
          }
          break;
        case "withCredentials":
          if (member.getValue() instanceof Boolean) {
            obj.setWithCredentials((Boolean)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(EventSourceOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(EventSourceOptions obj, java.util.Map<String, Object> json) {
    json.put("reconnectInterval", obj.getReconnectInterval());
    if (obj.getUrl() != null) {
      json.put("url", obj.getUrl());
    }
    json.put("withCredentials", obj.isWithCredentials());
  }
}
