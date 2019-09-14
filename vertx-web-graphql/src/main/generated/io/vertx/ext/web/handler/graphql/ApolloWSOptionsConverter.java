package io.vertx.ext.web.handler.graphql;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.ext.web.handler.graphql.ApolloWSOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.graphql.ApolloWSOptions} original class using Vert.x codegen.
 */
public class ApolloWSOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, ApolloWSOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "keepAlive":
          if (member.getValue() instanceof Number) {
            obj.setKeepAlive(((Number)member.getValue()).longValue());
          }
          break;
      }
    }
  }

  public static void toJson(ApolloWSOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(ApolloWSOptions obj, java.util.Map<String, Object> json) {
    json.put("keepAlive", obj.getKeepAlive());
  }
}
