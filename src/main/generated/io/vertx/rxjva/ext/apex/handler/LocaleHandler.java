/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.rxjava.ext.apex.handler;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.ext.apex.RoutingContext;
import io.vertx.core.Handler;

/**
 * A handler that sets the locale of the RoutingContext based on the specified locale resolvers 
 * and an optional list of locales supported by your application
 * Note that the value of the Locale set on the context is a <a href="http://en.wikipedia.org/wiki/IETF_language_tag">IETF Language Tag</a> 
 * (uses hyphen instead of underscore, ie: fr-fr or en-gb instead of fr_FR, en_GB). 
 * To parse the Locale to a Java Locale use Locale.forLanguageTag(rc.getLocale());
 * 
 * A typical application will setup several locale resolvers sorted by priority:
 *  - A resolver based on the current user of the application 
 *      (your user's locale is application specific and usually retrieved from the app db) 
 *  - A resolver based on an ip address
 *  - A resolver based on the Accept-Language header of the current request
 *  - A resolver based on a specified default fallback locale in case nothing matches
 *  
 *  The LocaleHandler will simply loop through each resolver and find the best matching locale supported by your app, if any
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.apex.handler.LocaleHandler original} non RX-ified interface using Vert.x codegen.
 */

public class LocaleHandler implements Handler<RoutingContext> {

  final io.vertx.ext.apex.handler.LocaleHandler delegate;

  public LocaleHandler(io.vertx.ext.apex.handler.LocaleHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void handle(RoutingContext arg0) { 
    this.delegate.handle((io.vertx.ext.apex.RoutingContext) arg0.getDelegate());
  }

  /**
   * Add the specified resolver to the locale Handler.
   * Note that resolvers are evaluated based on insertion order
   * @param resolver 
   * @return 
   */
  public LocaleHandler addResolver(LocaleResolver resolver) { 
    this.delegate.addResolver((io.vertx.ext.apex.handler.LocaleResolver) resolver.getDelegate());
    return this;
  }

  /**
   * Add the specified locale as being supported by your application.
   * This is used to find the best matching locale between locales supported by a user and locales supported by your application 
   * For convenience, the format of the speficied locale can either be a valid Locale (ie: fr_FR) or a valid LanguageTag (ie: fr-fr)
   * @param locale 
   * @return 
   */
  public LocaleHandler addSupportedLocale(String locale) { 
    this.delegate.addSupportedLocale(locale);
    return this;
  }

  /**
   * Create a handler
   * @return the handler
   */
  public static LocaleHandler create() { 
    LocaleHandler ret= LocaleHandler.newInstance(io.vertx.ext.apex.handler.LocaleHandler.create());
    return ret;
  }


  public static LocaleHandler newInstance(io.vertx.ext.apex.handler.LocaleHandler arg) {
    return new LocaleHandler(arg);
  }
}
