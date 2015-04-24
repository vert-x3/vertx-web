package io.vertx.ext.apex.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.impl.AcceptLanguageLocaleResolver;
import io.vertx.ext.apex.handler.impl.FallbackLocaleResolver;

/**
 * A LocaleResoler resolves the locale for the current request.
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 *
 */
@VertxGen
public interface LocaleResolver {

  /**
   * Returns the locale to use for the current request 
   * Note that the value returned can contain multiple locales or languages and accept any values supported by the ACCEPT_LANGUAGE header (ie: * da, en-gb;q=0.8, en;q=0.7)
   * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.4">See W3c Specification</a>
   * 
   * @param context - the RoutingContext
   * @param resultHandler - the result handler
   * 
   */
  void resolve(RoutingContext context, Handler<AsyncResult<String>> resultHandler);
  
  static LocaleResolver acceptLanguageHeaderResolver() {
    return new AcceptLanguageLocaleResolver();
  }

  static LocaleResolver fallbackResolver(String locale) {
    return new FallbackLocaleResolver(locale);
  }

}
