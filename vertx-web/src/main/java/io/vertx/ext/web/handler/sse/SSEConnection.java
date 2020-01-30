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
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sse.impl.SSEConnectionImpl;

import java.util.List;

@VertxGen
public interface SSEConnection {

  static SSEConnection create(RoutingContext context) {
    return new SSEConnectionImpl(context);
  }

  @Fluent
  SSEConnection forward(String address);

  @Fluent
  SSEConnection forward(List<String> addresses);

  @Fluent
  SSEConnection reject(int code);

  @Fluent
  SSEConnection reject(int code, String reason);

  @Fluent
  SSEConnection comment(String comment);

  @Fluent
  SSEConnection retry(Long delay, List<String> data);

  @Fluent
  SSEConnection retry(Long delay, String data);

  @Fluent
  SSEConnection data(List<String> data);

  @Fluent
  SSEConnection data(String data);

  @Fluent
  SSEConnection event(String eventName, List<String> data);

  @Fluent
  SSEConnection event(String eventName, String data);

  @Fluent
  SSEConnection id(String id, List<String> data);

  @Fluent
  SSEConnection id(String id, String data);

  @Fluent
  SSEConnection close();

  boolean rejected();

  String lastId();

  @GenIgnore
  HttpServerRequest request();

}
