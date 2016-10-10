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

package io.vertx.groovy.ext.web;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.http.HttpServerRequest
import java.util.List
import io.vertx.core.http.HttpMethod
import io.vertx.groovy.core.Vertx
import io.vertx.core.Handler
@CompileStatic
public class Router {
  private final def io.vertx.ext.web.Router delegate;
  public Router(Object delegate) {
    this.delegate = (io.vertx.ext.web.Router) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static Router router(Vertx vertx) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.Router.router(vertx != null ? (io.vertx.core.Vertx)vertx.getDelegate() : null), io.vertx.groovy.ext.web.Router.class);
    return ret;
  }
  public void accept(HttpServerRequest arg0) {
    delegate.accept(arg0 != null ? (io.vertx.core.http.HttpServerRequest)arg0.getDelegate() : null);
  }
  public Route route() {
    def ret = InternalHelper.safeCreate(delegate.route(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route route(HttpMethod arg0, String arg1) {
    def ret = InternalHelper.safeCreate(delegate.route(arg0, arg1), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route route(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.route(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route routeWithRegex(HttpMethod arg0, String arg1) {
    def ret = InternalHelper.safeCreate(delegate.routeWithRegex(arg0, arg1), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route routeWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.routeWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route get() {
    def ret = InternalHelper.safeCreate(delegate.get(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route get(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.get(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route getWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.getWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route head() {
    def ret = InternalHelper.safeCreate(delegate.head(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route head(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.head(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route headWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.headWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route options() {
    def ret = InternalHelper.safeCreate(delegate.options(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route options(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.options(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route optionsWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.optionsWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route put() {
    def ret = InternalHelper.safeCreate(delegate.put(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route put(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.put(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route putWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.putWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route post() {
    def ret = InternalHelper.safeCreate(delegate.post(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route post(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.post(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route postWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.postWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route delete() {
    def ret = InternalHelper.safeCreate(delegate.delete(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route delete(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.delete(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route deleteWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.deleteWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route trace() {
    def ret = InternalHelper.safeCreate(delegate.trace(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route trace(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.trace(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route traceWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.traceWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route connect() {
    def ret = InternalHelper.safeCreate(delegate.connect(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route connect(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.connect(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route connectWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.connectWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route patch() {
    def ret = InternalHelper.safeCreate(delegate.patch(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route patch(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.patch(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public Route patchWithRegex(String arg0) {
    def ret = InternalHelper.safeCreate(delegate.patchWithRegex(arg0), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  public List<Route> getRoutes() {
    def ret = (List)delegate.getRoutes()?.collect({InternalHelper.safeCreate(it, io.vertx.groovy.ext.web.Route.class)});
    return ret;
  }
  public Router clear() {
    delegate.clear();
    return this;
  }
  public Router mountSubRouter(String arg0, Router arg1) {
    delegate.mountSubRouter(arg0, arg1 != null ? (io.vertx.ext.web.Router)arg1.getDelegate() : null);
    return this;
  }
  public Router exceptionHandler(Handler<Throwable> arg0) {
    delegate.exceptionHandler(arg0);
    return this;
  }
  public void handleContext(RoutingContext arg0) {
    delegate.handleContext(arg0 != null ? (io.vertx.ext.web.RoutingContext)arg0.getDelegate() : null);
  }
  public void handleFailure(RoutingContext arg0) {
    delegate.handleFailure(arg0 != null ? (io.vertx.ext.web.RoutingContext)arg0.getDelegate() : null);
  }
}
