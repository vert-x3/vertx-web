package io.vertx.ext.apex.handler.impl;

import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.LocaleResolver;

import java.util.Objects;

/**
 * Resolves locales based on the specified fallback value
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
  public String resolve(RoutingContext context) {
    return locale;
  }

}
