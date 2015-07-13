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
import io.vertx.groovy.core.http.HttpServerRequest
import java.util.List
import io.vertx.core.http.HttpMethod
import io.vertx.groovy.core.Vertx
import io.vertx.core.Handler
/**
 * A router receives request from an {@link io.vertx.groovy.core.http.HttpServer} and routes it to the first matching
 * {@link io.vertx.groovy.ext.web.Route} that it contains. A router can contain many routes.
 * <p>
 * Routers are also used for routing failures.
*/
@CompileStatic
public class Router {
  private final def io.vertx.ext.web.Router delegate;
  public Router(Object delegate) {
    this.delegate = (io.vertx.ext.web.Router) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a router
   * @param vertx the Vert.x instance
   * @return the router
   */
  public static Router router(Vertx vertx) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.web.Router.router((io.vertx.core.Vertx)vertx.getDelegate()), io.vertx.groovy.ext.web.Router.class);
    return ret;
  }
  /**
   * This method is used to provide a request to the router. Usually you take request from the
   * {@link io.vertx.groovy.core.http.HttpServer#requestHandler} and pass it to this method. The
   * router then routes it to matching routes.
   * @param request the request
   */
  public void accept(HttpServerRequest request) {
    this.delegate.accept((io.vertx.core.http.HttpServerRequest)request.getDelegate());
  }
  /**
   * Add a route with no matching criteria, i.e. it matches all requests or failures.
   * @return the route
   */
  public Route route() {
    def ret= InternalHelper.safeCreate(this.delegate.route(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches the specified HTTP method and path
   * @param method the HTTP method to match
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route route(HttpMethod method, String path) {
    def ret= InternalHelper.safeCreate(this.delegate.route(method, path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route route(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.route(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches the specified HTTP method and path regex
   * @param method the HTTP method to match
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route routeWithRegex(HttpMethod method, String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.routeWithRegex(method, regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route routeWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.routeWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches any HTTP GET request
   * @return the route
   */
  public Route get() {
    def ret= InternalHelper.safeCreate(this.delegate.get(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP GET request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route get(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.get(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP GET request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route getWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.getWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches any HTTP HEAD request
   * @return the route
   */
  public Route head() {
    def ret= InternalHelper.safeCreate(this.delegate.head(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP HEAD request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route head(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.head(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP HEAD request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route headWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.headWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches any HTTP OPTIONS request
   * @return the route
   */
  public Route options() {
    def ret= InternalHelper.safeCreate(this.delegate.options(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP OPTIONS request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route options(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.options(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP OPTIONS request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route optionsWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.optionsWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches any HTTP PUT request
   * @return the route
   */
  public Route put() {
    def ret= InternalHelper.safeCreate(this.delegate.put(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP PUT request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route put(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.put(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP PUT request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route putWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.putWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches any HTTP POST request
   * @return the route
   */
  public Route post() {
    def ret= InternalHelper.safeCreate(this.delegate.post(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP POST request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route post(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.post(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP POST request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route postWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.postWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches any HTTP DELETE request
   * @return the route
   */
  public Route delete() {
    def ret= InternalHelper.safeCreate(this.delegate.delete(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP DELETE request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route delete(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.delete(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP DELETE request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route deleteWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.deleteWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches any HTTP TRACE request
   * @return the route
   */
  public Route trace() {
    def ret= InternalHelper.safeCreate(this.delegate.trace(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP TRACE request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route trace(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.trace(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP TRACE request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route traceWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.traceWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches any HTTP CONNECT request
   * @return the route
   */
  public Route connect() {
    def ret= InternalHelper.safeCreate(this.delegate.connect(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP CONNECT request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route connect(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.connect(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP CONNECT request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route connectWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.connectWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches any HTTP PATCH request
   * @return the route
   */
  public Route patch() {
    def ret= InternalHelper.safeCreate(this.delegate.patch(), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP PATCH request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route patch(String path) {
    def ret= InternalHelper.safeCreate(this.delegate.patch(path), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * Add a route that matches a HTTP PATCH request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route patchWithRegex(String regex) {
    def ret= InternalHelper.safeCreate(this.delegate.patchWithRegex(regex), io.vertx.groovy.ext.web.Route.class);
    return ret;
  }
  /**
   * @return a list of all the routes on this router
   * @return 
   */
  public List<Route> getRoutes() {
    def ret = this.delegate.getRoutes()?.collect({underpants -> new io.vertx.groovy.ext.web.Route(underpants)});
      return ret;
  }
  /**
   * Remove all the routes from this router
   * @return a reference to this, so the API can be used fluently
   */
  public Router clear() {
    this.delegate.clear();
    return this;
  }
  /**
   * Mount a sub router on this router
   * @param mountPoint the mount point (path prefix) to mount it on
   * @param subRouter the router to mount as a sub router
   * @return a reference to this, so the API can be used fluently
   */
  public Router mountSubRouter(String mountPoint, Router subRouter) {
    this.delegate.mountSubRouter(mountPoint, (io.vertx.ext.web.Router)subRouter.getDelegate());
    return this;
  }
  /**
   * Specify a handler for any unhandled exceptions on this router. The handler will be called for exceptions thrown
   * from handlers. This does not affect the normal failure routing logic.
   * @param exceptionHandler the exception handler
   * @return a reference to this, so the API can be used fluently
   */
  public Router exceptionHandler(Handler<Throwable> exceptionHandler) {
    this.delegate.exceptionHandler(exceptionHandler);
    return this;
  }
  /**
   * Used to route a context to the router. Used for sub-routers. You wouldn't normally call this method directly.
   * @param context the routing context
   */
  public void handleContext(RoutingContext context) {
    this.delegate.handleContext((io.vertx.ext.web.RoutingContext)context.getDelegate());
  }
  /**
   * Used to route a failure to the router. Used for sub-routers. You wouldn't normally call this method directly.
   * @param context the routing context
   */
  public void handleFailure(RoutingContext context) {
    this.delegate.handleFailure((io.vertx.ext.web.RoutingContext)context.getDelegate());
  }
}
