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
import io.vertx.core.VertxException;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sse.SSEConnection;

import java.util.ArrayList;
import java.util.List;

public class SSEConnectionImpl implements SSEConnection {

  private static final String MSG_SEPARATOR = "\n";
  private static final String PACKET_SEPARATOR = "\n\n";

  private final RoutingContext context;
  private final List<MessageConsumer<?>> consumers = new ArrayList<>();
  private final List<Handler<SSEConnection>> closeHandlers = new ArrayList<>();

  public SSEConnectionImpl(RoutingContext context) {
    this.context = context;
  }

  @Override
  public SSEConnection forward(String address) {
    consumers.add(context.vertx().eventBus().consumer(address, this::ebMsgHandler));
    return this;
  }

  @Override
  public SSEConnection comment(String comment) {
    context.response().write("comment: " + comment + PACKET_SEPARATOR);
    return this;
  }

  @Override
  public SSEConnection retry(long delay) {
    return writeHeader(SSEHeaders.RETRY, Long.toString(delay));
  }

  @Override
  public SSEConnection data(String data) {
    return writeData(data);
  }

  @Override
  public SSEConnection event(String eventName) {
    return writeHeader(SSEHeaders.EVENT, eventName);
  }

  @Override
  public SSEConnection id(String id) {
    return writeHeader(SSEHeaders.ID, id);
  }

  @Override
  public SSEConnection close() {
    try {context.response().end(); // best effort
    } catch(VertxException | IllegalStateException e) {
      // connection has already been closed by the browser
      // do not log to avoid performance issues (ddos issue if client opening and closing alot of connections abruptly)
    }
    consumers.forEach(MessageConsumer::unregister);
    closeHandlers.forEach(consumer -> consumer.handle(this));
    return this;
  }

  @Override
  public SSEConnection closeHandler(Handler<SSEConnection> connection) {
    closeHandlers.add(connection);
    return this;
  }

  @Override
  public HttpServerRequest request() {
    return context.request();
  }

  @Override
  public String lastId() {
    return request().getHeader(SSEHeaders.LAST_EVENT_ID.toString());
  }

  private SSEConnection writeHeader(SSEHeaders headerName, String headerValue) {
    context.response().write(headerName + ": " + headerValue + MSG_SEPARATOR);
    return this;
  }

  private SSEConnection writeData(String data) {
    context.response().write("data: " + data + PACKET_SEPARATOR);
    return this;
  }

  private void ebMsgHandler(Message<?> msg) {
    MultiMap headers = msg.headers();
    String eventName = headers.get(SSEHeaders.EVENT.toString());
    String id = headers.get(SSEHeaders.ID.toString());
    String data = msg.body() == null ? "" : msg.body().toString();
    if (eventName != null) {
      event(eventName);
      data(data);
    }
    if (id != null) {
      id(id);
      data(data);
    }
    if (eventName == null && id == null) {
      data(data);
    }
  }
}
