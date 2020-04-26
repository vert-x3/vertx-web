/*
 * Copyright 2020 Red Hat, Inc.
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

package io.vertx.ext.web.handler.sse;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sse.impl.SSEConnectionImpl;

@VertxGen
public interface SSEConnection {

  static SSEConnection create(RoutingContext context) {
    return new SSEConnectionImpl(context);
  }

  @Fluent
  SSEConnection forward(String address);

  @Fluent
  SSEConnection comment(String comment);

  @Fluent
  SSEConnection retry(long delay);

  @Fluent
  SSEConnection data(String data);

  @Fluent
  SSEConnection event(String eventName);

  @Fluent
  SSEConnection id(String id);

  @Fluent
  SSEConnection close();

  @Fluent
  SSEConnection closeHandler(Handler<SSEConnection> connection);

  String lastId();

  @GenIgnore
  HttpServerRequest request();

}
