package io.vertx.ext.web.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.web.client.OAuth2WebClientOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.client.OAuth2WebClientOptions} original class using Vert.x codegen.
 */
public class OAuth2WebClientOptionsConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, OAuth2WebClientOptions obj) {
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

  public static void toJson(OAuth2WebClientOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(OAuth2WebClientOptions obj, java.util.Map<String, Object> json) {
    json.put("leeway", obj.getLeeway());
    json.put("renewTokenOnForbidden", obj.isRenewTokenOnForbidden());
  }
}
