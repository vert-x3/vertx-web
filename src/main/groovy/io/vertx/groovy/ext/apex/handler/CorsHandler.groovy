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
import io.vertx.core.http.HttpMethod
import io.vertx.groovy.ext.apex.RoutingContext
import java.util.Set
import io.vertx.core.Handler
/**
 * A handler which implements server side http://www.w3.org/TR/cors/[CORS] support for Apex.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class CorsHandler {
  final def io.vertx.ext.apex.handler.CorsHandler delegate;
  public CorsHandler(io.vertx.ext.apex.handler.CorsHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a CORS handler
   *
   * @param allowedOriginPattern  the allowed origin pattern
   * @return  the handler
   */
  public static CorsHandler create(String allowedOriginPattern) {
    def ret= CorsHandler.FACTORY.apply(io.vertx.ext.apex.handler.CorsHandler.create(allowedOriginPattern));
    return ret;
  }
  /**
   * Add an allowed method
   *
   * @param method  the method to add
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler allowedMethod(HttpMethod method) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.allowedMethod(method));
    return ret;
  }
  /**
   * Add an allowed header
   *
   * @param headerName  the allowed header name
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler allowedHeader(String headerName) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.allowedHeader(headerName));
    return ret;
  }
  /**
   * Add a set of allowed headers
   *
   * @param headerNames  the allowed header names
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler allowedHeaders(Set<String> headerNames) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.allowedHeaders(headerNames));
    return ret;
  }
  /**
   * Add an exposed header
   *
   * @param headerName  the exposed header name
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler exposedHeader(String headerName) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.exposedHeader(headerName));
    return ret;
  }
  /**
   * Add a set of exposed headers
   *
   * @param headerNames  the exposed header names
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler exposedHeaders(Set<String> headerNames) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.exposedHeaders(headerNames));
    return ret;
  }
  /**
   * Set whether credentials are allowed
   *
   * @param allow true if allowed
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler allowCredentials(boolean allow) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.allowCredentials(allow));
    return ret;
  }
  /**
   * Set how long the browser should cache the information
   *
   * @param maxAgeSeconds  max age in seconds
   * @return a reference to this, so the API can be used fluently
   */
  public CorsHandler maxAgeSeconds(int maxAgeSeconds) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.maxAgeSeconds(maxAgeSeconds));
    return ret;
  }
  public void handle(RoutingContext context) {
    this.delegate.handle((io.vertx.ext.apex.RoutingContext)context.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.handler.CorsHandler, CorsHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.handler.CorsHandler arg -> new CorsHandler(arg);
  };
}
