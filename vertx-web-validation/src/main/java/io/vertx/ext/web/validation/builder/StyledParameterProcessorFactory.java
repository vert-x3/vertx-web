package io.vertx.ext.web.validation.builder;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.json.schema.SchemaRepository;

/**
 * This interface is used to build complex parameter processors supported only in cookie & query. <br/>
 *
 * Look at {@link Parameters} for all available factories
 */
@VertxGen
@FunctionalInterface
public interface StyledParameterProcessorFactory {

  ParameterProcessor create(ParameterLocation location, SchemaRepository repository);
}
