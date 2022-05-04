package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder;

/**
 * This is the entry point of this module. Provides the parsing, validation and puts the parsed objects into {@link RoutingContext}. <br/>
 *
 * You can easily build a new validation handler using a {@link ValidationHandlerBuilder}. <br/>
 *
 * For more info read the doc. <br/>
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface ValidationHandler extends Handler<RoutingContext> {

  String REQUEST_CONTEXT_KEY = "requestParameters";

  /**
   * Returns {@code true} if this handler requires a {@link io.vertx.ext.web.handler.BodyHandler} to be present in the
   * route.
   */
  boolean isBodyRequired();
}
