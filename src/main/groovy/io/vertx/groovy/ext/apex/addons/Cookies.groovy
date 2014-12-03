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

package io.vertx.groovy.ext.apex.addons;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.core.RoutingContext
import java.util.Set
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class Cookies {
  final def io.vertx.ext.apex.addons.Cookies delegate;
  public Cookies(io.vertx.ext.apex.addons.Cookies delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static Cookies cookies() {
    def ret= Cookies.FACTORY.apply(io.vertx.ext.apex.addons.Cookies.cookies());
    return ret;
  }
  public static Cookie getCookie(String name) {
    def ret= Cookie.FACTORY.apply(io.vertx.ext.apex.addons.Cookies.getCookie(name));
    return ret;
  }
  public static void addCookie(Cookie cookie) {
    io.vertx.ext.apex.addons.Cookies.addCookie((io.vertx.ext.apex.addons.Cookie)cookie.getDelegate());
  }
  public static Cookie removeCookie(String name) {
    def ret= Cookie.FACTORY.apply(io.vertx.ext.apex.addons.Cookies.removeCookie(name));
    return ret;
  }
  public static Set<String> cookiesNames() {
    def ret = io.vertx.ext.apex.addons.Cookies.cookiesNames();
    return ret;
  }
  public static int cookieCount() {
    def ret = io.vertx.ext.apex.addons.Cookies.cookieCount();
    return ret;
  }
  public void handle(RoutingContext event) {
    this.delegate.handle((io.vertx.ext.apex.core.RoutingContext)event.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.Cookies, Cookies> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.Cookies arg -> new Cookies(arg);
  };
}
