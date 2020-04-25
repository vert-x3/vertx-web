package io.vertx.ext.web.handler.sockjs;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions} original class using Vert.x codegen.
 */
public class SockJSBridgeOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, SockJSBridgeOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "maxAddressLength":
          if (member.getValue() instanceof Number) {
            obj.setMaxAddressLength(((Number)member.getValue()).intValue());
          }
          break;
        case "maxHandlersPerSocket":
          if (member.getValue() instanceof Number) {
            obj.setMaxHandlersPerSocket(((Number)member.getValue()).intValue());
          }
          break;
        case "pingTimeout":
          if (member.getValue() instanceof Number) {
            obj.setPingTimeout(((Number)member.getValue()).longValue());
          }
          break;
        case "replyTimeout":
          if (member.getValue() instanceof Number) {
            obj.setReplyTimeout(((Number)member.getValue()).longValue());
          }
          break;
      }
    }
  }

  public static void toJson(SockJSBridgeOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(SockJSBridgeOptions obj, java.util.Map<String, Object> json) {
    json.put("maxAddressLength", obj.getMaxAddressLength());
    json.put("maxHandlersPerSocket", obj.getMaxHandlersPerSocket());
    json.put("pingTimeout", obj.getPingTimeout());
    json.put("replyTimeout", obj.getReplyTimeout());
  }
}
