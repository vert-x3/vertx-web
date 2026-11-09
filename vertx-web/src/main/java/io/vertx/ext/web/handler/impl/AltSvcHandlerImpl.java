/*
 * Copyright 2026 Red Hat, Inc.
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

import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.HostAndPort;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AltSvcHandler;
import io.vertx.ext.web.handler.AltSvcOptions;
import io.vertx.ext.web.impl.HttpConnectionLocal;
import io.vertx.ext.web.impl.Origin;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class AltSvcHandlerImpl implements AltSvcHandler {

  private final Map<String, String> origins;
  private final HttpConnectionLocal<Set<String>> announcedOrigins = new HttpConnectionLocal<>();

  public AltSvcHandlerImpl(AltSvcOptions options) {
    if (options == null) {
      throw new IllegalArgumentException("options cannot be null");
    }
    this.origins = normalizeOrigins(options.getOrigins());
  }

  @Override
  public void handle(RoutingContext ctx) {
    String origin = origin(ctx);
    String header = origin == null ? null : origins.get(origin);
    if (header != null && announce(ctx.request().connection(), origin)) {
      if (requiresImmediateWrite(ctx.request().version())) {
        ctx.response().writeAltSvc(header);
      } else {
        ctx.addHeadersEndHandler(v -> ctx.response().writeAltSvc(header));
      }
    }
    ctx.next();
  }

  private boolean announce(HttpConnection connection, String origin) {
    Set<String> origins = announcedOrigins.getOrCreate(connection, HashSet::new);
    synchronized (origins) {
      return origins.add(origin);
    }
  }

  private static boolean requiresImmediateWrite(HttpVersion version) {
    return version == HttpVersion.HTTP_2 || version == HttpVersion.HTTP_3;
  }

  private static Map<String, String> normalizeOrigins(Map<String, String> origins) {
    Map<String, String> normalized = new HashMap<>();
    origins.forEach((origin, alternativeService) ->
      normalized.put(normalizeOrigin(origin), normalizeService(alternativeService)));
    return Collections.unmodifiableMap(normalized);
  }

  private static String origin(RoutingContext ctx) {
    HostAndPort authority = ctx.request().authority();
    if (authority == null) {
      return null;
    }
    return normalizeOrigin(ctx.request().scheme() + "://" + authority);
  }

  private static String normalizeOrigin(String origin) {
    String encoded = Origin.parse(origin).encode();
    if (encoded == null) {
      throw new IllegalArgumentException("Unsupported origin: " + origin);
    }
    return encoded;
  }

  private static String normalizeService(String alternativeService) {
    int index = alternativeService.indexOf(':');
    if (alternativeService.indexOf('=') != -1 || index == -1) {
      return alternativeService;
    }
    return alternativeService.substring(0, index) + "=\"" + alternativeService.substring(index + 1) + "\"";
  }
}
