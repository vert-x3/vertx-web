package io.vertx.ext.web.openapi.impl;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.validation.impl.validator.SchemaValidator;

public interface SchemaHolder {

  JsonObject getOriginalSchema();

  JsonObject getNormalizedSchema();

  /**
   * The result of this function creates a schema with different semantics, so don't use it!
   * It's useful for ValueParser inference only
   *
   * @return
   */
  JsonObject getFakeSchema();

  JsonPointer getSchemaPointer();

  @GenIgnore
  SchemaValidator getValidator();

}
