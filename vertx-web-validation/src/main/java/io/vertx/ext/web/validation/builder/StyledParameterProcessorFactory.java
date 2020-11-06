package io.vertx.ext.web.validation.builder;

import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;
import io.vertx.json.schema.SchemaParser;

/**
 * This interface is used to build complex parameter processors supported only in cookie & query. <br/>
 *
 * Look at {@link Parameters} for all available factories
 */
@FunctionalInterface
public interface StyledParameterProcessorFactory {

  ParameterProcessor create(ParameterLocation location, SchemaParser parser);

}
