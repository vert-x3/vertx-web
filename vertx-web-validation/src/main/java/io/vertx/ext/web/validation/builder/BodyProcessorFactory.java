package io.vertx.ext.web.validation.builder;

import io.vertx.ext.web.validation.impl.body.BodyProcessor;
import io.vertx.json.schema.SchemaParser;

/**
 * This interface is used to build body processors. <br/>
 *
 * Look at {@link Bodies} for all available factories.
 */
public interface BodyProcessorFactory {

  BodyProcessor create(SchemaParser parser);

}
