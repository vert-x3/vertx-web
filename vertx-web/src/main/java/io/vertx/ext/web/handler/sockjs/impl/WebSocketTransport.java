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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.impl.Origin;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
class WebSocketTransport extends BaseTransport {

  private static final Logger LOG = LoggerFactory.getLogger(WebSocketTransport.class);

  WebSocketTransport(Vertx vertx,
                     Router router, LocalMap<String, SockJSSession> sessions,
                     SockJSHandlerOptions options,
                     Handler<SockJSSocket> sockHandler) {
    super(vertx, sessions, options);
    final Origin origin = options.getOrigin() != null ? Origin.parse(options.getOrigin()) : null;
    String wsRE = COMMON_PATH_ELEMENT_RE + "websocket";

    router.getWithRegex(wsRE).handler(ctx -> {
      HttpServerRequest req = ctx.request();
      String connectionHeader = req.headers().get(HttpHeaders.CONNECTION);
      if (connectionHeader == null || !connectionHeader.toLowerCase().contains("upgrade")) {
        ctx.response().setStatusCode(400);
        ctx.response().end("Can \"Upgrade\" only to \"WebSocket\".");
      } else {
        if (!Origin.check(origin, ctx)) {
          ctx.fail(403, new IllegalStateException("Invalid Origin"));
          return;
        }
        // upgrade
        req.toWebSocket(toWebSocket -> {
          if (toWebSocket.succeeded()) {
            if (LOG.isTraceEnabled()) {
              LOG.trace("WS, handler");
            }
            // handle the sockjs session as usual
            SockJSSession session = new SockJSSession(vertx, sessions, ctx, options, sockHandler);
            session.register(req, new WebSocketListener(toWebSocket.result(), session));
          } else {
            // the upgrade failed
            ctx.fail(toWebSocket.cause());
          }
        });
      }
    });

    router.getWithRegex(wsRE).handler(rc -> {
      if (LOG.isTraceEnabled()) LOG.trace("WS, get: " + rc.request().uri());
      rc.response().setStatusCode(400);
      rc.response().end("Can \"Upgrade\" only to \"WebSocket\".");
    });

    router.routeWithRegex(wsRE).handler(rc -> {
      if (LOG.isTraceEnabled()) LOG.trace("WS, all: " + rc.request().uri());
      rc.response().putHeader(HttpHeaders.ALLOW, "GET").setStatusCode(405).end();
    });
  }

  private static class WebSocketListener implements TransportListener {

    final ServerWebSocket ws;
    final SockJSSession session;
    boolean closed;

    WebSocketListener(ServerWebSocket ws, SockJSSession session) {
      this.ws = ws;
      this.session = session;
      ws.textMessageHandler(this::handleMessages);
      ws.closeHandler(v -> {
        closed = true;
        session.shutdown();
      });
      ws.exceptionHandler(t -> {
        closed = true;
        session.shutdown();
        session.handleException(t);
      });
    }

    private void handleMessages(String msgs) {
      if (!session.isClosed()) {
        if (msgs.equals("") || msgs.equals("[]")) {
          //Ignore empty frames
        } else if ((msgs.startsWith("[\"") && msgs.endsWith("\"]")) ||
               (msgs.startsWith("\"") && msgs.endsWith("\""))) {
          session.handleMessages(msgs);
        } else {
          //Invalid JSON - we close the connection
          close();
        }
      }
    }

    @Override
    public void sendFrame(String body, Handler<AsyncResult<Void>> handler) {
      if (LOG.isTraceEnabled()) LOG.trace("WS, sending frame");
      if (!closed) {
        ws.writeTextMessage(body, handler);
      } else {
        if (handler != null) {
          handler.handle(Future.failedFuture(ConnectionBase.CLOSED_EXCEPTION));
        }
      }
    }

    public void close() {
      if (!closed) {
        ws.close();
        session.shutdown();
        closed = true;
      }
    }

    public void sessionClosed() {
      session.writeClosed(this);
      closed = true;
      // Asynchronously close the websocket to fix a bug in the SockJS TCK
      // due to the WebSocket client that skip some frames (bug)
      session.context().runOnContext(v -> ws.close());
    }

  }
}
