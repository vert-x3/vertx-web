package io.vertx.ext.web.validation.impl.validator;

import io.vertx.core.Future;
import io.vertx.ext.web.validation.RequestParameter;

/**
 * A value validator asynchronously validates a Json value
 */
public interface ValueValidator {

  /**
   * Validate the provided {@code json}
   *
   * @param json
   * @return
   */
  Future<RequestParameter> validate(Object json);

  /**
   * Get default value
   *
   * @return
   */
  Future<Object> getDefault();

}
