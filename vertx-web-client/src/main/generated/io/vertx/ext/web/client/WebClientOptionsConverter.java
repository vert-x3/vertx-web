package io.vertx.ext.web.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonMapper;

/**
 * Converter and mapper for {@link io.vertx.ext.web.client.WebClientOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.client.WebClientOptions} original class using Vert.x codegen.
 */
public class WebClientOptionsConverter implements JsonMapper<WebClientOptions, JsonObject> {

  public static final WebClientOptionsConverter INSTANCE = new WebClientOptionsConverter();

  @Override public JsonObject serialize(WebClientOptions value) { return (value != null) ? value.toJson() : null; }

  @Override public WebClientOptions deserialize(JsonObject value) { return (value != null) ? new WebClientOptions(value) : null; }

  @Override public Class<WebClientOptions> getTargetClass() { return WebClientOptions.class; }

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, WebClientOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "followRedirects":
          if (member.getValue() instanceof Boolean) {
            obj.setFollowRedirects((Boolean)member.getValue());
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

  public static void toJson(WebClientOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(WebClientOptions obj, java.util.Map<String, Object> json) {
    json.put("followRedirects", obj.isFollowRedirects());
    if (obj.getUserAgent() != null) {
      json.put("userAgent", obj.getUserAgent());
    }
    json.put("userAgentEnabled", obj.isUserAgentEnabled());
  }
}
