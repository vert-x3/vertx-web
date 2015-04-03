/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.apex.handler.impl;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.LocaleHandler;
import io.vertx.ext.apex.handler.LocaleResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Objects;

/**
 * Default implementation of the interface {@link io.vertx.ext.apex.handler.LocaleHandler}
 *
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 * 
 */
public class LocaleHandlerImpl implements LocaleHandler {

  private Collection<LocaleResolver> localeResolvers;
  private Collection<String> supportedLanguages;

  public LocaleHandlerImpl() {
  }
    
  /**
   * get the first resolved locale
   * 
   * @param context
   * @return
   */
  protected String getResolvedLocale(RoutingContext context) {
    if (localeResolvers!=null) {
      for (LocaleResolver localeResolver: localeResolvers) {
        String resolvedLocale = localeResolver.resolve(context);
        if (resolvedLocale!=null) {
          return resolvedLocale;
        }
      }
    }
    return null;
  }
  
  /**
   * get a list of language range supported by the user
   * 
   * @param context
   * @return
   */
  protected List<LanguageRange> userLanguages(RoutingContext context) {
    String resolvedLocale = getResolvedLocale(context);
    if (resolvedLocale!=null) {
      try {
        return LanguageRange.parse(resolvedLocale);
      }
      catch (Exception e) {
      // ignore parsing exception
      }
    // we may have received a locale separated by '_' instead of '-'
      resolvedLocale = resolvedLocale.replace('_', '-');
      try {
        return LanguageRange.parse(resolvedLocale);
      }
      catch (Exception e) {
      // ignore parsing exception
      }
    }
    return null;
  }

  @Override
  public void handle(RoutingContext context) {
    // by default, the locale is set to null
    String bestMatchingLanguage = null;
    List<LanguageRange> userLanguages = userLanguages(context);
 
    if (userLanguages!=null && supportedLanguages!=null) {
      // return the best matching locale or if there is no match, the first locale handled on the server
      bestMatchingLanguage = Locale.lookupTag(userLanguages, supportedLanguages);
      if(bestMatchingLanguage==null) {
        bestMatchingLanguage = supportedLanguages.iterator().next();
      }
    } else if (userLanguages!=null && supportedLanguages==null) {
      // return the first locale accepted by the browser
      bestMatchingLanguage = userLanguages.get(0).getRange();
    }
    else if (userLanguages==null && supportedLanguages!=null) {
      // return the first supported locale
      bestMatchingLanguage = supportedLanguages.iterator().next();
    }
    else if (userLanguages==null && supportedLanguages==null) {
      // nothing to do, the computed locale is already null
    }
    
    if (bestMatchingLanguage!=null) {
      Locale bestMatchingLocale = Locale.forLanguageTag(bestMatchingLanguage);
      if (bestMatchingLocale!=null) {
        // we've got a valid Locale. lets convert it to a String
        String bestMatchingLocaleAsString = bestMatchingLocale.toLanguageTag();
        context.setLocale(bestMatchingLocaleAsString);
        if (bestMatchingLocaleAsString!=null) {
        // set the content-language header on the response
        	context.response().headers().add(HttpHeaders.CONTENT_LANGUAGE, bestMatchingLocaleAsString);
        }
      }
    }
    context.next();
  }

  @Override
  public LocaleHandler addResolver(LocaleResolver resolver) {
    if (localeResolvers==null) {
      localeResolvers = new ArrayList<>();
    }
    localeResolvers.add(resolver);
    return this;
  }

  @Override
  public LocaleHandler addSupportedLocale(String locale) {
    Objects.requireNonNull(locale);
    
    locale = toLanguageTag(locale);
    if (locale==null) {
      throw new IllegalArgumentException("invalid locale or language: " + locale);
    }
    if (supportedLanguages==null) {
      // use a linkedHashSet to preserve insertion order
      supportedLanguages = new LinkedHashSet<>();
    }
    supportedLanguages.add(locale);
    return this;
  }
  
  /**
   * Converts a locale, supporting both '_' or '-' as separator between the language and country code 
   * Returns a valid languageTag or null if the conversion failed
   * 
   * @param locale
   * @return
   */
  public final static String toLanguageTag(String locale) {
    Objects.requireNonNull(locale);
    
    locale = locale.replace('_', '-');
    // check that the locale is valid
    Locale validLocale = Locale.forLanguageTag(locale);
    if ("und".equals(validLocale.getLanguage())) {
      return null;
    }
    return validLocale.toLanguageTag();
  }
  
}
