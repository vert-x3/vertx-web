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

import static io.vertx.core.buffer.Buffer.buffer;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
class XhrTransport extends BaseTransport {

  private static final Logger log = LoggerFactory.getLogger(XhrTransport.class);

  private static final Buffer H_BLOCK;

  static {
    byte[] bytes = new byte[2048 + 1];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte)'h';
    }
    bytes[bytes.length - 1] = (byte)'\n';
    H_BLOCK = buffer(bytes);
  }

  XhrTransport(Vertx vertx, Router router, LocalMap<String, SockJSSession> sessions, SockJSHandlerOptions options,
               Handler<SockJSSocket> sockHandler) {

    super(vertx, sessions, options);

    String xhrBase = COMMON_PATH_ELEMENT_RE;
    String xhrRE = xhrBase + "xhr";
    String xhrStreamRE = xhrBase + "xhr_streaming";

    Handler<RoutingContext> xhrOptionsHandler = createCORSOptionsHandler(options, "OPTIONS, POST");

    router.optionsWithRegex(xhrRE).handler(xhrOptionsHandler);
    router.optionsWithRegex(xhrStreamRE).handler(xhrOptionsHandler);

    registerHandler(router, sockHandler, xhrRE, false, options);
    registerHandler(router, sockHandler, xhrStreamRE, true, options);

    String xhrSendRE = COMMON_PATH_ELEMENT_RE + "xhr_send";

    router.optionsWithRegex(xhrSendRE).handler(xhrOptionsHandler);

    router.postWithRegex(xhrSendRE).handler(rc -> {
      if (log.isTraceEnabled()) log.trace("XHR send, post, " + rc.request().uri());
      String sessionID = rc.request().getParam("param0");
      final SockJSSession session = sessions.get(sessionID);
      if (session != null && !session.isClosed()) {
        handleSend(rc, session);
      } else {
        rc.response().setStatusCode(404);
        setJSESSIONID(options, rc);
        rc.response().end();
      }
    });
  }

  private void registerHandler(Router router, Handler<SockJSSocket> sockHandler, String re,
                               boolean streaming, SockJSHandlerOptions options) {
    router.postWithRegex(re).handler(rc -> {
      if (log.isTraceEnabled()) log.trace("XHR, post, " + rc.request().uri());
      setNoCacheHeaders(rc);
      String sessionID = rc.request().getParam("param0");
      SockJSSession session = getSession(rc, options.getSessionTimeout(), options.getHeartbeatInterval(), sessionID, sockHandler);
      HttpServerRequest req = rc.request();
      session.register(req, streaming? new XhrStreamingListener(options.getMaxBytesStreaming(), rc, session) : new XhrPollingListener(rc, session));
    });
  }

  private void handleSend(RoutingContext rc, SockJSSession session) {
    Buffer body = rc.getBody();
    if (body != null) {
      handleSendMessage(rc, session, body);
    } else if (rc.request().isEnded()) {
      log.error("Request ended before SockJS handler could read the body. Do you have an asynchronous request "
          + "handler before the SockJS handler? If so, add a BodyHandler before the SockJS handler "
          + "(see the docs).");
      rc.fail(500);
    } else {
      rc.request().bodyHandler(buff -> handleSendMessage(rc, session, buff));
    }
  }

  private void handleSendMessage(RoutingContext rc, SockJSSession session, Buffer body) {
    String msgs = body.toString();
    if (msgs.equals("")) {
      rc.response().setStatusCode(500);
      rc.response().end("Payload expected.");
      return;
    }
    if (!session.handleMessages(msgs)) {
      sendInvalidJSON(rc.response());
    } else {
      rc.response().putHeader("Content-Type", "text/plain; charset=UTF-8");
      setNoCacheHeaders(rc);
      setJSESSIONID(options, rc);
      setCORS(rc);
      rc.response().setStatusCode(204);
      rc.response().end();
    }
    if (log.isTraceEnabled()) log.trace("XHR send processed ok");
  }

  private abstract class BaseXhrListener extends BaseListener {

    boolean headersWritten;

    BaseXhrListener(RoutingContext rc, SockJSSession session) {
      super(rc, session);
    }

    public void sendFrame(String body) {
      if (log.isTraceEnabled()) log.trace("XHR sending frame");
      if (!headersWritten) {
        HttpServerResponse resp = rc.response();
        resp.putHeader("Content-Type", "application/javascript; charset=UTF-8");
        setJSESSIONID(options, rc);
        setCORS(rc);
        if (rc.request().version() != HttpVersion.HTTP_1_0) {
          resp.setChunked(true);
        }
        // NOTE that this is streaming!!!
        // Client are not expecting to see Content-Length as we don't know it's value
        headersWritten = true;
      }
    }

    public void close() {
    }
  }

  private class XhrPollingListener extends BaseXhrListener {

    XhrPollingListener(RoutingContext rc, SockJSSession session) {
      super(rc, session);
      addCloseHandler(rc.response(), session);
    }

    public void sendFrame(String body) {
      super.sendFrame(body);
      rc.response().write(body + "\n");
      close();
    }

    public void close() {
      if (log.isTraceEnabled()) log.trace("XHR poll closing listener");
      if (!closed) {
        try {
          session.resetListener();
          rc.response().end();
          rc.response().close();
          closed = true;
        } catch (IllegalStateException e) {
          // Underlying connection might already be closed - that's fine
        }
      }
    }
  }

  private class XhrStreamingListener extends BaseXhrListener {

    int bytesSent;
    int maxBytesStreaming;

    XhrStreamingListener(int maxBytesStreaming, RoutingContext rc, SockJSSession session) {
      super(rc, session);
      this.maxBytesStreaming = maxBytesStreaming;
      addCloseHandler(rc.response(), session);
    }

    public void sendFrame(String body) {
      boolean hr = headersWritten;
      super.sendFrame(body);
      if (!hr) {
        rc.response().write(H_BLOCK);
      }
      String sbody = body + "\n";
      Buffer buff = buffer(sbody);
      rc.response().write(buff);
      bytesSent += buff.length();
      if (bytesSent >= maxBytesStreaming) {
        close();
      }
    }

    public void close() {
      if (log.isTraceEnabled()) log.trace("XHR stream closing listener");
      if (!closed) {
        session.resetListener();
        try {
          rc.response().end();
          rc.response().close();
          closed = true;
        } catch (IllegalStateException e) {
          // Underlying connection might already be closed - that's fine
        }
      }
    }
  }

}
