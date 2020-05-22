package io.vertx.ext.web.openapi.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.validation.impl.validator.SchemaValidator;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SchemaHolderImpl implements SchemaHolder {

  private final JsonObject originalSchema;
  private final JsonPointer schemaPointer;

  private final Supplier<Map.Entry<JsonPointer, JsonObject>> normalizedSchemaSupplier;
  private JsonPointer normalizedSchemaPointer;
  private JsonObject normalizedSchema;
  private final JsonObject fakeSchema;
  private final BiFunction<JsonObject, JsonPointer, SchemaValidator> validatorFactory;
  private SchemaValidator validator;

  public SchemaHolderImpl(JsonObject originalSchema, JsonPointer schemaPointer, Supplier<Map.Entry<JsonPointer, JsonObject>> normalizedSchemaSupplier, JsonObject fakeSchema, BiFunction<JsonObject, JsonPointer, SchemaValidator> validatorFactory) {
    this.originalSchema = originalSchema;
    this.schemaPointer = schemaPointer;
    this.normalizedSchemaSupplier = normalizedSchemaSupplier;
    this.fakeSchema = fakeSchema;
    this.validatorFactory = validatorFactory;
  }

  @Override
  public JsonObject getOriginalSchema() {
    return originalSchema;
  }

  @Override
  public JsonPointer getSchemaPointer() {
    return schemaPointer;
  }

  @Override
  public JsonObject getNormalizedSchema() {
    if (normalizedSchema == null) {
      Map.Entry<JsonPointer, JsonObject> normalizedEntry = normalizedSchemaSupplier.get();
      this.normalizedSchema = normalizedEntry.getValue();
      this.normalizedSchemaPointer = normalizedEntry.getKey();
    }
    return normalizedSchema;
  }

  @Override
  public JsonObject getFakeSchema() {
    return fakeSchema;
  }

  @Override
  public SchemaValidator getValidator() {
    if (validator == null)
      validator = validatorFactory.apply(getNormalizedSchema(), this.normalizedSchemaPointer);
    return validator;
  }
}
