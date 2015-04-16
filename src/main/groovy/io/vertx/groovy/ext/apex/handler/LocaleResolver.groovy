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

package io.vertx.groovy.ext.apex.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.RoutingContext
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * A LocaleResoler resolves the locale for the current request.
*/
@CompileStatic
public class LocaleResolver {
  final def io.vertx.ext.apex.handler.LocaleResolver delegate;
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
   * @param context - the RoutingContext
   * @param resultHandler - the result handler
   */
  public void resolve(RoutingContext context, Handler<AsyncResult<String>> resultHandler) {
    this.delegate.resolve((io.vertx.ext.apex.RoutingContext)context.getDelegate(), resultHandler);
  }
  public static LocaleResolver acceptLanguageHeaderResolver() {
    def ret= new io.vertx.groovy.ext.apex.handler.LocaleResolver(io.vertx.ext.apex.handler.LocaleResolver.acceptLanguageHeaderResolver());
    return ret;
  }
  public static LocaleResolver fallbackResolver(String locale) {
    def ret= new io.vertx.groovy.ext.apex.handler.LocaleResolver(io.vertx.ext.apex.handler.LocaleResolver.fallbackResolver(locale));
    return ret;
  }
}
