package io.vertx.ext.web.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.web.client.WebClientOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.client.WebClientOptions} original class using Vert.x codegen.
 */
public class WebClientOptionsConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, WebClientOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "followRedirects":
          if (member.getValue() instanceof Boolean) {
            obj.setFollowRedirects((Boolean)member.getValue());
          }
          break;
        case "templateExpandOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setTemplateExpandOptions(new io.vertx.uritemplate.ExpandOptions((io.vertx.core.json.JsonObject)member.getValue()));
          }
          break;
        case "userAgent":
          if (member.getValue() instanceof String) {
            obj.setUserAgent((String)member.getValue());
          }
          break;
        case "userAgentEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setUserAgentEnabled((Boolean)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(WebClientOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(WebClientOptions obj, java.util.Map<String, Object> json) {
    json.put("followRedirects", obj.isFollowRedirects());
    if (obj.getTemplateExpandOptions() != null) {
      json.put("templateExpandOptions", obj.getTemplateExpandOptions().toJson());
    }
    if (obj.getUserAgent() != null) {
      json.put("userAgent", obj.getUserAgent());
    }
    json.put("userAgentEnabled", obj.isUserAgentEnabled());
  }
}
