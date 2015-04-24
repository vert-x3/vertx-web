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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.LocaleHandler;
import io.vertx.ext.apex.handler.LocaleResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
    localeResolvers = new ArrayList<>();
    supportedLanguages = new LinkedHashSet<>();
  }
    
  /**
   * get the first resolved locale
   * 
   * @param context - the RoutingContext
   * @param resultHandler - the result handler
   */
  protected void getResolvedLocale(RoutingContext context, Handler<AsyncResult<String>> resultHandler) {
    Iterator<LocaleResolver> it = localeResolvers.iterator();
    while (it.hasNext()) {
      LocaleResolver localeResolver = it.next();
      localeResolver.resolve(context, lr -> { 
        if (!lr.failed() && lr.result()!=null) {
          resultHandler.handle(Future.succeededFuture(lr.result()));
        }
        else if (!it.hasNext()){
         // if this is the last element, return null
          resultHandler.handle(Future.succeededFuture());
        }
      });
    }
  }
  
  private List<LanguageRange> getLanguageRange(String languageTagOrLocale) {
    if (languageTagOrLocale!=null) {
      try {
        return LanguageRange.parse(languageTagOrLocale);
      }
      catch (Exception e) {
      // ignore parsing exception
      }
    // we may have received a locale separated by '_' instead of '-'
      languageTagOrLocale = languageTagOrLocale.replace('_', '-');
      try {
        return LanguageRange.parse(languageTagOrLocale);
      }
      catch (Exception e) {
      // ignore parsing exception
      }
    }
    return null;
  }
  
  /**
   * get a list of language range supported by the user
   * 
   * @param context - the RoutingContext
   * @param resultHandler - the result handler
   */
  protected void userLanguages(RoutingContext context, Handler<AsyncResult<List<LanguageRange>>> resultHandler) {
    getResolvedLocale(context, rl -> {
      resultHandler.handle(Future.succeededFuture(getLanguageRange(rl.result())));
    });
  }

  @Override
  public void handle(RoutingContext context) {
    userLanguages(context, ul-> {
      List<LanguageRange> userLanguages = ul.result();
      // by default, the locale is set to null
      String bestMatchingLanguage = null;
   
      if (userLanguages!=null && !supportedLanguages.isEmpty()) {
        // return the best matching locale or if there is no match, the first locale handled on the server
        bestMatchingLanguage = Locale.lookupTag(userLanguages, supportedLanguages);
        if(bestMatchingLanguage==null) {
          bestMatchingLanguage = supportedLanguages.iterator().next();
        }
      } else if (userLanguages!=null && supportedLanguages.isEmpty()) {
        // return the first locale accepted by the browser
        bestMatchingLanguage = userLanguages.get(0).getRange();
      }
      else if (userLanguages==null && !supportedLanguages.isEmpty()) {
        // return the first supported locale
        bestMatchingLanguage = supportedLanguages.iterator().next();
      }
      else if (userLanguages==null && supportedLanguages.isEmpty()) {
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
    });
  }

  @Override
  public LocaleHandler addResolver(LocaleResolver resolver) {
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
