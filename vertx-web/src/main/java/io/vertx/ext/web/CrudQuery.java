package io.vertx.ext.web;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class CrudQuery {
  private JsonObject query;
  private Integer start;
  private Integer end;
  private JsonObject sort;

  public CrudQuery() {}

  public CrudQuery(JsonObject json) {
    CrudQueryConverter.fromJson(json, this);
  }

  public CrudQuery(CrudQuery other) {
    this.query = other.query;
    this.start = other.start;
    this.end = other.end;
    this.sort = other.sort;
  }

  public JsonObject getQuery() {
    return query;
  }

  public CrudQuery setQuery(JsonObject query) {
    this.query = query;
    return this;
  }

  public Integer getStart() {
    return start;
  }

  public CrudQuery setStart(Integer start) {
    this.start = start;
    return this;
  }

  public Integer getEnd() {
    return end;
  }

  public CrudQuery setEnd(Integer end) {
    this.end = end;
    return this;
  }

  public JsonObject getSort() {
    return sort;
  }

  public CrudQuery setSort(JsonObject sort) {
    this.sort = sort;
    return this;
  }

  public JsonObject toJson() {
    final JsonObject json = new JsonObject();
    CrudQueryConverter.toJson(this, json);
    return json;
  }
}
