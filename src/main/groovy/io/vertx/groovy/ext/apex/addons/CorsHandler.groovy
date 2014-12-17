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
import io.vertx.core.http.HttpMethod
import java.util.Set
import io.vertx.core.Handler
/**
 * Server side CORS support for Vert.x Apex
 * http://www.w3.org/TR/cors/
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class CorsHandler {
  final def io.vertx.ext.apex.addons.CorsHandler delegate;
  public CorsHandler(io.vertx.ext.apex.addons.CorsHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static CorsHandler cors(String allowedOriginPattern) {
    def ret= CorsHandler.FACTORY.apply(io.vertx.ext.apex.addons.CorsHandler.cors(allowedOriginPattern));
    return ret;
  }
  public CorsHandler allowedMethod(HttpMethod method) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.allowedMethod(method));
    return ret;
  }
  public CorsHandler allowedHeader(String headerName) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.allowedHeader(headerName));
    return ret;
  }
  public CorsHandler allowedHeaders(Set<String> headerNames) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.allowedHeaders(headerNames));
    return ret;
  }
  public CorsHandler exposedHeader(String headerName) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.exposedHeader(headerName));
    return ret;
  }
  public CorsHandler exposedHeaders(Set<String> headerNames) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.exposedHeaders(headerNames));
    return ret;
  }
  public CorsHandler allowCredentials(boolean allow) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.allowCredentials(allow));
    return ret;
  }
  public CorsHandler maxAgeSeconds(int maxAgeSeconds) {
    def ret= CorsHandler.FACTORY.apply(this.delegate.maxAgeSeconds(maxAgeSeconds));
    return ret;
  }
  public void handle(RoutingContext context) {
    this.delegate.handle((io.vertx.ext.apex.core.RoutingContext)context.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.CorsHandler, CorsHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.CorsHandler arg -> new CorsHandler(arg);
  };
}
