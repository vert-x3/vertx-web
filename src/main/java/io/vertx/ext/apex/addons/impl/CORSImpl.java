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

package io.vertx.ext.apex.addons.impl;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.addons.CORS;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import static io.vertx.core.http.HttpHeaders.*;

/**
 * Ported from original authored by David Dossot
 * @author <a href="david@dossot.net">David Dossot</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CORSImpl implements CORS {

  private final String allowedOriginPattern;
  private final String allowedMethods;
  private final String allowedHeaders;
  private final String exposedHeaders;
  private final boolean allowCredentials;
  private final Pattern allowedOrigin;

  /**
   * @param allowedOriginPattern if null, '*' will be used.
   */
  public CORSImpl(String allowedOriginPattern,
                  Set<String> allowedMethods,
                  Set<String> allowedHeaders,
                  Set<String> exposedHeaders,
                  boolean allowCredentials) {
    if (allowCredentials && allowedOriginPattern == null) {
      throw new IllegalArgumentException("Resource that supports credentials can't accept all origins.");
    }

    this.allowedOriginPattern = allowedOriginPattern;
    this.allowedMethods = join(allowedMethods, ",");
    this.allowedHeaders = join(allowedHeaders, ",");
    this.exposedHeaders = join(exposedHeaders, ",");
    this.allowCredentials = allowCredentials;
    if (allowedOriginPattern != null) {
      allowedOrigin = Pattern.compile(allowedOriginPattern);
    } else {
      allowedOrigin = null;
    }
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (isPreflightRequest(request)) {
      handlePreflightRequest(request);
    } else {
      // FIXME = no checking of origin????
      addCorsResponseHeaders(context);
      context.next();
    }
  }

  private boolean isPreflightRequest(HttpServerRequest request) {
    return request.method() == HttpMethod.OPTIONS &&
      (request.headers().get(ACCESS_CONTROL_REQUEST_HEADERS) != null || request.headers().get(ACCESS_CONTROL_REQUEST_METHOD) != null);
  }

  private void handlePreflightRequest(HttpServerRequest request) {
    if (isValidOrigin(request.headers().get(ORIGIN))) {
      addCorsResponseHeaders(request.headers().get(ORIGIN),
        request.response().setStatusCode(204).setStatusMessage("No Content")).end();
    } else {
      request.response().setStatusCode(403).setStatusMessage("CORS Rejected").end();
    }
  }

  private HttpServerResponse addCorsResponseHeaders(RoutingContext context) {
    String origin = context.request().headers().get(ORIGIN);
    return addCorsResponseHeaders(origin, context.response());
  }

  private HttpServerResponse addCorsResponseHeaders(String origin, HttpServerResponse response) {
    if (isValidOrigin(origin)) {
      response.putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, getAllowedOrigin(origin));

      if (allowedMethods != null) {
        response.putHeader(ACCESS_CONTROL_ALLOW_METHODS, allowedMethods);
      }

      if (allowedHeaders != null) {
        response.putHeader(ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders);
      }

      if (exposedHeaders != null) {
        response.putHeader(ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders);
      }

      if (allowCredentials) {
        response.putHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
      }
    }

    return response;
  }

  private boolean isValidOrigin(String origin) {
    return allowedOriginPattern == null
      || (isNotBlank(origin) && allowedOrigin.matcher(origin).matches());
  }

  private String getAllowedOrigin(String origin) {
    return allowedOriginPattern == null ? "*" : origin;
  }

  private static boolean isNotBlank(String s) {
    return s == null || !s.trim().isEmpty();
  }

  private static String join(Collection<String> ss, String j) {
    if (ss == null || ss.isEmpty()) {
      return "";
    }

    StringBuffer sb = new StringBuffer();
    boolean first = true;
    for (String s : ss) {
      if (!first) {
        sb.append(j);
      }
      sb.append(s);
      first = false;
    }
    return sb.toString();
  }

}
