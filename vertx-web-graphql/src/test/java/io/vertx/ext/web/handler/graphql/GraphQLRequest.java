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

package io.vertx.ext.web.handler.graphql;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static java.util.stream.Collectors.joining;

/**
 * @author Thomas Segismont
 */
public class GraphQLRequest {
  static final String JSON = "application/json";
  static final String GRAPHQL = "application/graphql";

  private HttpMethod method = POST;
  private String httpQueryString;
  private String graphQLQuery;
  private boolean graphQLQueryAsParam;
  private String operationName;
  private boolean operationNameAsParam;
  private JsonObject variables = new JsonObject();
  private boolean variablesAsParam;
  private String contentType = JSON;
  private Buffer requestBody;
  private String locale;

  GraphQLRequest setMethod(HttpMethod method) {
    this.method = method;
    if (method == GET) {
      graphQLQueryAsParam = operationNameAsParam = variablesAsParam = true;
    } else if (method == POST) {
      graphQLQueryAsParam = operationNameAsParam = variablesAsParam = false;
    }
    return this;
  }

  GraphQLRequest setHttpQueryString(String httpQueryString) {
    this.httpQueryString = httpQueryString;
    return this;
  }

  GraphQLRequest setGraphQLQuery(String graphQLQuery) {
    this.graphQLQuery = graphQLQuery;
    return this;
  }

  GraphQLRequest setGraphQLQueryAsParam(boolean graphQLQueryAsParam) {
    this.graphQLQueryAsParam = graphQLQueryAsParam;
    return this;
  }

  GraphQLRequest setOperationName(String operationName) {
    this.operationName = operationName;
    return this;
  }

  GraphQLRequest setOperationNameAsParam(boolean operationNameAsParam) {
    this.operationNameAsParam = operationNameAsParam;
    return this;
  }

  GraphQLRequest addVariable(String name, Object value) {
    if (variables == null) {
      variables = new JsonObject();
    }
    variables.put(name, value);
    return this;
  }

  GraphQLRequest setVariablesAsParam(boolean variablesAsParam) {
    this.variablesAsParam = variablesAsParam;
    return this;
  }

  GraphQLRequest setContentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  GraphQLRequest setRequestBody(Buffer requestBody) {
    this.requestBody = requestBody;
    return this;
  }

  GraphQLRequest setLocale(String locale) {
    this.locale = locale;
    return this;
  }

  void send(HttpClient client, Handler<AsyncResult<JsonObject>> handler) throws Exception {
    send(client, 200, handler);
  }

  void send(HttpClient client, int expectedStatus, Handler<AsyncResult<JsonObject>> handler) throws Exception {
    Promise<JsonObject> promise = Promise.promise();
    promise.future().onComplete(handler);
    Future<HttpClientRequest> fut = client.request(method, 8080, "localhost", getUri());
    fut.onComplete(ar1 -> {
      if (ar1.succeeded()) {
        HttpClientRequest request = ar1.result();
        if (locale != null) {
          request.putHeader("Accept-Language", locale);
        }
        if (contentType != null) {
          request.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
        request.onComplete(ar2 -> {
          if (ar2.succeeded()) {
            HttpClientResponse response = ar2.result();
            if (expectedStatus != response.statusCode()) {
              promise.fail(response.statusCode() + " " + response.statusMessage());
            } else if (response.statusCode() == 200) {
              response.bodyHandler(buffer -> promise.complete(new JsonObject(buffer)));
            } else {
              promise.complete();
            }
          } else {
            promise.fail(ar2.cause());
          }
        });
        Buffer buffer;
        if (requestBody != null) {
          buffer = requestBody;
        } else if (GRAPHQL.equalsIgnoreCase(contentType)) {
          buffer = graphQLQuery != null ? Buffer.buffer(graphQLQuery) : null;
        } else {
          buffer = getJsonBody();
        }
        if (buffer != null) {
          request.end(buffer);
        } else {
          request.end();
        }
      } else {
        promise.fail(ar1.cause());
      }
    });
  }

  private String getUri() {
    StringBuilder uri = new StringBuilder("/graphql");
    if (httpQueryString != null) {
      return uri.append("?").append(httpQueryString).toString();
    }
    Map<String, String> params = new LinkedHashMap<>();
    if (graphQLQueryAsParam && graphQLQuery != null) {
      params.put("query", graphQLQuery);
    }
    if (operationNameAsParam && operationName != null) {
      params.put("operationName", operationName);
    }
    if (variablesAsParam && !variables.isEmpty()) {
      params.put("variables", variables.toString());
    }
    if (!params.isEmpty()) {
      uri.append("?");
      uri.append(params.entrySet().stream()
        .map(entry -> {
          try {
            return entry.getKey() + "=" + encode(entry.getValue());
          } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
          }
        }).collect(joining("&")));
    }
    return uri.toString();
  }

  private Buffer getJsonBody() {
    JsonObject json = new JsonObject();
    if (graphQLQuery != null) {
      json.put("query", graphQLQuery);
    }
    if (operationName != null) {
      json.put("operationName", operationName);
    }
    if (!variables.isEmpty()) {
      json.put("variables", variables);
    }
    return json.isEmpty() ? null : json.toBuffer();
  }

  static String encode(String s) throws UnsupportedEncodingException {
    return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
  }
}
