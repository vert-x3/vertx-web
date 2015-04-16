package io.vertx.ext.apex.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.LocaleResolver;

/**
 * Resolves locales based on the accept-language header of the current request
 * 
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 *
 */
public class AcceptLanguageLocaleResolver implements LocaleResolver {

  @Override
  public void resolve(RoutingContext context, Handler<AsyncResult<String>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(context.request().headers().get(HttpHeaders.ACCEPT_LANGUAGE)));
  }

}
