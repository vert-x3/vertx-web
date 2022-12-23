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

package io.vertx.ext.web.handler.impl;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.impl.Origin;
import io.vertx.ext.web.impl.RoutingContextInternal;
import io.vertx.ext.web.impl.Utils;

import java.util.LinkedHashSet;
import java.util.List;
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

  private Set<Pattern> relativeOrigins;
  private Set<Origin> staticOrigins;

  private String allowedMethodsString;
  private String allowedHeadersString;
  private String exposedHeadersString;
  private boolean allowCredentials;
  private String maxAgeSeconds;
  private boolean allowPrivateNetwork;
  private final Set<String> allowedMethods = new LinkedHashSet<>();
  private final Set<String> allowedHeaders = new LinkedHashSet<>();
  private final Set<String> exposedHeaders = new LinkedHashSet<>();

  public CorsHandlerImpl() {
    relativeOrigins = null;
    staticOrigins = null;
  }

  private boolean starOrigin() {
    return relativeOrigins == null && staticOrigins == null;
  }

  private boolean uniqueOrigin() {
    return relativeOrigins == null && staticOrigins != null && staticOrigins.size() == 1;
  }

  @Override
  public CorsHandler addOrigin(String origin) {
    Objects.requireNonNull(origin, "'origin' cannot be null");

    if (staticOrigins == null) {
      if (origin.equals("*")) {
        // we signal any as null
        return this;
      }
      staticOrigins = new LinkedHashSet<>();
    } else {
      if (origin.equals("*")) {
        // we signal any as null
        throw new IllegalStateException("Cannot mix '*' with explicit origins");
      }
    }
    staticOrigins.add(Origin.parse(origin));
    return this;
  }

  @Override
  public CorsHandler addOrigins(List<String> origins) {
    Objects.requireNonNull(origins, "'origins' cannot be null");

    for (String origin : origins) {
      addOrigin(origin);
    }
    return this;
  }

  @Override
  public CorsHandler addRelativeOrigin(String origin) {
    Objects.requireNonNull(origin, "'origin' cannot be null");

    if (relativeOrigins == null) {
      relativeOrigins = new LinkedHashSet<>();
    } else {
      if (origin.equals(".*")) {
        // we signal any as null
        throw new IllegalStateException("Cannot mix '/.*/' with relative origins");
      }
    }
    relativeOrigins.add(Pattern.compile(origin));
    return this;
  }

  @Override
  public CorsHandler addRelativeOrigins(List<String> origins) {
    Objects.requireNonNull(origins, "'origins' cannot be null");

    for (String origin : origins) {
      addRelativeOrigin(origin);
    }
    return this;
  }

  @Override
  public CorsHandler allowedMethod(HttpMethod method) {
    allowedMethods.add(method.name());
    allowedMethodsString = String.join(",", allowedMethods);
    return this;
  }

  @Override
  public CorsHandler allowedMethods(Set<HttpMethod> methods) {
    for (HttpMethod method : methods) {
      allowedMethods.add(method.name());
    }
    allowedMethodsString = String.join(",", allowedMethods);
    return this;
  }

  @Override
  public CorsHandler allowedHeader(String headerName) {
    allowedHeaders.add(headerName);
    allowedHeadersString = String.join(",", allowedHeaders);
    return this;
  }

  @Override
  public CorsHandler allowedHeaders(Set<String> headerNames) {
    allowedHeaders.addAll(headerNames);
    allowedHeadersString = String.join(",", allowedHeaders);
    return this;
  }

  @Override
  public CorsHandler exposedHeader(String headerName) {
    exposedHeaders.add(headerName);
    exposedHeadersString = String.join(",", exposedHeaders);
    return this;
  }

  @Override
  public CorsHandler exposedHeaders(Set<String> headerNames) {
    exposedHeaders.addAll(headerNames);
    exposedHeadersString = String.join(",", exposedHeaders);
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
  public CorsHandler allowPrivateNetwork(boolean allow) {
    this.allowPrivateNetwork = allow;
    return this;
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();
    String origin = context.request().headers().get(ORIGIN);
    if (origin == null) {
      // https://fetch.spec.whatwg.org/#cors-protocol-and-http-caches
      // If CORS protocol requirements are more complicated than setting `Access-Control-Allow-Origin` to *
      // or a static origin, `Vary` is to be used.
      if (!starOrigin() && !uniqueOrigin()) {
        Utils.appendToMapIfAbsent(response.headers(), VARY, ",", ORIGIN);
      }
      // Not a CORS request - we don't set any headers and just call the next handler
      context.next();
    } else if (isValidOrigin(origin)) {
      String accessControlRequestMethod = request.headers().get(ACCESS_CONTROL_REQUEST_METHOD);
      if (request.method() == HttpMethod.OPTIONS && accessControlRequestMethod != null) {
        // Pre-flight request
        addCredentialsAndOriginHeader(response, origin);
        if (allowedMethodsString != null) {
          response.putHeader(ACCESS_CONTROL_ALLOW_METHODS, allowedMethodsString);
        }
        if (allowedHeadersString != null) {
          response.putHeader(ACCESS_CONTROL_ALLOW_HEADERS, allowedHeadersString);
        } else {
          if (request.headers().contains(ACCESS_CONTROL_REQUEST_HEADERS)) {
            // echo back the request headers
            response.putHeader(ACCESS_CONTROL_ALLOW_HEADERS, request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS));
            // in this case we need to vary on this header
            Utils.appendToMapIfAbsent(response.headers(), VARY, ",", ACCESS_CONTROL_REQUEST_HEADERS);
          }
        }
        if (maxAgeSeconds != null) {
          response.putHeader(ACCESS_CONTROL_MAX_AGE, maxAgeSeconds);
        }
        if (request.headers().contains(ACCESS_CONTROL_REQUEST_PRIVATE_NETWORK) && allowPrivateNetwork) {
          response.putHeader(ACCESS_CONTROL_ALLOW_PRIVATE_NETWORK, "true");
        }
        response
          // for old Safari
          .putHeader(CONTENT_LENGTH, "0")
          .setStatusCode(204)
          .end();

      } else {
        // when it is possible to determine if only one origin is allowed, we can skip this extra caching header
        if (!starOrigin() && !uniqueOrigin()) {
          Utils.appendToMapIfAbsent(response.headers(), VARY, ",", ORIGIN);
        }
        addCredentialsAndOriginHeader(response, origin);
        if (exposedHeadersString != null) {
          response.putHeader(ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeadersString);
        }
        ((RoutingContextInternal) context).visitHandler(RoutingContextInternal.CORS_HANDLER);
        context.next();
      }
    } else {
      context
        .response()
        .setStatusMessage("CORS Rejected - Invalid origin");
      context
        .fail(403, new IllegalStateException("CORS Rejected - Invalid origin"));
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

  private boolean isValidOrigin(String origin) {

    // * means accept all origins
    if (starOrigin()) {
      return Origin.isValid(origin);
    }

    if(staticOrigins != null) {
      // check whether origin is contained within allowed origin set
      for (Origin allowedOrigin : staticOrigins) {
        if (allowedOrigin.sameOrigin(origin)) {
          return true;
        }
      }
    }

    if(relativeOrigins != null) {
      // check for allowed origin pattern match
      for (Pattern allowedOrigin : relativeOrigins) {
        if (allowedOrigin.matcher(origin).matches()) {
          return true;
        }
      }
    }

    return false;
  }

  private String getAllowedOrigin(String origin) {
    if(starOrigin()) {
      return "*";
    }
    return origin;
  }
}
