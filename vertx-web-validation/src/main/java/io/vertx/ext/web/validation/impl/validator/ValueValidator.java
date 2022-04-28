package io.vertx.ext.web.validation.impl.validator;

import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.json.schema.validator.OutputUnit;
import io.vertx.json.schema.validator.impl.SchemaValidatorInternal;

import java.util.Objects;

/**
 * A value validator validates a Json value
 */
public class ValueValidator {

  private final SchemaValidatorInternal validator;

  public ValueValidator(SchemaValidatorInternal validator) {
    Objects.requireNonNull(validator, "'validator' cannot be null");
    this.validator = validator;
  }

  /**
   * Validate the provided {@code json}
   *
   * @param json
   * @return
   */
  public RequestParameter validate(Object json) {
    OutputUnit res = validator.validate(json);
    if (res.getValid()) {
      return RequestParameter.create(json);
    } else {
      // when the validation fails, there are a list of errors and annotations
      // while annotations are non-fatal we should use the list of errors to describe the failure?
      throw res.toException(json);
    }
  }

  /**
   * Get default value
   *
   * @return
   */
  public Object getDefault() {
    // TODO: this may fail for TRUE/FALSE schemas
    return validator
      .schema()
      .get("default");
  }
}
