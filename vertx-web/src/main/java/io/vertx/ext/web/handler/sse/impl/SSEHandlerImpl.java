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

package io.vertx.ext.web.handler.sse.impl;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.MIMEHeader;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sse.SSEConnection;
import io.vertx.ext.web.handler.sse.SSEHandler;
import io.vertx.ext.web.impl.ParsableMIMEValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SSEHandlerImpl implements SSEHandler {

  private final Collection<MIMEHeader> eventStreamHeader = Collections.singletonList(
    new ParsableMIMEValue("text/event-stream"));
  private Handler<SSEConnection> connectHandler;

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void handle(final RoutingContext context) {
    HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();

    // Determine if the request contains the "Accept: text/event-stream" header.
    List<MIMEHeader> acceptableTypes = context.parsedHeaders().accept();
    if (!acceptableTypes.isEmpty()) {
      MIMEHeader selectedAccept = context.parsedHeaders().findBestUserAcceptedIn(acceptableTypes, eventStreamHeader);

      if (selectedAccept == null) {
        response.setStatusCode(406).end();
        return;
      }
    }

    // Set response headers.
    MultiMap headers = response.headers();
    headers.add(HttpHeaders.CONTENT_TYPE.toString(), "text/event-stream")
      .add(HttpHeaders.CACHE_CONTROL.toString(), "no-cache")
      .add(HttpHeaders.CONNECTION.toString(), "keep-alive");

    response.setChunked(true);
    SSEConnection connection = SSEConnection.create(context);
    request.connection().closeHandler(v -> connection.close());

    if (connectHandler != null) {
      connectHandler.handle(connection);
    }
    response.write("");
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized SSEHandler connectHandler(Handler<SSEConnection> handler) {
    connectHandler = handler;
    return this;
  }

}
