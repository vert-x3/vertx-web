package io.vertx.ext.web.openapi.it.models;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true, publicConverter = false)
public class Transaction {
  private String id;

  private String message;

  private String from;

  private String to;

  private Double value;

  public String getId() {
    return id;
  }

  @Fluent
  public void setId(String id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  @Fluent
  public void setMessage(String message) {
    this.message = message;
  }

  public String getFrom() {
    return from;
  }

  @Fluent
  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  @Fluent
  public void setTo(String to) {
    this.to = to;
  }

  public Double getValue() {
    return value;
  }

  @Fluent
  public void setValue(Double value) {
    this.value = value;
  }

  public Transaction(String id, String message, String from, String to, Double value) {
    this.id = id;
    this.message = message;
    this.from = from;
    this.to = to;
    this.value = value;
  }

  public Transaction(JsonObject jsonObject) {
    TransactionConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    TransactionConverter.toJson(this, json);
    return json;
  }
}
