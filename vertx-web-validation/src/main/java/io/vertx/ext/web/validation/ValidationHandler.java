package io.vertx.ext.web.validation;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder;
import io.vertx.json.schema.SchemaParser;

/**
 * This is the entry point of this module. Provides the parsing, validation and puts the parsed objects into {@link RoutingContext}. <br/>
 *
 * You can easily build a new validation handler using a {@link ValidationHandlerBuilder}, that you can create with {@link this#builder(SchemaParser)}. <br/>
 *
 * For more info read the doc. <br/>
 *
 * @author Francesco Guardiani @slinkydeveloper
 */
@VertxGen
public interface ValidationHandler extends Handler<RoutingContext> {

  String REQUEST_CONTEXT_KEY = "requestParameters";

  @GenIgnore
  static ValidationHandlerBuilder builder(SchemaParser parser) {
    return ValidationHandlerBuilder.create(parser);
  }

}
