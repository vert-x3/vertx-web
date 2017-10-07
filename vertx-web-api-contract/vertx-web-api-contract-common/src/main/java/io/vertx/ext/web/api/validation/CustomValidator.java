package io.vertx.ext.web.api.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.RoutingContext;

/**
 * This interface is used to add custom <b>synchronous</b> functions inside validation process. You can add it in
 * {@link HTTPRequestValidationHandler}.
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface CustomValidator {
  /**
   * This function have to be <b>synchronous</b>. It doesn't return nothing if validation succedes, otherwise it
   * throws ValidationException.
   * <br/>
   * <b>Don't call routingContext.next() or routingContext.fail() from this function</b>
   *
   * @param routingContext the actual routing context
   * @throws ValidationException
   */
  void validate(RoutingContext routingContext) throws ValidationException;
}
