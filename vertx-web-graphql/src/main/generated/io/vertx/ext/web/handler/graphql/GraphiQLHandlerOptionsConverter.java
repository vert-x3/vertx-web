package io.vertx.ext.web.handler.graphql;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions} original class using Vert.x codegen.
 */
public class GraphiQLHandlerOptionsConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, GraphiQLHandlerOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "enabled":
          if (member.getValue() instanceof Boolean) {
            obj.setEnabled((Boolean)member.getValue());
          }
          break;
        case "graphQLUri":
          if (member.getValue() instanceof String) {
            obj.setGraphQLUri((String)member.getValue());
          }
          break;
        case "graphQLWSEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setGraphQLWSEnabled((Boolean)member.getValue());
          }
          break;
        case "graphQLWSUri":
          break;
        case "graphWSQLUri":
          if (member.getValue() instanceof String) {
            obj.setGraphWSQLUri((String)member.getValue());
          }
          break;
        case "headers":
          if (member.getValue() instanceof JsonObject) {
            java.util.Map<String, java.lang.String> map = new java.util.LinkedHashMap<>();
            ((Iterable<java.util.Map.Entry<String, Object>>)member.getValue()).forEach(entry -> {
              if (entry.getValue() instanceof String)
                map.put(entry.getKey(), (String)entry.getValue());
            });
            obj.setHeaders(map);
          }
          break;
        case "httpEnabled":
          if (member.getValue() instanceof Boolean) {
            obj.setHttpEnabled((Boolean)member.getValue());
          }
          break;
        case "query":
          if (member.getValue() instanceof String) {
            obj.setQuery((String)member.getValue());
          }
          break;
        case "variables":
          if (member.getValue() instanceof JsonObject) {
            obj.setVariables(((JsonObject)member.getValue()).copy());
          }
          break;
        case "wsConnectionParams":
          if (member.getValue() instanceof JsonObject) {
            obj.setWsConnectionParams(((JsonObject)member.getValue()).copy());
          }
          break;
      }
    }
  }

  public static void toJson(GraphiQLHandlerOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(GraphiQLHandlerOptions obj, java.util.Map<String, Object> json) {
    json.put("enabled", obj.isEnabled());
    if (obj.getGraphQLUri() != null) {
      json.put("graphQLUri", obj.getGraphQLUri());
    }
    json.put("graphQLWSEnabled", obj.isGraphQLWSEnabled());
    if (obj.getGraphQLWSUri() != null) {
      json.put("graphQLWSUri", obj.getGraphQLWSUri());
    }
    if (obj.getHeaders() != null) {
      JsonObject map = new JsonObject();
      obj.getHeaders().forEach((key, value) -> map.put(key, value));
      json.put("headers", map);
    }
    json.put("httpEnabled", obj.isHttpEnabled());
    if (obj.getQuery() != null) {
      json.put("query", obj.getQuery());
    }
    if (obj.getVariables() != null) {
      json.put("variables", obj.getVariables());
    }
    if (obj.getWsConnectionParams() != null) {
      json.put("wsConnectionParams", obj.getWsConnectionParams());
    }
  }
}
