package io.vertx.ext.web.validation.impl.body;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameter;

/**
 * Entry point for managing request bodies
 */
public interface BodyProcessor {

  boolean canProcess(String contentType);

  Future<RequestParameter> process(RoutingContext requestContext);

}
