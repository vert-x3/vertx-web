package io.vertx.ext.web.handler.graphql;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions} original class using Vert.x codegen.
 */
public class GraphQLHandlerOptionsConverter {

  private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
  private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, GraphQLHandlerOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "requestBatchingEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setRequestBatchingEnabled((Boolean)member.getValue());
          }
          break;
        case "requestMultipartEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setRequestMultipartEnabled((Boolean)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(GraphQLHandlerOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(GraphQLHandlerOptions obj, java.util.Map<String, Object> json) {
    json.put("requestBatchingEnabled", obj.isRequestBatchingEnabled());
    json.put("requestMultipartEnabled", obj.isRequestMultipartEnabled());
  }
}
