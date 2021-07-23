package io.vertx.ext.web.validation.builder;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.json.schema.SchemaParser;

/**
 * This interface is used to build body processors. <br/>
 *
 * Look at {@link Bodies} for all available factories.
 */
@VertxGen
public interface BodyProcessorFactory {

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  BodyProcessor create(SchemaParser parser);

}
