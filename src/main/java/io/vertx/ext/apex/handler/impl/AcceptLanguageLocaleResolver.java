package io.vertx.ext.apex.handler.impl;

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
  public String resolve(RoutingContext context) {
    return context.request().headers().get(HttpHeaders.ACCEPT_LANGUAGE);
  }

}
