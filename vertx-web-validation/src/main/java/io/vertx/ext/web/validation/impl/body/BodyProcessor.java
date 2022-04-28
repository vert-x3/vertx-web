package io.vertx.ext.web.validation.impl.body;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameter;

/**
 * Entry point for managing request bodies
 */
@VertxGen
public interface BodyProcessor {

  boolean canProcess(String contentType);

  RequestParameter process(RoutingContext requestContext);
}
