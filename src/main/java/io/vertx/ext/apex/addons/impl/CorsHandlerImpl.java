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
import io.vertx.ext.apex.addons.CorsHandler;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * Based partially on original authored by David Dossot
 * @author <a href="david@dossot.net">David Dossot</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CorsHandlerImpl implements CorsHandler {

  private final Pattern allowedOrigin;

  private String allowedMethodsString;
  private String allowedHeadersString;
  private String exposedHeadersString;
  private boolean allowCredentials;
  private String maxAgeSeconds;
  private final Set<HttpMethod> allowedMethods = new LinkedHashSet<>();
  private final Set<String> allowedHeaders = new LinkedHashSet<>();
  private final Set<String> exposedHeaders = new LinkedHashSet<>();

  public CorsHandlerImpl(String allowedOriginPattern) {
    Objects.requireNonNull(allowedOriginPattern);
    if ("*".equals(allowedOriginPattern)) {
      allowedOrigin = null;
    } else {
      allowedOrigin = Pattern.compile(allowedOriginPattern);
    }
  }

  @Override
  public CorsHandler allowedMethod(HttpMethod method) {
    allowedMethods.add(method);
    allowedMethodsString = join(allowedMethods);
    return this;
  }

  @Override
  public CorsHandler allowedMethods(Set<HttpMethod> methods) {
    allowedMethods.addAll(methods);
    allowedMethodsString = join(allowedMethods);
    return this;
  }

  @Override
  public CorsHandler allowedHeader(String headerName) {
    allowedHeaders.add(headerName);
    allowedHeadersString = join(allowedHeaders);
    return this;
  }

  @Override
  public CorsHandler allowedHeaders(Set<String> headerNames) {
    allowedHeaders.addAll(headerNames);
    allowedHeadersString = join(allowedHeaders);
    return this;
  }

  @Override
  public CorsHandler exposedHeader(String headerName) {
    exposedHeaders.add(headerName);
    exposedHeadersString = join(exposedHeaders);
    return this;
  }

  @Override
  public CorsHandler exposedHeaders(Set<String> headerNames) {
    exposedHeaders.addAll(headerNames);
    exposedHeadersString = join(exposedHeaders);
    return this;
  }

  @Override
  public CorsHandler allowCredentials(boolean allow) {
    this.allowCredentials = allow;
    return this;
  }

  @Override
  public CorsHandler maxAgeSeconds(int maxAgeSeconds) {
    this.maxAgeSeconds = maxAgeSeconds == -1 ? null : String.valueOf(maxAgeSeconds);
    return this;
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();
    String origin = context.request().headers().get(ORIGIN);
    if (isValidOrigin(origin)) {
      String accessControlRequestMethod = request.headers().get(ACCESS_CONTROL_REQUEST_METHOD);
      if (request.method() == HttpMethod.OPTIONS && accessControlRequestMethod != null) {
        // Pre-flight request
        addCredentialsAndOriginHeader(response, origin);
        if (allowedMethodsString != null) {
          response.putHeader(ACCESS_CONTROL_ALLOW_METHODS, allowedMethodsString);
        }
        if (allowedHeadersString != null) {
          response.putHeader(ACCESS_CONTROL_ALLOW_HEADERS, allowedHeadersString);
        }
        if (maxAgeSeconds != null) {
          response.putHeader(ACCESS_CONTROL_MAX_AGE, maxAgeSeconds);
        }
        response.setStatusCode(204).end();
      } else {
        addCredentialsAndOriginHeader(response, origin);
        if (exposedHeadersString != null) {
          response.putHeader(ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeadersString);
        }
        context.next();
      }
    } else {
      sendInvalid(request.response());
    }
  }

  private void addCredentialsAndOriginHeader(HttpServerResponse response, String origin) {
    if (allowCredentials) {
      response.putHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
      // Must be exact origin (not '*') in case of credentials
      response.putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    } else {
      // Can be '*' too
      response.putHeader(ACCESS_CONTROL_ALLOW_ORIGIN, getAllowedOrigin(origin));
    }
  }

  private void sendInvalid(HttpServerResponse resp) {
    resp.setStatusCode(403).setStatusMessage("CORS Rejected - Invalid origin").end();
  }

  private boolean isValidOrigin(String origin) {
    if (allowedOrigin == null) {
      // Null means accept all origins
      return true;
    }
    if (origin == null) {
      return false;
    }
    return allowedOrigin.matcher(origin).matches();
  }

  private String getAllowedOrigin(String origin) {
    return allowedOrigin == null ? "*" : origin;
  }

  private String join(Collection<? extends Object> ss) {
    if (ss == null || ss.isEmpty()) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Object s : ss) {
      if (!first) {
        sb.append(',');
      }
      sb.append(s);
      first = false;
    }
    return sb.toString();
  }

}
