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

package io.vertx.groovy.ext.web.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.http.HttpMethod
import java.util.Set
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
/**
 * A handler which implements server side http://www.w3.org/TR/cors/[CORS] support for Vert.x-Web.
*/
@CompileStatic
public class CorsHandler implements Handler<RoutingContext> {
  final def io.vertx.ext.web.handler.CorsHandler delegate;
  public CorsHandler(io.vertx.ext.web.handler.CorsHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.web.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create a CORS handler
   * @param allowedOriginPattern the allowed origin pattern
   * @return the handler
   */
  public static CorsHandler create(String allowedOriginPattern) {
    def ret= new io.vertx.groovy.ext.web.handler.CorsHandler(io.vertx.ext.web.handler.CorsHandler.create(allowedOriginPattern));
    return ret;
  }
  /**
   * Add an allowed method
   * @param method the method to add
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler allowedMethod(HttpMethod method) {
    this.delegate.allowedMethod(method);
    return this;
  }
  /**
   * Add an allowed header
   * @param headerName the allowed header name
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler allowedHeader(String headerName) {
    this.delegate.allowedHeader(headerName);
    return this;
  }
  /**
   * Add a set of allowed headers
   * @param headerNames the allowed header names
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler allowedHeaders(Set<String> headerNames) {
    this.delegate.allowedHeaders(headerNames);
    return this;
  }
  /**
   * Add an exposed header
   * @param headerName the exposed header name
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler exposedHeader(String headerName) {
    this.delegate.exposedHeader(headerName);
    return this;
  }
  /**
   * Add a set of exposed headers
   * @param headerNames the exposed header names
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler exposedHeaders(Set<String> headerNames) {
    this.delegate.exposedHeaders(headerNames);
    return this;
  }
  /**
   * Set whether credentials are allowed
   * @param allow true if allowed
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler allowCredentials(boolean allow) {
    this.delegate.allowCredentials(allow);
    return this;
  }
  /**
   * Set how long the browser should cache the information
   * @param maxAgeSeconds max age in seconds
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler maxAgeSeconds(int maxAgeSeconds) {
    this.delegate.maxAgeSeconds(maxAgeSeconds);
    return this;
  }
}
