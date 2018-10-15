package io.vertx.ext.web.api.router_factory_integration;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;

@DataObject(generateConverter = true, publicConverter = false)
public class FilterData {

  private List<String> from;
  private List<String> to;
  private List<String> message;

  public FilterData() {
    init();
  }

  public FilterData(JsonObject object) {
    init();
    FilterDataConverter.fromJson(object, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    FilterDataConverter.toJson(this, json);
    return json;
  }

  private void init() {
    this.from = new ArrayList<>();
    this.to = new ArrayList<>();
    this.message = new ArrayList<>();
  }

  public List<String> getFrom() {
    return from;
  }

  @Fluent public FilterData setFrom(List<String> from) {
    this.from = from;
    return this;
  }

  public List<String> getTo() {
    return to;
  }

  public void setTo(List<String> to) {
    this.to = to;
  }

  public List<String> getMessage() {
    return message;
  }

  public void setMessage(List<String> message) {
    this.message = message;
  }

  public static FilterData generate() {
    FilterData data = new FilterData();
    data.getFrom().add("bla@vertx.io");
    data.getFrom().add("hello@vertx.io");
    data.getTo().add("aaa@vertx.io");
    data.getMessage().add("aaa");
    data.getMessage().add("bbb");
    return data;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }
}
