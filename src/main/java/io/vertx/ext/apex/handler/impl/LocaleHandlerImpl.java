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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Objects;

/**
 * Default implementation of the interface {@link io.vertx.ext.apex.handler.LocaleHandler}
 * This class could be extended to support different mechanisms to determine the locale (based on IP address, user's  preference, etc..)
 *
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 * 
 */
public class LocaleHandlerImpl implements LocaleHandler {

  private Collection<Locale> applicationLocales;
  
  public LocaleHandlerImpl() {
  }

  public LocaleHandlerImpl(Collection<Locale> handledLocales) {
    Objects.requireNonNull(handledLocales);
    if (handledLocales.isEmpty()) {
    	throw new IllegalArgumentException("handledLocales must contain at least one locale");
    }
    this.applicationLocales = handledLocales;
  }
  
  protected List<LanguageRange> userLanguages(RoutingContext context) {
    String acceptLanguage = context.request().headers().get(HttpHeaders.ACCEPT_LANGUAGE);
    if (acceptLanguage!= null) {
      try {
        return LanguageRange.parse(acceptLanguage);
      }
      catch (Exception e) {
      // ignore the parsing exception
      }
    }
    return null;
  }

  @Override
  public void handle(RoutingContext context) {
    // by default, the locale is set to null
    Locale computedLocale = null;
    List<LanguageRange> userLanguages = userLanguages(context);
 
    if (userLanguages!=null && applicationLocales!=null) {
      // return the best matching locale or if there is no match, the first locale handled on the server
      computedLocale = Locale.lookup(userLanguages, applicationLocales);
      if(computedLocale==null) {
        computedLocale = applicationLocales.iterator().next();
      }
    } else if (userLanguages!=null && applicationLocales==null) {
      // return the first locale accepted by the browser
      computedLocale = Locale.forLanguageTag(userLanguages.get(0).getRange());
    }
    else if (userLanguages==null && applicationLocales!=null) {
      // return the first supported locale
      computedLocale = applicationLocales.iterator().next();
    }
    else if (userLanguages==null && applicationLocales==null) {
      // nothing to do, the computed locale is already null
    }
    context.setLocale(computedLocale);
    if (computedLocale!=null) {
    // set the content-language header on the response
    	context.response().headers().add(HttpHeaders.CONTENT_LANGUAGE, computedLocale.toLanguageTag());
    }
    context.next();
  }
}
