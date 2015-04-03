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

/**
 * A LocaleResoler resolves the locale for the current request.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.apex.handler.LocaleResolver original} non RX-ified interface using Vert.x codegen.
 */

public class LocaleResolver {

  final io.vertx.ext.apex.handler.LocaleResolver delegate;

  public LocaleResolver(io.vertx.ext.apex.handler.LocaleResolver delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Returns the locale to use for the current request 
   * Note that the value returned can contain multiple locales or languages and accept any values supported by the ACCEPT_LANGUAGE header (ie: * da, en-gb;q=0.8, en;q=0.7)
   * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.4">See W3c Specification</a>
   * @param context 
   * @return 
   */
  public String resolve(RoutingContext context) { 
    String ret = this.delegate.resolve((io.vertx.ext.apex.RoutingContext) context.getDelegate());
    return ret;
  }

  public static LocaleResolver acceptLanguageHeaderResolver() { 
    LocaleResolver ret= LocaleResolver.newInstance(io.vertx.ext.apex.handler.LocaleResolver.acceptLanguageHeaderResolver());
    return ret;
  }

  public static LocaleResolver fallbackResolver(String locale) { 
    LocaleResolver ret= LocaleResolver.newInstance(io.vertx.ext.apex.handler.LocaleResolver.fallbackResolver(locale));
    return ret;
  }


  public static LocaleResolver newInstance(io.vertx.ext.apex.handler.LocaleResolver arg) {
    return new LocaleResolver(arg);
  }
}
