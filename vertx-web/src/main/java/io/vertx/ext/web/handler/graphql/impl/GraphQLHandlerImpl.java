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
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;

import java.util.List;

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
    ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();

    List<String> queryParam = rc.queryParam("query");
    if (queryParam.isEmpty()) {
      rc.fail(400, new NoStackTraceThrowable("query param is missing"));
      return;
    }

    builder.query(queryParam.get(0));

    graphQL.executeAsync(builder.build())
      .whenComplete((executionResult, throwable) -> {
        if (throwable == null) {
          rc.response().end(new JsonObject(executionResult.toSpecification()).toBuffer());
        } else {
          rc.fail(throwable);
        }
      });
  }
}
