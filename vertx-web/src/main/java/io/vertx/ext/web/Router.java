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

package io.vertx.ext.web;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.impl.RouterImpl;

import java.util.List;

/**
 * A router receives request from an {@link io.vertx.core.http.HttpServer} and routes it to the first matching
 * {@link Route} that it contains. A router can contain many routes.
 * <p>
 * Routers are also used for routing failures.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Router extends Handler<HttpServerRequest> {

  /**
   * Create a router
   *
   * @param vertx  the Vert.x instance
   * @return the router
   */
  static Router router(Vertx vertx) {
    return new RouterImpl(vertx);
  }

  /**
   * This method is used to provide a request to the router. Usually you take request from the
   * {@link io.vertx.core.http.HttpServer#requestHandler(Handler)} and pass it to this method. The
   * router then routes it to matching routes.
   *
   * This method is now deprecated you can use this object directly as a request handler, which
   * means there is no need for a method reference anymore.
   *
   * @param request  the request
   * @deprecated
   */
  @Deprecated
  default void accept(HttpServerRequest request) {
    handle(request);
  }

  /**
   * Add a route with no matching criteria, i.e. it matches all requests or failures.
   *
   * @return  the route
   */
  Route route();

  /**
   * Add a route that matches the specified HTTP method and path
   *
   * @param method  the HTTP method to match
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route route(HttpMethod method, String path);

  /**
   * Add a route that matches the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route route(String path);

  /**
   * Add a route that matches the specified HTTP method and path regex
   *
   * @param method  the HTTP method to match
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route routeWithRegex(HttpMethod method, String regex);

  /**
   * Add a route that matches the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route routeWithRegex(String regex);

  /**
   * Add a route that matches any HTTP GET request
   *
   * @return the route
   */
  Route get();

  /**
   * Add a route that matches a HTTP GET request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route get(String path);

  /**
   * Add a route that matches a HTTP GET request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route getWithRegex(String regex);

  /**
   * Add a route that matches any HTTP HEAD request
   *
   * @return the route
   */
  Route head();

  /**
   * Add a route that matches a HTTP HEAD request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route head(String path);

  /**
   * Add a route that matches a HTTP HEAD request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route headWithRegex(String regex);

  /**
   * Add a route that matches any HTTP OPTIONS request
   *
   * @return the route
   */
  Route options();

  /**
   * Add a route that matches a HTTP OPTIONS request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route options(String path);

  /**
   * Add a route that matches a HTTP OPTIONS request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route optionsWithRegex(String regex);

  /**
   * Add a route that matches any HTTP PUT request
   *
   * @return the route
   */
  Route put();

  /**
   * Add a route that matches a HTTP PUT request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route put(String path);

  /**
   * Add a route that matches a HTTP PUT request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route putWithRegex(String regex);

  /**
   * Add a route that matches any HTTP POST request
   *
   * @return the route
   */
  Route post();

  /**
   * Add a route that matches a HTTP POST request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route post(String path);

  /**
   * Add a route that matches a HTTP POST request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route postWithRegex(String regex);

  /**
   * Add a route that matches any HTTP DELETE request
   *
   * @return the route
   */
  Route delete();

  /**
   * Add a route that matches a HTTP DELETE request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route delete(String path);

  /**
   * Add a route that matches a HTTP DELETE request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route deleteWithRegex(String regex);

  /**
   * Add a route that matches any HTTP TRACE request
   *
   * @return the route
   */
  Route trace();

  /**
   * Add a route that matches a HTTP TRACE request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route trace(String path);

  /**
   * Add a route that matches a HTTP TRACE request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route traceWithRegex(String regex);

  /**
   * Add a route that matches any HTTP CONNECT request
   *
   * @return the route
   */
  Route connect();

  /**
   * Add a route that matches a HTTP CONNECT request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route connect(String path);

  /**
   * Add a route that matches a HTTP CONNECT request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route connectWithRegex(String regex);

  /**
   * Add a route that matches any HTTP PATCH request
   *
   * @return the route
   */
  Route patch();

  /**
   * Add a route that matches a HTTP PATCH request and the specified path
   *
   * @param path  URI paths that begin with this path will match
   *
   * @return the route
   */
  Route patch(String path);

  /**
   * Add a route that matches a HTTP PATCH request and the specified path regex
   *
   * @param regex  URI paths that begin with a match for this regex will match
   *
   * @return the route
   */
  Route patchWithRegex(String regex);

  /**
   * @return a list of all the routes on this router
   */
  List<Route> getRoutes();

  /**
   * Remove all the routes from this router
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Router clear();

  /**
   * Mount a sub router on this router
   *
   * @param mountPoint  the mount point (path prefix) to mount it on
   * @param subRouter  the router to mount as a sub router
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Router mountSubRouter(String mountPoint, Router subRouter);

  /**
   * Specify a handler for any unhandled exceptions on this router. The handler will be called for exceptions thrown
   * from handlers. This does not affect the normal failure routing logic.
   *
   * @param exceptionHandler  the exception handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Router exceptionHandler(@Nullable Handler<Throwable> exceptionHandler);

  /**
   * Used to route a context to the router. Used for sub-routers. You wouldn't normally call this method directly.
   *
   * @param context  the routing context
   */
  void handleContext(RoutingContext context);

  /**
   * Used to route a failure to the router. Used for sub-routers. You wouldn't normally call this method directly.
   *
   * @param context  the routing context
   */
  void handleFailure(RoutingContext context);

}
