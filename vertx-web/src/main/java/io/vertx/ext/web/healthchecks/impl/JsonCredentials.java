package io.vertx.ext.web.healthchecks.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;

public class JsonCredentials implements Credentials {

  private final JsonObject json;

  public JsonCredentials(JsonObject json) {
    this.json = json;
  }

  @Override
  public JsonObject toJson() {
    return json;
  }
}
