/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web.handler.sockjs.impl;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.PlatformHandler;
import io.vertx.ext.web.handler.ProtocolUpgradeHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.impl.Origin;

import static io.vertx.core.http.HttpHeaders.*;
import static io.vertx.ext.web.impl.Utils.canUpgradeToWebsocket;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
class RawWebSocketTransport {

  private final Origin origin;
  private final Vertx vertx;
  private final SockJSHandlerOptions options;
  private final Handler<SockJSSocket> sockHandler;

  RawWebSocketTransport(Vertx vertx, Router router, SockJSHandlerOptions options, Handler<SockJSSocket> sockHandler) {

    this.vertx = vertx;
    this.options = options;
    this.origin = options.getOrigin() != null ? Origin.parse(options.getOrigin()) : null;
    this.sockHandler = sockHandler;

    router.get("/websocket")
      .handler((ProtocolUpgradeHandler) this::handleGet);

    router.route("/websocket")
      .handler((PlatformHandler) rc -> rc.response().putHeader(ALLOW, "GET").setStatusCode(405).end());
  }

  private void handleGet(RoutingContext ctx) {
    HttpServerRequest req = ctx.request();
    if (!canUpgradeToWebsocket(req)) {
      ctx.response().setStatusCode(400);
      ctx.response().end("Can \"Upgrade\" only to \"WebSocket\".");
      return;
    }

    if (!Origin.check(origin, ctx)) {
      ctx.fail(403, new IllegalStateException("Invalid Origin"));
      return;
    }

    // upgrade
    req
      .toWebSocket()
      .onFailure(ctx::fail)
      .onSuccess(socket -> {
        // handle the sockjs session as usual
        SockJSSocket sock = new RawWSSockJSSocket(vertx, ctx, options, socket);
        sockHandler.handle(sock);
      });
  }

  private static class RawWSSockJSSocket extends SockJSSocketBase {

    final ServerWebSocket ws;
    MultiMap headers;
    boolean closed;

    RawWSSockJSSocket(Vertx vertx, RoutingContext rc, SockJSHandlerOptions options, ServerWebSocket ws) {
      super(vertx, rc, options);
      this.ws = ws;
      ws.closeHandler(v -> {
        // Make sure the writeHandler gets unregistered
        synchronized (RawWSSockJSSocket.this) {
          closed = true;
        }
        RawWSSockJSSocket.super.close();
      });
    }

    @Override
    public SockJSSocket handler(Handler<Buffer> handler) {
      ws.binaryMessageHandler(handler);
      ws.textMessageHandler(textMessage -> handler.handle(Buffer.buffer(textMessage)));
      return this;
    }

    @Override
    public SockJSSocket pause() {
      ws.pause();
      return this;
    }

    @Override
    public SockJSSocket resume() {
      ws.resume();
      return this;
    }

    @Override
    public SockJSSocket fetch(long amount) {
      ws.fetch(amount);
      return this;
    }

    @Override
    public Future<Void> write(Buffer data) {
      if (!closed) {
        return ws.writeBinaryMessage(data);
      }
      final Promise<Void> promise = ((VertxInternal) vertx).promise();
      vertx.runOnContext(v -> promise.fail(ConnectionBase.CLOSED_EXCEPTION));
      return promise.future();
    }

    @Override
    public Future<Void> write(String data) {
      if (!closed) {
        return ws.writeTextMessage(data);
      }
      final Promise<Void> promise = ((VertxInternal) vertx).promise();
      vertx.runOnContext(v -> promise.fail(ConnectionBase.CLOSED_EXCEPTION));
      return promise.future();
    }

    @Override
    public SockJSSocket setWriteQueueMaxSize(int maxQueueSize) {
      ws.setWriteQueueMaxSize(maxQueueSize);
      return this;
    }

    @Override
    public boolean writeQueueFull() {
      return ws.writeQueueFull();
    }

    @Override
    public SockJSSocket drainHandler(Handler<Void> handler) {
      ws.drainHandler(handler);
      return this;
    }

    @Override
    public SockJSSocket exceptionHandler(Handler<Throwable> handler) {
      ws.exceptionHandler(handler);
      return this;
    }

    @Override
    public SockJSSocket endHandler(Handler<Void> endHandler) {
      ws.endHandler(endHandler);
      return this;
    }

    @Override
    public SockJSSocket closeHandler(Handler<Void> closeHandler) {
      ws.closeHandler(closeHandler);
      return this;
    }

    @Override
    public void close() {
      synchronized (this) {
        if (closed) {
          return;
        }
        closed = true;
      }
      super.close();
      ws.close();
    }

    @Override
    public void closeAfterSessionExpired() {
      this.close((short) 1001, "Session expired");
    }

    @Override
    public void close(int statusCode, String reason) {
      synchronized (this) {
        if (closed) {
          return;
        }
        closed = true;
      }
      super.close();
      ws.close((short) statusCode, reason);
    }

    @Override
    public SocketAddress remoteAddress() {
      return ws.remoteAddress();
    }

    @Override
    public SocketAddress localAddress() {
      return ws.localAddress();
    }

    @Override
    public MultiMap headers() {
      if (headers == null) {
        headers = BaseTransport.removeCookieHeaders(ws.headers());
      }
      return headers;
    }

    @Override
    public String uri() {
      return ws.uri();
    }
  }
}
