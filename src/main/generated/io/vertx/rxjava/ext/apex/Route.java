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

package io.vertx.rxjava.ext.apex;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.Handler;

/**
 * A route is a holder for a set of criteria which determine whether an HTTP request or failure should be routed
 * to a handler.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 * NOTE: This class has been automatically generated from the original non RX-ified interface using Vert.x codegen.
 */

public class Route {

  final io.vertx.ext.apex.Route delegate;

  public Route(io.vertx.ext.apex.Route delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Add an HTTP method for this route. By default a route will match all HTTP methods. If any are specified then the route
   * will only match any of the specified methods
   *
   * @param method  the HTTP method to add
   * @return a reference to this, so the API can be used fluently
   */
  public Route method(HttpMethod method) {
    Route ret= Route.newInstance(this.delegate.method(method));
    return ret;
  }

  /**
   * Set the path prefix for this route. If set then this route will only match request URI paths which start with this
   * path prefix. Only a single path or path regex can be set for a route.
   *
   * @param path  the path prefix
   * @return a reference to this, so the API can be used fluently
   */
  public Route path(String path) {
    Route ret= Route.newInstance(this.delegate.path(path));
    return ret;
  }

  /**
   * Set the path prefix as a regular expression. If set then this route will only match request URI paths, the beginning
   * of which match the regex. Only a single path or path regex can be set for a route.
   *
   * @param path  the path regex
   * @return a reference to this, so the API can be used fluently
   */
  public Route pathRegex(String path) {
    Route ret= Route.newInstance(this.delegate.pathRegex(path));
    return ret;
  }

  /**
   * Add a content type produced by this route. Used for content based routing.
   *
   * @param contentType  the content type
   * @return a reference to this, so the API can be used fluently
   */
  public Route produces(String contentType) {
    Route ret= Route.newInstance(this.delegate.produces(contentType));
    return ret;
  }

  /**
   * Add a content type consumed by this route. Used for content based routing.
   *
   * @param contentType  the content type
   * @return a reference to this, so the API can be used fluently
   */
  public Route consumes(String contentType) {
    Route ret= Route.newInstance(this.delegate.consumes(contentType));
    return ret;
  }

  /**
   * Specify the order for this route. The router tests routes in that order.
   *
   * @param order  the order
   * @return a reference to this, so the API can be used fluently
   */
  public Route order(int order) {
    Route ret= Route.newInstance(this.delegate.order(order));
    return ret;
  }

  /**
   * Specify whether this is the last route for the router.
   *
   * @param last  true if last
   * @return a reference to this, so the API can be used fluently
   */
  public Route last(boolean last) {
    Route ret= Route.newInstance(this.delegate.last(last));
    return ret;
  }

  /**
   * Specify a request handler for the route. The router routes requests to handlers depending on whether the various
   * criteria such as method, path, etc match. There can be only one request handler for a route. If you set this more
   * than once it will overwrite the previous handler.
   *
   * @param requestHandler  the request handler
   * @return a reference to this, so the API can be used fluently
   */
  public Route handler(Handler<RoutingContext> requestHandler) {
    Route ret= Route.newInstance(this.delegate.handler(new Handler<io.vertx.ext.apex.RoutingContext>() {
      public void handle(io.vertx.ext.apex.RoutingContext event) {
        requestHandler.handle(new RoutingContext(event));
      }
    }));
    return ret;
  }

  /**
   * Specify a failure handler for the route. The router routes failures to failurehandlers depending on whether the various
   * criteria such as method, path, etc match. There can be only one failure handler for a route. If you set this more
   * than once it will overwrite the previous handler.
   *
   * @param failureHandler  the request handler
   * @return a reference to this, so the API can be used fluently
   */
  public Route failureHandler(Handler<RoutingContext> failureHandler) {
    Route ret= Route.newInstance(this.delegate.failureHandler(new Handler<io.vertx.ext.apex.RoutingContext>() {
      public void handle(io.vertx.ext.apex.RoutingContext event) {
        failureHandler.handle(new RoutingContext(event));
      }
    }));
    return ret;
  }

  /**
   * Remove this route from the router
   *
   * @return a reference to this, so the API can be used fluently
   */
  public Route remove() {
    Route ret= Route.newInstance(this.delegate.remove());
    return ret;
  }

  /**
   * Disable this route. While disabled the router will not route any requests or failures to it.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public Route disable() {
    Route ret= Route.newInstance(this.delegate.disable());
    return ret;
  }

  /**
   * Enable this route.
   *
   * @return a reference to this, so the API can be used fluently
   */
  public Route enable() {
    Route ret= Route.newInstance(this.delegate.enable());
    return ret;
  }

  /**
   * @return the path prefix (if any) for this route
   */
  public String getPath() {
    String ret = this.delegate.getPath();
    return ret;
  }


  public static Route newInstance(io.vertx.ext.apex.Route arg) {
    return new Route(arg);
  }
}
