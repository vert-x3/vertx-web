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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

/**
 * HTTP Strict Transport Security (HSTS) <a href="http://tools.ietf.org/html/rfc6797">RFC6797</a>.
 *
 * This handler adds the strict transport security headers, for this domain or subdomains.
 */
@VertxGen
public interface HSTSHandler extends Handler<RoutingContext> {

  // 6 months
  long DEFAULT_MAX_AGE = 15768000;

  /**
   * Creates a new instance that shall consider the configuration for sub domains.
   * @param maxAge max age to attribute to the header
   * @param includeSubDomains consider sub domains when adding the header
   * @return an instance.
   */
  static HSTSHandler create(long maxAge, boolean includeSubDomains) {
    final String header;

    if (includeSubDomains) {
      header = "max-age=" + maxAge + "; includeSubdomains";
    } else {
      header = "max-age=" + maxAge;
    }

    return ctx -> {
      final HttpServerRequest request = ctx.request();

      boolean isSecure = (request.isSSL())
        // Non-standard header field used by Microsoft applications and load-balancers
        || ("on".equalsIgnoreCase(request.getHeader("Front-End-Https")));

      if (isSecure) {
        request.response().putHeader("Strict-Transport-Security", header);
      }

      ctx.next();
    };
  }

  /**
   * Creates a new instance that shall consider the configuration for sub domains.
   * @param includeSubDomains consider sub domains when adding the header
   * @return an instance.
   */
  static HSTSHandler create(boolean includeSubDomains) {
    return create(DEFAULT_MAX_AGE, includeSubDomains);
  }

  /**
   * Creates a new instance that does not consider the configuration for sub domains.
   * Using the default max age.
   * @return an instance.
   */
  static HSTSHandler create() {
    return create(false);
  }
}
