package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.impl.JsonPointerIteratorImpl;
import io.vertx.ext.web.openapi.OpenAPIHolder;

public class JsonPointerIteratorWithLoader extends JsonPointerIteratorImpl {
  private final OpenAPIHolder loader;

  public JsonPointerIteratorWithLoader(OpenAPIHolder loader) {
    super();
    this.loader = loader;
  }

  @Override
  public boolean objectContainsKey(Object value, String key) {
    if (value instanceof JsonObject)
      value = loader.solveIfNeeded((JsonObject) value);
    return super.objectContainsKey(value, key);
  }

  @Override
  public Object getObjectParameter(Object value, String key, boolean createOnMissing) {
    if (value instanceof JsonObject)
      value = loader.solveIfNeeded((JsonObject) value);
    return super.getObjectParameter(value, key, createOnMissing);
  }

}
