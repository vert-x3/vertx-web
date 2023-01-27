/*
 * Copyright 2019 Red Hat, Inc.
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

package io.vertx.ext.web.handler.graphql.impl;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.ext.web.impl.Utils;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * @author Thomas Segismont
 */
public class GraphiQLHandlerImpl implements GraphiQLHandler {

  private static final Logger log = LoggerFactory.getLogger(GraphiQLHandlerImpl.class);

  private static final String WEBROOT = "io/vertx/ext/web/handler/graphiql";
  private static final Function<RoutingContext, MultiMap> DEFAULT_GRAPHIQL_REQUEST_HEADERS_FACTORY = rc -> null;

  private final GraphiQLHandlerOptions options;
  private final StaticHandler staticHandler;

  private Function<RoutingContext, MultiMap> graphiQLRequestHeadersFactory = DEFAULT_GRAPHIQL_REQUEST_HEADERS_FACTORY;

  public GraphiQLHandlerImpl(GraphiQLHandlerOptions options) {
    Objects.requireNonNull(options, "options");
    this.options = options;
    staticHandler = options.isEnabled() ? StaticHandler.create(WEBROOT).setCachingEnabled(true).setMaxAgeSeconds(SECONDS.convert(365, DAYS)) : null;
  }

  @Override
  public GraphiQLHandler graphiQLRequestHeaders(Function<RoutingContext, MultiMap> factory) {
    graphiQLRequestHeadersFactory = factory != null ? factory : DEFAULT_GRAPHIQL_REQUEST_HEADERS_FACTORY;
    return this;
  }

  @Override
  public void handle(RoutingContext rc) {
    if (!options.isEnabled()) {
      rc.next();
      return;
    }
    String filename = Utils.pathOffset(rc.normalizedPath(), rc);
    if (filename.isEmpty()) {
      rc.response().setStatusCode(301).putHeader(HttpHeaders.LOCATION, rc.currentRoute().getPath()).end();
      return;
    }
    if ("/".equals(filename) || "/index.html".equals(filename)) {
      String resource = rc.vertx().fileSystem()
        .readFileBlocking(WEBROOT + "/index.html")
        .toString(UTF_8)
        .replace("__VERTX_GRAPHIQL_CONFIG__", replacement(rc));
      rc.response()
        .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
        .putHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=utf8")
        .end(resource);
    } else {
      staticHandler.handle(rc);
    }
  }

  private String replacement(RoutingContext rc) {
    JsonObject json = new JsonObject();
    json.put("httpEnabled", options.isHttpEnabled());
    if (options.getGraphQLUri() != null) {
      json.put("graphQLUri", options.getGraphQLUri());
    }
    json.put("graphQLWSEnabled", options.isGraphQLWSEnabled());
    if (options.getGraphQLWSUri() != null) {
      json.put("graphQLWSUri", options.getGraphQLWSUri());
    }
    if (options.getWsConnectionParams() != null) {
      json.put("wsConnectionParams", options.getWsConnectionParams());
    }
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    Map<String, String> fixedHeaders = options.getHeaders();
    if (fixedHeaders != null) {
      fixedHeaders.forEach(headers::add);
    }
    Function<RoutingContext, MultiMap> rh;
    synchronized (this) {
      rh = this.graphiQLRequestHeadersFactory;
    }
    MultiMap dynamicHeaders = rh.apply(rc);
    if (dynamicHeaders != null) {
      headers.addAll(dynamicHeaders);
    }
    if (!headers.isEmpty()) {
      JsonObject headersJson = new JsonObject();
      headers.forEach(header -> headersJson.put(header.getKey(), header.getValue()));
      json.put("headers", headersJson);
    }
    if (options.getQuery() != null) {
      json.put("query", options.getQuery());
    }
    if (options.getVariables() != null) {
      json.put("parameters", options.getVariables());
    }
    return json.encode();
  }
}
