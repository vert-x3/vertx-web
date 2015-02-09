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

package io.vertx.groovy.ext.apex;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.core.http.HttpServerRequest
import java.util.List
import io.vertx.core.http.HttpMethod
import io.vertx.groovy.core.Vertx
import io.vertx.core.Handler
/**
 * A router receives request from an {@link io.vertx.core.http.HttpServer} and routes it to the first matching
 * {@link io.vertx.ext.apex.Route} that it contains. A router can contain many routes.
 * <p>
 * Routers are also used for routing failures.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class Router {
  final def io.vertx.ext.apex.Router delegate;
  public Router(io.vertx.ext.apex.Router delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a router
   *
   * @param vertx  the Vert.x instance
   * @return the router
   */
  public static Router router(Vertx vertx) {
    def ret= Router.FACTORY.apply(io.vertx.ext.apex.Router.router((io.vertx.core.Vertx)vertx.getDelegate()));
    return ret;
  }
  /**
   * This method is used to provide a request to the router. Usually you take request from the
   * {@link io.vertx.core.http.HttpServer#requestHandler(io.vertx.core.Handler)} and pass it to this method. The
   * router then routes it to matching routes.
   *
   * @param request  the request
   */
  public void accept(HttpServerRequest request) {
    this.delegate.accept((io.vertx.core.http.HttpServerRequest)request.getDelegate());
  }
  /**
   * Add a route with no matching criteria, i.e. it matches all requests or failures.
   *
   * @return  the route
   */
  public Route route() {
    def ret= Route.FACTORY.apply(this.delegate.route());
    return ret;
  }
  /**
   * Add a route that matches the specified HTTP method and path
   *
   * @param method  the HTTP method to match
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  public Route route(HttpMethod method, String path) {
    def ret= Route.FACTORY.apply(this.delegate.route(method, path));
    return ret;
  }
  /**
   * Add a route that matches the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  public Route route(String path) {
    def ret= Route.FACTORY.apply(this.delegate.route(path));
    return ret;
  }
  /**
   * Add a route that matches the specified HTTP method and path regex
   *
   * @param method  the HTTP method to match
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  public Route routeWithRegex(HttpMethod method, String regex) {
    def ret= Route.FACTORY.apply(this.delegate.routeWithRegex(method, regex));
    return ret;
  }
  /**
   * Add a route that matches the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  public Route routeWithRegex(String regex) {
    def ret= Route.FACTORY.apply(this.delegate.routeWithRegex(regex));
    return ret;
  }
  /**
   * Add a route that matches any HTTP GET request
   *
   * @return the route
   */
  public Route get() {
    def ret= Route.FACTORY.apply(this.delegate.get());
    return ret;
  }
  /**
   * Add a route that matches a HTTP GET request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  public Route get(String path) {
    def ret= Route.FACTORY.apply(this.delegate.get(path));
    return ret;
  }
  /**
   * Add a route that matches a HTTP GET request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  public Route getWithRegex(String regex) {
    def ret= Route.FACTORY.apply(this.delegate.getWithRegex(regex));
    return ret;
  }
  /**
   * Add a route that matches any HTTP HEAD request
   *
   * @return the route
   */
  public Route head() {
    def ret= Route.FACTORY.apply(this.delegate.head());
    return ret;
  }
  /**
   * Add a route that matches a HTTP HEAD request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  public Route head(String path) {
    def ret= Route.FACTORY.apply(this.delegate.head(path));
    return ret;
  }
  /**
   * Add a route that matches a HTTP HEAD request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  public Route headWithRegex(String regex) {
    def ret= Route.FACTORY.apply(this.delegate.headWithRegex(regex));
    return ret;
  }
  /**
   * Add a route that matches any HTTP OPTIONS request
   *
   * @return the route
   */
  public Route options() {
    def ret= Route.FACTORY.apply(this.delegate.options());
    return ret;
  }
  /**
   * Add a route that matches a HTTP OPTIONS request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  public Route options(String path) {
    def ret= Route.FACTORY.apply(this.delegate.options(path));
    return ret;
  }
  /**
   * Add a route that matches a HTTP OPTIONS request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  public Route optionsWithRegex(String regex) {
    def ret= Route.FACTORY.apply(this.delegate.optionsWithRegex(regex));
    return ret;
  }
  /**
   * Add a route that matches any HTTP PUT request
   *
   * @return the route
   */
  public Route put() {
    def ret= Route.FACTORY.apply(this.delegate.put());
    return ret;
  }
  /**
   * Add a route that matches a HTTP PUT request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  public Route put(String path) {
    def ret= Route.FACTORY.apply(this.delegate.put(path));
    return ret;
  }
  /**
   * Add a route that matches a HTTP PUT request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  public Route putWithRegex(String regex) {
    def ret= Route.FACTORY.apply(this.delegate.putWithRegex(regex));
    return ret;
  }
  /**
   * Add a route that matches any HTTP POST request
   *
   * @return the route
   */
  public Route post() {
    def ret= Route.FACTORY.apply(this.delegate.post());
    return ret;
  }
  /**
   * Add a route that matches a HTTP POST request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  public Route post(String path) {
    def ret= Route.FACTORY.apply(this.delegate.post(path));
    return ret;
  }
  /**
   * Add a route that matches a HTTP POST request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  public Route postWithRegex(String regex) {
    def ret= Route.FACTORY.apply(this.delegate.postWithRegex(regex));
    return ret;
  }
  /**
   * Add a route that matches any HTTP DELETE request
   *
   * @return the route
   */
  public Route delete() {
    def ret= Route.FACTORY.apply(this.delegate.delete());
    return ret;
  }
  /**
   * Add a route that matches a HTTP DELETE request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  public Route delete(String path) {
    def ret= Route.FACTORY.apply(this.delegate.delete(path));
    return ret;
  }
  /**
   * Add a route that matches a HTTP DELETE request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  public Route deleteWithRegex(String regex) {
    def ret= Route.FACTORY.apply(this.delegate.deleteWithRegex(regex));
    return ret;
  }
  /**
   * @return a list of all the routes on this router
   */
  public List<Route> getRoutes() {
    def ret = this.delegate.getRoutes()?.collect({underpants -> Route.FACTORY.apply(underpants)});
      return ret;
  }
  /**
   * Remove all the routes from this router
   *
   * @return a reference to this, so the API can be used fluently
   */
  public Router clear() {
    def ret= Router.FACTORY.apply(this.delegate.clear());
    return ret;
  }
  /**
   * Mount a sub router on this router
   *
   * @param mountPoint  the mount point (path prefix) to mount it on
   * @param subRouter  the router to mount as a sub router
   * @return a reference to this, so the API can be used fluently
   */
  public Router mountSubRouter(String mountPoint, Router subRouter) {
    def ret= Router.FACTORY.apply(this.delegate.mountSubRouter(mountPoint, (io.vertx.ext.apex.Router)subRouter.getDelegate()));
    return ret;
  }
  /**
   * Specify a handler for any unhandled exceptions on this router. The handler will be called for exceptions thrown
   * from handlers. This does not affect the normal failure routing logic.
   *
   * @param exceptionHandler  the exception handler
   * @return a reference to this, so the API can be used fluently
   */
  public Router exceptionHandler(Handler<Throwable> exceptionHandler) {
    def ret= Router.FACTORY.apply(this.delegate.exceptionHandler(exceptionHandler));
    return ret;
  }
  /**
   * Used to route a context to the router. Used for sub-routers. You wouldn't normally call this method directly.
   *
   * @param context  the routing context
   */
  public void handleContext(RoutingContext context) {
    this.delegate.handleContext((io.vertx.ext.apex.RoutingContext)context.getDelegate());
  }
  /**
   * Used to route a failure to the router. Used for sub-routers. You wouldn't normally call this method directly.
   *
   * @param context  the routing context
   */
  public void handleFailure(RoutingContext context) {
    this.delegate.handleFailure((io.vertx.ext.apex.RoutingContext)context.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.Router, Router> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.Router arg -> new Router(arg);
  };
}
