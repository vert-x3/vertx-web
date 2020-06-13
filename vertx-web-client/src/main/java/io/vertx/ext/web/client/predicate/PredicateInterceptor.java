package io.vertx.ext.web.client.predicate;

import io.vertx.ext.web.client.impl.HttpContext;

public interface PredicateInterceptor {

  boolean handle(HttpContext<?> context);

}
