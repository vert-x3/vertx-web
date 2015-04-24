package io.vertx.ext.apex.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.LocaleResolver;

import java.util.Objects;

/**
 * This resolvers always returns the specified locale. it's used as a fallback resolver, hence its name, and is usually the last resolver in the list
 * 
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 *
 */
public class FallbackLocaleResolver implements LocaleResolver {

  private String locale;
  
  public FallbackLocaleResolver(String locale) {
    Objects.requireNonNull(locale);
    
    this.locale = LocaleHandlerImpl.toLanguageTag(locale);
  }
  
  @Override
  public void resolve(RoutingContext context, Handler<AsyncResult<String>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(locale));
  }
  
}
