/*
 * Copyright 2023 Red Hat, Inc.
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
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.StaticHandlerOptions;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.ext.web.impl.Utils;

import java.util.Map;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Thomas Segismont
 */
public class GraphiQLHandlerImpl implements GraphiQLHandler {

  private static final String WEBROOT = "io/vertx/ext/web/handler/graphiql";
  private static final Function<RoutingContext, MultiMap> DEFAULT_FACTORY = rc -> null;

  private final Vertx vertx;
  private final GraphiQLHandlerOptions options;
  private final Function<RoutingContext, MultiMap> factory;

  public GraphiQLHandlerImpl(Vertx vertx, GraphiQLHandlerOptions options, Function<RoutingContext, MultiMap> factory) {
    this.vertx = vertx;
    this.options = options == null ? new GraphiQLHandlerOptions() : options;
    this.factory = factory == null ? DEFAULT_FACTORY : factory;
  }

  @Override
  public Router router() {
    Router router = Router.router(vertx);
    if (options.isEnabled()) {
      router.get().handler(this::redirectIfNeeded);
      router.get("/").handler(this::serveIndex);
      router.get("/index.html").handler(this::serveIndex);
      router.get().handler(StaticHandler.create(WEBROOT, new StaticHandlerOptions().setCachingEnabled(true).setMaxAgeSeconds(SECONDS.convert(365, DAYS))));
    }
    return router;
  }

  private void redirectIfNeeded(RoutingContext rc) {
    String normalizedPath = rc.normalizedPath();
    if (Utils.pathOffset(normalizedPath, rc).isEmpty()) {
      rc.response().setStatusCode(302).putHeader(HttpHeaders.LOCATION, normalizedPath + "/").end();
      return;
    }
    rc.next();
  }

  private void serveIndex(RoutingContext rc) {
    String resource = rc.vertx().fileSystem()
      .readFileBlocking(WEBROOT + "/index.html")
      .toString(UTF_8)
      .replace("__VERTX_GRAPHIQL_CONFIG__", replacement(rc));
    rc.response()
      .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
      .putHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=utf8")
      .end(resource);
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
    MultiMap dynamicHeaders = factory.apply(rc);
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
