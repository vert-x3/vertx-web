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

package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.addons.impl.CorsImpl;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Set;

/**
 * Server side CORS support for Vert.x Apex
 * http://www.w3.org/TR/cors/
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Cors extends Handler<RoutingContext> {

  static Cors cors(String allowedOriginPattern) {
    return new CorsImpl(allowedOriginPattern);
  }

  Cors allowedMethod(HttpMethod method);

  @GenIgnore
  Cors allowedMethods(Set<HttpMethod> methods);

  Cors allowedHeader(String headerName);

  Cors allowedHeaders(Set<String> headerNames);

  Cors exposedHeader(String headerName);

  Cors exposedHeaders(Set<String> headerNames);

  Cors allowCredentials(boolean allow);

  Cors maxAgeSeconds(int maxAgeSeconds);

  @Override
  void handle(RoutingContext context);

}
