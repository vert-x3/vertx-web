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

import graphql.ExecutionInput;
import graphql.GraphQL;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;

import java.util.List;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;

/**
 * @author Thomas Segismont
 */
public class GraphQLHandlerImpl implements GraphQLHandler {

  private final GraphQL graphQL;

  public GraphQLHandlerImpl(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @Override
  public void handle(RoutingContext rc) {
    HttpMethod method = rc.request().method();
    if (method == GET) {
      handleGet(rc);
    } else if (method == POST) {
      Buffer body = rc.getBody();
      if (body == null) {
        rc.request().bodyHandler(buffer -> handlePost(rc, buffer));
      } else {
        handlePost(rc, body);
      }
    } else {
      rc.fail(405);
    }
  }

  private void handleGet(RoutingContext rc) {
    List<String> queryParam = rc.queryParam("query");
    if (queryParam.isEmpty()) {
      failQueryMissing(rc);
      return;
    }
    execute(rc, queryParam.get(0));
  }

  private void handlePost(RoutingContext rc, Buffer body) {
    List<String> queryParam = rc.queryParam("query");
    if (!queryParam.isEmpty()) {
      execute(rc, queryParam.get(0));
      return;
    }

    String contentType = rc.request().headers().get(HttpHeaders.CONTENT_TYPE);
    if (contentType == null) {
      contentType = "application/json";
    } else {
      contentType = contentType.toLowerCase();
    }

    switch (contentType) {

      case "application/json":
        try {
          JsonObject bodyAsJson = new JsonObject(body);
          String query = bodyAsJson.getString("query");
          if (query == null) {
            failQueryMissing(rc);
          } else {
            execute(rc, query);
          }
        } catch (Exception e) {
          rc.fail(400, e);
        }
        break;

      case "application/graphql":
        execute(rc, body.toString());
        break;

      default:
        rc.fail(415);
    }
  }

  private void execute(RoutingContext rc, String query) {
    ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();

    builder.query(query);

    graphQL.executeAsync(builder.build())
      .whenComplete((executionResult, throwable) -> {
        if (throwable == null) {
          rc.response().end(new JsonObject(executionResult.toSpecification()).toBuffer());
        } else {
          rc.fail(throwable);
        }
      });
  }

  private void failQueryMissing(RoutingContext rc) {
    rc.fail(400, new NoStackTraceThrowable("Query is missing"));
  }
}
