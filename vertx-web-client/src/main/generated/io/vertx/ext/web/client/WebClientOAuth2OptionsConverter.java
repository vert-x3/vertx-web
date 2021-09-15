package io.vertx.ext.web.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.ext.web.client.WebClientOAuth2Options}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.client.WebClientOAuth2Options} original class using Vert.x codegen.
 */
public class WebClientOAuth2OptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, WebClientOAuth2Options obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "leeway":
          if (member.getValue() instanceof Number) {
            obj.setLeeway(((Number)member.getValue()).intValue());
          }
          break;
        case "renewTokenOnForbidden":
          if (member.getValue() instanceof Boolean) {
            obj.setRenewTokenOnForbidden((Boolean)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(WebClientOAuth2Options obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(WebClientOAuth2Options obj, java.util.Map<String, Object> json) {
    json.put("leeway", obj.getLeeway());
    json.put("renewTokenOnForbidden", obj.isRenewTokenOnForbidden());
  }
}
