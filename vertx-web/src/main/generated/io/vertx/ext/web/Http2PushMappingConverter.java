package io.vertx.ext.web;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.web.Http2PushMapping}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.Http2PushMapping} original class using Vert.x codegen.
 */
public class Http2PushMappingConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Http2PushMapping obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "extensionTarget":
          if (member.getValue() instanceof String) {
            obj.setExtensionTarget((String)member.getValue());
          }
          break;
        case "filePath":
          if (member.getValue() instanceof String) {
            obj.setFilePath((String)member.getValue());
          }
          break;
        case "noPush":
          if (member.getValue() instanceof Boolean) {
            obj.setNoPush((Boolean)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(Http2PushMapping obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(Http2PushMapping obj, java.util.Map<String, Object> json) {
    if (obj.getExtensionTarget() != null) {
      json.put("extensionTarget", obj.getExtensionTarget());
    }
    if (obj.getFilePath() != null) {
      json.put("filePath", obj.getFilePath());
    }
    json.put("noPush", obj.isNoPush());
  }
}
