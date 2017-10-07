package io.vertx.ext.web.validation.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.validation.ParameterTypeValidator;
import io.vertx.ext.web.validation.ValidationException;

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
      Set<ValidationMessage> errors = schema.validate(new ObjectMapper().readTree(value));
      if (!errors.isEmpty())
        throw ValidationException.ValidationExceptionFactory.generateInvalidJsonBodyException(errors.toString());
      return RequestParameter.create(new JsonObject(value));
    } catch (IOException e) {
      throw ValidationException.ValidationExceptionFactory.generateNotParsableJsonBodyException();
    }
  }

  public static class JsonTypeValidatorFactory {
    public static JsonTypeValidator createJsonTypeValidator(JsonNode node) {
      return new JsonTypeValidator(new JsonSchemaFactory().getSchema(node));
    }

    public static JsonTypeValidator createJsonTypeValidator(String object) {
      if (object.length() != 0) return createJsonTypeValidator(Utils.toJsonNode(object));
      else return null;
    }

    public static JsonTypeValidator createJsonTypeValidator(JsonObject object) {
      return createJsonTypeValidator(Utils.toJsonNode(object));
    }
  }

}
