package io.vertx.ext.web.validation.builder;

import io.vertx.ext.json.schema.SchemaParser;
import io.vertx.ext.web.validation.impl.body.BodyProcessor;

/**
 * This interface is used to build body processors. <br/>
 *
 * Look at {@link Bodies} for all available factories.
 */
public interface BodyProcessorFactory {

  BodyProcessor create(SchemaParser parser);

}
