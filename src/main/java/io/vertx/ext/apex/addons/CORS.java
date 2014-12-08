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
import io.vertx.ext.apex.addons.impl.CORSImpl;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Set;

/**
 * Server side CORS support for Vert.x Apex
 * http://www.w3.org/TR/cors/
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface CORS extends Handler<RoutingContext> {

  static CORS cors(String allowedOriginPattern) {
    return new CORSImpl(allowedOriginPattern);
  }

  CORS allowedMethod(HttpMethod method);

  @GenIgnore
  CORS allowedMethods(Set<HttpMethod> methods);

  CORS allowedHeader(String headerName);

  CORS allowedHeaders(Set<String> headerNames);

  CORS exposedHeader(String headerName);

  CORS exposedHeaders(Set<String> headerNames);

  CORS allowCredentials(boolean allow);

  CORS maxAgeSeconds(int maxAgeSeconds);

  @Override
  void handle(RoutingContext context);

}
