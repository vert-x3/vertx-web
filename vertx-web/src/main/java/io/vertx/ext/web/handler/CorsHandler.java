/*
 * Copyright 2022 Red Hat, Inc.
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

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.CorsHandlerImpl;

import java.util.List;
import java.util.Set;

/**
 * A handler which implements server side http://www.w3.org/TR/cors/[CORS] support for Vert.x-Web.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface CorsHandler extends SecurityPolicyHandler {

  /**
   * Create a CORS handler using a regular expression to match origins. An origin follows rfc6454#section-7
   * and is expected to have the format: {@code <scheme> "://" <hostname> [ ":" <port> ]}
   *
   * @param allowedOriginPattern  the allowed origin pattern
   * @return  the handler
   */
  static CorsHandler create(String allowedOriginPattern) {
    return new CorsHandlerImpl(allowedOriginPattern);
  }

  /**
   * Create a empty CORS handler that allows {@code *} origin.
   * @return the handler
   */
  static CorsHandler create() {
    return new CorsHandlerImpl();
  }

  /**
   * Add an origin to the list of allowed Origins. An origin follows rfc6454#section-7
   * and is expected to have the format: {@code <scheme> "://" <hostname> [ ":" <port> ]}
   * @param origin the well formatted static origin
   * @return self
   */
  @Fluent
  CorsHandler addOrigin(String origin);

  /**
   * Set the list of allowed origins. An origin follows rfc6454#section-7
   * and is expected to have the format: {@code <scheme> "://" <hostname> [ ":" <port> ]}
   * @param origins the well formatted static origin list
   * @return self
   */
  @Fluent
  CorsHandler addOrigins(List<String> origins);

  /**
   * Add an allowed method
   *
   * @param method  the method to add
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CorsHandler allowedMethod(HttpMethod method);

  /**
   * Add a set of  allowed methods
   *
   * @param methods the methods to add
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CorsHandler allowedMethods(Set<HttpMethod> methods);

  /**
   * Add an allowed header
   *
   * @param headerName  the allowed header name
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CorsHandler allowedHeader(String headerName);

  /**
   * Add a set of allowed headers
   *
   * @param headerNames  the allowed header names
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CorsHandler allowedHeaders(Set<String> headerNames);

  /**
   * Add an exposed header
   *
   * @param headerName  the exposed header name
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CorsHandler exposedHeader(String headerName);

  /**
   * Add a set of exposed headers
   *
   * @param headerNames  the exposed header names
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CorsHandler exposedHeaders(Set<String> headerNames);

  /**
   * Set whether credentials are allowed. Note that user agents will block
   * requests that use a wildcard as origin and include credentials.
   *
   * From the MDN documentation you can read:
   *
   * <blockquote>
   * Important note: when responding to a credentialed request,
   * server must specify a domain, and cannot use wild carding.
   * </blockquote>
   *
   * @param allow true if allowed
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CorsHandler allowCredentials(boolean allow);

  /**
   * Set how long the browser should cache the information
   *
   * @param maxAgeSeconds  max age in seconds
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CorsHandler maxAgeSeconds(int maxAgeSeconds);

  /**
   * Set whether access from public to private networks are allowed.
   * Defaults to false
   *
   * @param allow true if allowed
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CorsHandler allowPrivateNetwork(boolean allow);

}
