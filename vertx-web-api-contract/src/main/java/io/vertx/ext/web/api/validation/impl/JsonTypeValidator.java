package io.vertx.ext.web.api.validation.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class JsonTypeValidator implements ParameterTypeValidator {

  Schema schema;

  public JsonTypeValidator(Schema schema) {
    this.schema = schema;
  }

  @Override
  public RequestParameter isValid(String value) throws ValidationException {
    try {
      schema.validate(new JSONObject(value));
      return RequestParameter.create(new JsonObject(value));
    } catch (org.everit.json.schema.ValidationException e) {
      throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException(e.toString());
    }
  }

  public static class JsonTypeValidatorFactory {
    public static JsonTypeValidator createJsonTypeValidator(JSONObject node) {
      return new JsonTypeValidator(SchemaLoader.load(node));
    }

    public static JsonTypeValidator createJsonTypeValidator(String object) {
      if (object.length() != 0) return createJsonTypeValidator(new JSONObject(object));
      else return null;
    }

    public static JsonTypeValidator createJsonTypeValidator(JsonObject object) {
      return createJsonTypeValidator(object.toString());
    }
  }

}
