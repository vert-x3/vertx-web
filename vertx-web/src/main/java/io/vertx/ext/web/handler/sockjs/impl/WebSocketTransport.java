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

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
class WebSocketTransport extends BaseTransport {

  private static final Logger log = LoggerFactory.getLogger(WebSocketTransport.class);

  WebSocketTransport(Vertx vertx,
                     Router router, LocalMap<String, SockJSSession> sessions,
                     SockJSHandlerOptions options,
                     Handler<SockJSSocket> sockHandler) {
    super(vertx, sessions, options);
    String wsRE = COMMON_PATH_ELEMENT_RE + "websocket";

    router.getWithRegex(wsRE).handler(rc -> {
      HttpServerRequest req = rc.request();
      String connectionHeader = req.headers().get(io.vertx.core.http.HttpHeaders.CONNECTION);
      if (connectionHeader == null || !connectionHeader.toLowerCase().contains("upgrade")) {
        rc.response().setStatusCode(400);
        rc.response().end("Can \"Upgrade\" only to \"WebSocket\".");
      } else {
        ServerWebSocket ws = rc.request().upgrade();
        if (log.isTraceEnabled()) log.trace("WS, handler");
        SockJSSession session = new SockJSSession(vertx, sessions, rc, options.getHeartbeatInterval(), sockHandler);
        session.register(req, new WebSocketListener(ws, session));
      }
    });

    router.getWithRegex(wsRE).handler(rc -> {
      if (log.isTraceEnabled()) log.trace("WS, get: " + rc.request().uri());
      rc.response().setStatusCode(400);
      rc.response().end("Can \"Upgrade\" only to \"WebSocket\".");
    });

    router.routeWithRegex(wsRE).handler(rc -> {
      if (log.isTraceEnabled()) log.trace("WS, all: " + rc.request().uri());
      rc.response().putHeader("Allow", "GET").setStatusCode(405).end();
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

    public void sendFrame(final String body) {
      if (log.isTraceEnabled()) log.trace("WS, sending frame");
      if (!closed) {
        ws.writeTextMessage(body);
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
