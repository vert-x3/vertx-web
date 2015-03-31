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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.impl.LocaleHandlerImpl;

import java.util.Collection;
import java.util.Locale;

/**
 * A handler that sets the Locale of the RoutingContext based on the http header Accept-Language.
 * Note that this handler also sets the header 'Content-Language' of the response
 *
 * @author <a href="mailto://stephane.bastian.dev@gmail.com">Stephane Bastian</a>
 * 
 */
@VertxGen
public interface LocaleHandler extends Handler<RoutingContext> {

  /**
   * Create a handler
   *
   * @return the handler
   */
  static LocaleHandler create() {
    return new LocaleHandlerImpl();
  }

  /**
   * Create a handler with the specified locales handled by the application and a fallback Locale
   *
   * @param handledLocales a list of locale handled by the application
   * 
   * @return the handler
   */
  static LocaleHandler create(Collection<Locale> handledLocales) {
    return new LocaleHandlerImpl(handledLocales);
  }

}
