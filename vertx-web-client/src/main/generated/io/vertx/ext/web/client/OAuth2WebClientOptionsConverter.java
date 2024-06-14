package io.vertx.ext.web.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.web.client.OAuth2WebClientOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.client.OAuth2WebClientOptions} original class using Vert.x codegen.
 */
public class OAuth2WebClientOptionsConverter {

  private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, OAuth2WebClientOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "renewTokenOnForbidden":
          if (member.getValue() instanceof Boolean) {
            obj.setRenewTokenOnForbidden((Boolean)member.getValue());
          }
          break;
        case "leeway":
          if (member.getValue() instanceof Number) {
            obj.setLeeway(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

   static void toJson(OAuth2WebClientOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(OAuth2WebClientOptions obj, java.util.Map<String, Object> json) {
    json.put("renewTokenOnForbidden", obj.isRenewTokenOnForbidden());
    json.put("leeway", obj.getLeeway());
  }
}
