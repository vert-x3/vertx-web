package io.vertx.ext.web.validation.impl.validator;

import io.vertx.core.Future;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.json.schema.SchemaException;
import io.vertx.json.schema.validator.OutputUnit;
import io.vertx.json.schema.validator.impl.SchemaValidatorInternal;

import java.util.Objects;

public class SchemaValidator implements ValueValidator {

  private final SchemaValidatorInternal validator;

  public SchemaValidator(io.vertx.json.schema.validator.SchemaValidator validator) {
    Objects.requireNonNull(validator, "'validator' cannot be null");
    this.validator = (SchemaValidatorInternal) validator;
  }

  @Override
  public Future<RequestParameter> validate(Object json) {
    try {
      OutputUnit res = validator.validate(json);
      if (res.getValid()) {
        return Future.succeededFuture(RequestParameter.create(json));
      } else {
        // when the validation fails, there are a list of errors and annotations
        // while annotations are non fatal we should use the list of errors to describe the failure?
        return Future.failedFuture(res.toException(json));
      }
    } catch (SchemaException e) {
      // schema exception is thrown when the resolved schemas reach an illegal point
      return Future.failedFuture(e);
    }
  }

  @Override
  public Future<Object> getDefault() {
    return Future.succeededFuture(
      validator.schema().get("default"));
  }
}
