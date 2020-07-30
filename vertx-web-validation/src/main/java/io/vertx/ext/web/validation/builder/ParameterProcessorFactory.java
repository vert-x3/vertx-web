package io.vertx.ext.web.validation.builder;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.web.validation.impl.ParameterLocation;
import io.vertx.ext.web.validation.impl.parameter.ParameterProcessor;

/**
 * This interface is used to build parameter processors supported on every {@link ParameterLocation}.
 * You can use in query and cookie more complex parameters with {@link StyledParameterProcessorFactory}. <br/>
 *
 * Look at {@link Parameters} for all available factories
 */
@FunctionalInterface
public interface ParameterProcessorFactory {

  ParameterProcessor create(ParameterLocation location, SchemaParser jsonSchemaParser);

}
