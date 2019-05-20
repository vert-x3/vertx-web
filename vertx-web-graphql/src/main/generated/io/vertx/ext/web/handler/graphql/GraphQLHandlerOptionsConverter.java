package io.vertx.ext.web.handler.graphql;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter for {@link io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions} original class using Vert.x codegen.
 */
public class GraphQLHandlerOptionsConverter {

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, GraphQLHandlerOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "graphiQLOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setGraphiQLOptions(new io.vertx.ext.web.handler.graphql.GraphiQLOptions((JsonObject)member.getValue()));
          }
          break;
        case "requestBatchingEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setRequestBatchingEnabled((Boolean)member.getValue());
          }
          break;
      }
    }
  }

  public static void toJson(GraphQLHandlerOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(GraphQLHandlerOptions obj, java.util.Map<String, Object> json) {
    if (obj.getGraphiQLOptions() != null) {
      json.put("graphiQLOptions", obj.getGraphiQLOptions().toJson());
    }
    json.put("requestBatchingEnabled", obj.isRequestBatchingEnabled());
  }
}
