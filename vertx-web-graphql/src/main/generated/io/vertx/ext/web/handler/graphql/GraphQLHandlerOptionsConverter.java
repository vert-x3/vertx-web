package io.vertx.ext.web.handler.graphql;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Converter and Codec for {@link io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions} original class using Vert.x codegen.
 */
public class GraphQLHandlerOptionsConverter implements JsonCodec<GraphQLHandlerOptions, JsonObject> {

  public static final GraphQLHandlerOptionsConverter INSTANCE = new GraphQLHandlerOptionsConverter();

  @Override public JsonObject encode(GraphQLHandlerOptions value) { return (value != null) ? value.toJson() : null; }

  @Override public GraphQLHandlerOptions decode(JsonObject value) { return (value != null) ? new GraphQLHandlerOptions(value) : null; }

  @Override public Class<GraphQLHandlerOptions> getTargetClass() { return GraphQLHandlerOptions.class; }

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, GraphQLHandlerOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "graphiQLOptions":
          if (member.getValue() instanceof JsonObject) {
            obj.setGraphiQLOptions(io.vertx.ext.web.handler.graphql.GraphiQLOptionsConverter.INSTANCE.decode((JsonObject)member.getValue()));
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
      json.put("graphiQLOptions", io.vertx.ext.web.handler.graphql.GraphiQLOptionsConverter.INSTANCE.encode(obj.getGraphiQLOptions()));
    }
    json.put("requestBatchingEnabled", obj.isRequestBatchingEnabled());
  }
}
