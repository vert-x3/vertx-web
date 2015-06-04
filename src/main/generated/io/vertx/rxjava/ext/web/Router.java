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

package io.vertx.rxjava.ext.web;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.core.http.HttpServerRequest;
import java.util.List;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava.core.Vertx;
import io.vertx.core.Handler;

/**
 * A router receives request from an {@link io.vertx.rxjava.core.http.HttpServer} and routes it to the first matching
 * {@link io.vertx.rxjava.ext.web.Route} that it contains. A router can contain many routes.
 * <p>
 * Routers are also used for routing failures.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.Router original} non RX-ified interface using Vert.x codegen.
 */

public class Router {

  final io.vertx.ext.web.Router delegate;

  public Router(io.vertx.ext.web.Router delegate) {
    this.delegate = delegate;
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
    Router ret= Router.newInstance(io.vertx.ext.web.Router.router((io.vertx.core.Vertx) vertx.getDelegate()));
    return ret;
  }

  /**
   * This method is used to provide a request to the router. Usually you take request from the
   * {@link io.vertx.rxjava.core.http.HttpServer#requestHandler} and pass it to this method. The
   * router then routes it to matching routes.
   * @param request the request
   */
  public void accept(HttpServerRequest request) { 
    this.delegate.accept((io.vertx.core.http.HttpServerRequest) request.getDelegate());
  }

  /**
   * Add a route with no matching criteria, i.e. it matches all requests or failures.
   * @return the route
   */
  public Route route() { 
    Route ret= Route.newInstance(this.delegate.route());
    return ret;
  }

  /**
   * Add a route that matches the specified HTTP method and path
   * @param method the HTTP method to match
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route route(HttpMethod method, String path) { 
    Route ret= Route.newInstance(this.delegate.route(method, path));
    return ret;
  }

  /**
   * Add a route that matches the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route route(String path) { 
    Route ret= Route.newInstance(this.delegate.route(path));
    return ret;
  }

  /**
   * Add a route that matches the specified HTTP method and path regex
   * @param method the HTTP method to match
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route routeWithRegex(HttpMethod method, String regex) { 
    Route ret= Route.newInstance(this.delegate.routeWithRegex(method, regex));
    return ret;
  }

  /**
   * Add a route that matches the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route routeWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.routeWithRegex(regex));
    return ret;
  }

  /**
   * Add a route that matches any HTTP GET request
   * @return the route
   */
  public Route get() { 
    Route ret= Route.newInstance(this.delegate.get());
    return ret;
  }

  /**
   * Add a route that matches a HTTP GET request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route get(String path) { 
    Route ret= Route.newInstance(this.delegate.get(path));
    return ret;
  }

  /**
   * Add a route that matches a HTTP GET request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route getWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.getWithRegex(regex));
    return ret;
  }

  /**
   * Add a route that matches any HTTP HEAD request
   * @return the route
   */
  public Route head() { 
    Route ret= Route.newInstance(this.delegate.head());
    return ret;
  }

  /**
   * Add a route that matches a HTTP HEAD request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route head(String path) { 
    Route ret= Route.newInstance(this.delegate.head(path));
    return ret;
  }

  /**
   * Add a route that matches a HTTP HEAD request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route headWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.headWithRegex(regex));
    return ret;
  }

  /**
   * Add a route that matches any HTTP OPTIONS request
   * @return the route
   */
  public Route options() { 
    Route ret= Route.newInstance(this.delegate.options());
    return ret;
  }

  /**
   * Add a route that matches a HTTP OPTIONS request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route options(String path) { 
    Route ret= Route.newInstance(this.delegate.options(path));
    return ret;
  }

  /**
   * Add a route that matches a HTTP OPTIONS request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route optionsWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.optionsWithRegex(regex));
    return ret;
  }

  /**
   * Add a route that matches any HTTP PUT request
   * @return the route
   */
  public Route put() { 
    Route ret= Route.newInstance(this.delegate.put());
    return ret;
  }

  /**
   * Add a route that matches a HTTP PUT request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route put(String path) { 
    Route ret= Route.newInstance(this.delegate.put(path));
    return ret;
  }

  /**
   * Add a route that matches a HTTP PUT request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route putWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.putWithRegex(regex));
    return ret;
  }

  /**
   * Add a route that matches any HTTP POST request
   * @return the route
   */
  public Route post() { 
    Route ret= Route.newInstance(this.delegate.post());
    return ret;
  }

  /**
   * Add a route that matches a HTTP POST request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route post(String path) { 
    Route ret= Route.newInstance(this.delegate.post(path));
    return ret;
  }

  /**
   * Add a route that matches a HTTP POST request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route postWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.postWithRegex(regex));
    return ret;
  }

  /**
   * Add a route that matches any HTTP DELETE request
   * @return the route
   */
  public Route delete() { 
    Route ret= Route.newInstance(this.delegate.delete());
    return ret;
  }

  /**
   * Add a route that matches a HTTP DELETE request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route delete(String path) { 
    Route ret= Route.newInstance(this.delegate.delete(path));
    return ret;
  }

  /**
   * Add a route that matches a HTTP DELETE request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route deleteWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.deleteWithRegex(regex));
    return ret;
  }

  /**
   * Add a route that matches any HTTP TRACE request
   * @return the route
   */
  public Route trace() { 
    Route ret= Route.newInstance(this.delegate.trace());
    return ret;
  }

  /**
   * Add a route that matches a HTTP TRACE request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route trace(String path) { 
    Route ret= Route.newInstance(this.delegate.trace(path));
    return ret;
  }

  /**
   * Add a route that matches a HTTP TRACE request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route traceWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.traceWithRegex(regex));
    return ret;
  }

  /**
   * Add a route that matches any HTTP CONNECT request
   * @return the route
   */
  public Route connect() { 
    Route ret= Route.newInstance(this.delegate.connect());
    return ret;
  }

  /**
   * Add a route that matches a HTTP CONNECT request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route connect(String path) { 
    Route ret= Route.newInstance(this.delegate.connect(path));
    return ret;
  }

  /**
   * Add a route that matches a HTTP CONNECT request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route connectWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.connectWithRegex(regex));
    return ret;
  }

  /**
   * Add a route that matches any HTTP PATCH request
   * @return the route
   */
  public Route patch() { 
    Route ret= Route.newInstance(this.delegate.patch());
    return ret;
  }

  /**
   * Add a route that matches a HTTP PATCH request and the specified path
   * @param path URI paths that begin with this path will match
   * @return the route
   */
  public Route patch(String path) { 
    Route ret= Route.newInstance(this.delegate.patch(path));
    return ret;
  }

  /**
   * Add a route that matches a HTTP PATCH request and the specified path regex
   * @param regex URI paths that begin with a match for this regex will match
   * @return the route
   */
  public Route patchWithRegex(String regex) { 
    Route ret= Route.newInstance(this.delegate.patchWithRegex(regex));
    return ret;
  }

  /**
   * @return a list of all the routes on this router
   * @return 
   */
  public List<Route> getRoutes() { 
    List<Route> ret = this.delegate.getRoutes().stream().map(Route::newInstance).collect(java.util.stream.Collectors.toList());
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
    this.delegate.mountSubRouter(mountPoint, (io.vertx.ext.web.Router) subRouter.getDelegate());
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
    this.delegate.handleContext((io.vertx.ext.web.RoutingContext) context.getDelegate());
  }

  /**
   * Used to route a failure to the router. Used for sub-routers. You wouldn't normally call this method directly.
   * @param context the routing context
   */
  public void handleFailure(RoutingContext context) { 
    this.delegate.handleFailure((io.vertx.ext.web.RoutingContext) context.getDelegate());
  }


  public static Router newInstance(io.vertx.ext.web.Router arg) {
    return new Router(arg);
  }
}
