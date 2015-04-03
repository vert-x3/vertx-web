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

package io.vertx.ext.apex.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.impl.LocaleHandlerImpl;

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
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 * 
 */
@VertxGen
public interface LocaleHandler extends Handler<RoutingContext> {

  /**
   * Add the specified resolver to the locale Handler.
   * Note that resolvers are evaluated based on insertion order
   * 
   * @param resolver
   * @return
   */
  @Fluent
  LocaleHandler addResolver(LocaleResolver resolver);

  /**
   * Add the specified locale as being supported by your application.
   * This is used to find the best matching locale between locales supported by a user and locales supported by your application 
   * For convenience, the format of the speficied locale can either be a valid Locale (ie: fr_FR) or a valid LanguageTag (ie: fr-fr)
   * 
   * @param locale
   * @return
   */
  @Fluent
  LocaleHandler addSupportedLocale(String locale);

  /**
   * Create a handler
   *
   * @param localeResolvers a list of locale resolvers
   * @return the handler
   */
  static LocaleHandler create() {
    return new LocaleHandlerImpl();
  }

}
