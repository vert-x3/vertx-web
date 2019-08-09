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
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import io.vertx.ext.web.impl.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;

/**
 * @author Thomas Segismont
 */
public class GraphiQLHandlerImpl implements GraphiQLHandler {

  private static final Logger log = LoggerFactory.getLogger(GraphiQLHandlerImpl.class);

  private static final Function<RoutingContext, MultiMap> DEFAULT_GRAPHIQL_REQUEST_HEADERS_FACTORY = rc -> null;

  private final GraphiQLHandlerOptions options;

  private Function<RoutingContext, MultiMap> graphiQLRequestHeadersFactory = DEFAULT_GRAPHIQL_REQUEST_HEADERS_FACTORY;

  public GraphiQLHandlerImpl(GraphiQLHandlerOptions options) {
    Objects.requireNonNull(options, "options");
    this.options = options;
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
    HttpServerResponse response = rc.response();
    String filename = Utils.pathOffset(rc.normalisedPath(), rc);
    if (filename.isEmpty()) {
      if (rc.parsedHeaders().accept().stream().map(MIMEHeader::subComponent).anyMatch(sub -> "html".equalsIgnoreCase(sub))) {
        rc.response().setStatusCode(301).putHeader(HttpHeaders.LOCATION, rc.currentRoute().getPath()).end();
      } else {
        rc.next();
      }
      return;
    }
    if (filename.equals("/")) {
      filename = "/index.html";
    }
    String resource = loadResource(filename);
    if (resource == null) {
      rc.next();
      return;
    }
    if (filename.equals("/index.html")) {
      resource = resource.replace("__VERTX_GRAPHIQL_CONFIG__", replacement(rc));
      response.putHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    } else {
      response.putHeader(HttpHeaders.CACHE_CONTROL, "max-age=31536000");
    }
    String contentType = MimeMapping.getMimeTypeForFilename(filename);
    if (contentType != null) {
      if (contentType.startsWith("text")) {
        response.putHeader(HttpHeaders.CONTENT_TYPE, contentType + ";charset=utf8");
      } else {
        response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
      }
    }
    response.end(resource);
  }

  private String loadResource(String relativePath) {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource("io/vertx/ext/web/handler/graphiql" + relativePath);
    if (resource == null) {
      return null;
    }
    try (InputStream stream = resource.openStream()) {
      return new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
    } catch (IOException e) {
      if (log.isTraceEnabled()) {
        log.trace("Unable to load resource: " + relativePath, e);
      }
      return null;
    }
  }

  private String replacement(RoutingContext rc) {
    JsonObject json = new JsonObject();
    if (options.getGraphQLUri() != null) {
      json.put("graphQLUri", options.getGraphQLUri());
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
