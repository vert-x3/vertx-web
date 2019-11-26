package io.vertx.ext.web.api;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true, publicConverter = false)
public class OperationCookie {

  private String name;
  private String value;
  private String domain;
  private String path;

  public OperationCookie(JsonObject json) {
    OperationCookieConverter.fromJson(json, this);
  }

  public OperationCookie(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    OperationCookieConverter.toJson(this, json);
    return json;
  }

  public String getName() {
    return name;
  }

  OperationCookie setName(String name) {
    this.name = name;
    return this;
  }

  public String getValue() {
    return value;
  }

  public OperationCookie setValue(String value) {
    this.value = value;
    return this;
  }

  public String getDomain() {
    return domain;
  }

  public OperationCookie setDomain(String domain) {
    this.domain = domain;
    return this;
  }

  public String getPath() {
    return path;
  }

  public OperationCookie setPath(String path) {
    this.path = path;
    return this;
  }
}
