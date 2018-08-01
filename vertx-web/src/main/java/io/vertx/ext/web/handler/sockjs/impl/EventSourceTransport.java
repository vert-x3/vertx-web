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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

import static io.vertx.core.buffer.Buffer.buffer;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
class EventSourceTransport extends BaseTransport {

  private static final Logger log = LoggerFactory.getLogger(EventSourceTransport.class);

  EventSourceTransport(Vertx vertx, Router router, LocalMap<String, SockJSSession> sessions, SockJSHandlerOptions options,
                       Handler<SockJSSocket> sockHandler) {
    super(vertx, sessions, options);

    String eventSourceRE = COMMON_PATH_ELEMENT_RE + "eventsource";

    router.getWithRegex(eventSourceRE).handler(rc -> {
      if (log.isTraceEnabled()) log.trace("EventSource transport, get: " + rc.request().uri());
      String sessionID = rc.request().getParam("param0");
      SockJSSession session = getSession(rc, options.getSessionTimeout(), options.getHeartbeatInterval(), sessionID, sockHandler);
      HttpServerRequest req = rc.request();
      session.register(req, new EventSourceListener(options.getMaxBytesStreaming(), rc, session));
    });
  }

  private class EventSourceListener extends BaseListener {

    final int maxBytesStreaming;
    boolean headersWritten;
    int bytesSent;
    boolean closed;

    EventSourceListener(int maxBytesStreaming, RoutingContext rc, SockJSSession session) {
      super(rc, session);
      this.maxBytesStreaming = maxBytesStreaming;
      addCloseHandler(rc.response(), session);
    }

    public void sendFrame(String body) {
      if (log.isTraceEnabled()) log.trace("EventSource, sending frame");
      if (!headersWritten) {
        // event stream data is always UTF8
        // https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#Event_stream_format
        // no need to specify the character encoding
        rc.response().putHeader("Content-Type", "text/event-stream");
        setNoCacheHeaders(rc);
        setJSESSIONID(options, rc);
        rc.response().setChunked(true).write("\r\n");
        headersWritten = true;
      }
      String sb = "data: " +
        body +
        "\r\n\r\n";
      Buffer buff = buffer(sb);
      rc.response().write(buff);
      bytesSent += buff.length();
      if (bytesSent >= maxBytesStreaming) {
        if (log.isTraceEnabled()) log.trace("More than maxBytes sent so closing connection");
        // Reset and close the connection
        close();
      }
    }

    public void close() {
      if (!closed) {
        try {
          session.resetListener();
          rc.response().end();
          rc.response().close();
        } catch (IllegalStateException e) {
          // Underlying connection might already be closed - that's fine
        }
        closed = true;
      }
    }

  }
}
