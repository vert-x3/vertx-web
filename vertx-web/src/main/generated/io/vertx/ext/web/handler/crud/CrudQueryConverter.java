package io.vertx.ext.web.handler.crud;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.ext.web.handler.crud.CrudQuery}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.crud.CrudQuery} original class using Vert.x codegen.
 */
public class CrudQueryConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, CrudQuery obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "end":
          if (member.getValue() instanceof Number) {
            obj.setEnd(((Number)member.getValue()).intValue());
          }
          break;
        case "query":
          if (member.getValue() instanceof JsonObject) {
            obj.setQuery(((JsonObject)member.getValue()).copy());
          }
          break;
        case "sort":
          if (member.getValue() instanceof JsonObject) {
            obj.setSort(((JsonObject)member.getValue()).copy());
          }
          break;
        case "start":
          if (member.getValue() instanceof Number) {
            obj.setStart(((Number)member.getValue()).intValue());
          }
          break;
      }
    }
  }

  public static void toJson(CrudQuery obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(CrudQuery obj, java.util.Map<String, Object> json) {
    if (obj.getEnd() != null) {
      json.put("end", obj.getEnd());
    }
    if (obj.getQuery() != null) {
      json.put("query", obj.getQuery());
    }
    if (obj.getSort() != null) {
      json.put("sort", obj.getSort());
    }
    if (obj.getStart() != null) {
      json.put("start", obj.getStart());
    }
  }
}
