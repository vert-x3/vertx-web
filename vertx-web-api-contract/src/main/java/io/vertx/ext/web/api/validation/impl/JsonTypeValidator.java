package io.vertx.ext.web.api.validation.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.validation.ParameterTypeValidator;
import io.vertx.ext.web.api.validation.ValidationException;

import java.io.IOException;
import java.util.Set;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
public class JsonTypeValidator implements ParameterTypeValidator {

  JsonSchema schema;

  public JsonTypeValidator(JsonSchema schema) {
    this.schema = schema;
  }

  @Override
  public RequestParameter isValid(String value) throws ValidationException {
    try {
      JsonNode node;
      if (value == null)
        throw ValidationException.ValidationExceptionFactory.generateNotParsableJsonBodyException("Json should not be null");
      else if (value.length() == 0)
        node = JsonNodeFactory.instance.textNode("");
      else
        node = Json.mapper.readTree(value);

      Set<ValidationMessage> errors = schema.validate(node);
      if (errors.size() == 0) {
        if (node.isArray())
          return RequestParameter.create(new JsonArray(value));
        else if (node.isObject())
          return RequestParameter.create(new JsonObject(value));
        else
          return RequestParameter.create(value);
      } else {
        ValidationMessage firstError = errors.iterator().next();
        throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException(firstError.getPath(), value, firstError.getMessage());
      }
    } catch (IOException e) {
      throw ValidationException.ValidationExceptionFactory.generateNotParsableJsonBodyException(e.getMessage());
    }
  }

  public static class JsonTypeValidatorFactory {

    public static JsonTypeValidator createJsonTypeValidator(JsonNode schema) {
      return new JsonTypeValidator(JsonSchemaFactory.getInstance().getSchema(schema));
    }

    public static JsonTypeValidator createJsonTypeValidator(String schema) {
      if (schema.length() != 0) {
        try {
          return createJsonTypeValidator(Json.mapper.readTree(schema));
        } catch (IOException e) {
          throw new IllegalArgumentException("schema provided is invalid: " + e);
        }
      }
      else return null;
    }
  }

}
