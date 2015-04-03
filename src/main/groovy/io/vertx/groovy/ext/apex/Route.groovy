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
import io.vertx.core.http.HttpMethod
import io.vertx.core.Handler
/**
 * A route is a holder for a set of criteria which determine whether an HTTP request or failure should be routed
 * to a handler.
*/
@CompileStatic
public class Route {
  final def io.vertx.ext.apex.Route delegate;
  public Route(io.vertx.ext.apex.Route delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Add an HTTP method for this route. By default a route will match all HTTP methods. If any are specified then the route
   * will only match any of the specified methods
   * @param method the HTTP method to add
   * @return a reference to this, so the API can be used fluently
   */
  public Route method(HttpMethod method) {
    this.delegate.method(method);
    return this;
  }
  /**
   * Set the path prefix for this route. If set then this route will only match request URI paths which start with this
   * path prefix. Only a single path or path regex can be set for a route.
   * @param path the path prefix
   * @return a reference to this, so the API can be used fluently
   */
  public Route path(String path) {
    this.delegate.path(path);
    return this;
  }
  /**
   * Set the path prefix as a regular expression. If set then this route will only match request URI paths, the beginning
   * of which match the regex. Only a single path or path regex can be set for a route.
   * @param path the path regex
   * @return a reference to this, so the API can be used fluently
   */
  public Route pathRegex(String path) {
    this.delegate.pathRegex(path);
    return this;
  }
  /**
   * Add a content type produced by this route. Used for content based routing.
   * @param contentType the content type
   * @return a reference to this, so the API can be used fluently
   */
  public Route produces(String contentType) {
    def ret= new io.vertx.groovy.ext.apex.Route(this.delegate.produces(contentType));
    return ret;
  }
  /**
   * Add a content type consumed by this route. Used for content based routing.
   * @param contentType the content type
   * @return a reference to this, so the API can be used fluently
   */
  public Route consumes(String contentType) {
    this.delegate.consumes(contentType);
    return this;
  }
  /**
   * Specify the order for this route. The router tests routes in that order.
   * @param order the order
   * @return a reference to this, so the API can be used fluently
   */
  public Route order(int order) {
    this.delegate.order(order);
    return this;
  }
  /**
   * Specify whether this is the last route for the router.
   * @param last true if last
   * @return a reference to this, so the API can be used fluently
   */
  public Route last(boolean last) {
    this.delegate.last(last);
    return this;
  }
  /**
   * Specify a request handler for the route. The router routes requests to handlers depending on whether the various
   * criteria such as method, path, etc match. There can be only one request handler for a route. If you set this more
   * than once it will overwrite the previous handler.
   * @param requestHandler the request handler
   * @return a reference to this, so the API can be used fluently
   */
  public Route handler(Handler<RoutingContext> requestHandler) {
    this.delegate.handler(new Handler<io.vertx.ext.apex.RoutingContext>() {
      public void handle(io.vertx.ext.apex.RoutingContext event) {
        requestHandler.handle(new io.vertx.groovy.ext.apex.RoutingContext(event));
      }
    });
    return this;
  }
  /**
   * Specify a failure handler for the route. The router routes failures to failurehandlers depending on whether the various
   * criteria such as method, path, etc match. There can be only one failure handler for a route. If you set this more
   * than once it will overwrite the previous handler.
   * @param failureHandler the request handler
   * @return a reference to this, so the API can be used fluently
   */
  public Route failureHandler(Handler<RoutingContext> failureHandler) {
    this.delegate.failureHandler(new Handler<io.vertx.ext.apex.RoutingContext>() {
      public void handle(io.vertx.ext.apex.RoutingContext event) {
        failureHandler.handle(new io.vertx.groovy.ext.apex.RoutingContext(event));
      }
    });
    return this;
  }
  /**
   * Remove this route from the router
   * @return a reference to this, so the API can be used fluently
   */
  public Route remove() {
    this.delegate.remove();
    return this;
  }
  /**
   * Disable this route. While disabled the router will not route any requests or failures to it.
   * @return a reference to this, so the API can be used fluently
   */
  public Route disable() {
    this.delegate.disable();
    return this;
  }
  /**
   * Enable this route.
   * @return a reference to this, so the API can be used fluently
   */
  public Route enable() {
    this.delegate.enable();
    return this;
  }
  /**
   * If true then the normalised request path will be used when routing (e.g. removing duplicate /)
   * Default is true
   * @param useNormalisedPath use normalised path for routing?
   * @return a reference to this, so the API can be used fluently
   */
  public Route useNormalisedPath(boolean useNormalisedPath) {
    this.delegate.useNormalisedPath(useNormalisedPath);
    return this;
  }
  /**
   * @return the path prefix (if any) for this route
   * @return 
   */
  public String getPath() {
    def ret = this.delegate.getPath();
    return ret;
  }
}
