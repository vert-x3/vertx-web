package io.vertx.ext.web.api.validation.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.contract.openapi3.impl.OpenApi3Utils;
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
      JSONObject obj = new JSONObject(value);
      schema.validate(obj);
      // We can't simply reparse value because the validation can modify things inside obj
      // (for example when we apply default values)
      return RequestParameter.create(OpenApi3Utils.convertOrgJSONToVertxJSON(obj));
    } catch (org.everit.json.schema.ValidationException e) {
      throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException(e.toString());
    }
  }

  public static class JsonTypeValidatorFactory {

    // TODO document and hide useless methods
    public static JsonTypeValidator createJsonTypeValidator(JSONObject schema) {
      return new JsonTypeValidator(SchemaLoader.load(schema));
    }

    public static JsonTypeValidator createJsonTypeValidator(String schema) {
      if (schema.length() != 0) return createJsonTypeValidator(new JSONObject(schema));
      else return null;
    }

    public static JsonTypeValidator createJsonTypeValidator(JsonObject schema) {
      return createJsonTypeValidator((JSONObject) OpenApi3Utils.convertVertxJSONToOrgJSON(schema));
    }
  }

}
