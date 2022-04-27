package io.vertx.ext.web.validation.builder;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.json.schema.validator.SchemaRepository;

/**
 * This interface is used to build body processors. <br/>
 *
 * Look at {@link Bodies} for all available factories.
 */
@VertxGen
@FunctionalInterface
public interface BodyProcessorFactory {

  BodyProcessor create(SchemaRepository parser);
}
