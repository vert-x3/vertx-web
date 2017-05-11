package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.impl.HTTPRequestValidationHandlerImpl;

import java.util.regex.Pattern;

/**
 * Base interface for validation.
 * For basic HTTP Request Validator, use {@link HTTPRequestValidationHandler}
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen(concrete = false)
public interface ValidationHandler extends Handler<RoutingContext> {

}
