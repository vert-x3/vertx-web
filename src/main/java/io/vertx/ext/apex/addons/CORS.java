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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.addons.impl.CORSImpl;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface CORS extends Handler<RoutingContext> {

  static CORS cors(String allowedOriginPattern,
                   Set<String> allowedMethods,
                   Set<String> allowedHeaders,
                   Set<String> exposedHeaders,
                   boolean allowCredentials) {
    return new CORSImpl(allowedOriginPattern, allowedMethods, allowedHeaders, exposedHeaders, allowCredentials);
  }

  @Override
  void handle(RoutingContext context);

}
