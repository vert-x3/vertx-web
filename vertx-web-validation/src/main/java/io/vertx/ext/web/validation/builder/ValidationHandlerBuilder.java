package io.vertx.ext.web.validation.builder;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.RequestPredicate;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.ext.web.validation.builder.impl.ValidationHandlerBuilderImpl;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.json.schema.SchemaRepository;

/**
 * Builder for a {@link ValidationHandler}. <br/>
 *
 * For more info look the docs
 */
@VertxGen
public interface ValidationHandlerBuilder {

  /**
   * Add a parameter given the location and the processor
   *
   * @param location
   * @param processor
   * @return
   */
  @Fluent
  /**
   * @TODO: leaky abstraction it relies on API internals as public API breaking the codegen contract
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  ValidationHandlerBuilder parameter(ParameterLocation location, ParameterProcessor processor);

  @Fluent
  ValidationHandlerBuilder queryParameter(StyledParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder queryParameter(ParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder pathParameter(ParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder cookieParameter(StyledParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder cookieParameter(ParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder headerParameter(ParameterProcessorFactory parameterProcessor);

  @Fluent
  ValidationHandlerBuilder body(BodyProcessorFactory bodyProcessor);

  @Fluent
  /**
   * @TODO: leaky abstraction it relies on API internals as public API breaking the codegen contract
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  ValidationHandlerBuilder body(BodyProcessor bodyProcessor);

  @Fluent
  ValidationHandlerBuilder predicate(RequestPredicate predicate);

  /**
   * Build the {@link ValidationHandler} from this builder
   *
   * @return
   */
  ValidationHandler build();

  static ValidationHandlerBuilder create(SchemaRepository repository) {
    return new ValidationHandlerBuilderImpl(repository);
  }

}
