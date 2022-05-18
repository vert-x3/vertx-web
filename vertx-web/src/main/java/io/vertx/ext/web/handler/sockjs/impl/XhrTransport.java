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
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.PlatformHandler;
import io.vertx.ext.web.handler.SecurityPolicyHandler;
import io.vertx.ext.web.handler.sockjs.SockJSOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

import java.util.Arrays;

import static io.vertx.core.buffer.Buffer.buffer;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
class XhrTransport extends BaseTransport {

  private static final Logger LOG = LoggerFactory.getLogger(XhrTransport.class);

  private static final Buffer H_BLOCK;

  static {
    byte[] bytes = new byte[2048 + 1];
    Arrays.fill(bytes, 0, 2048, (byte) 'h');
    bytes[bytes.length - 1] = (byte)'\n';
    H_BLOCK = buffer(bytes);
  }

  private final Handler<SockJSSocket> sockHandler;

  XhrTransport(Vertx vertx, Router router, LocalMap<String, SockJSSession> sessions, SockJSOptions options, Handler<SockJSSocket> sockHandler) {

    super(vertx, sessions, options);

    this.sockHandler = sockHandler;

    String xhrBase = COMMON_PATH_ELEMENT_RE;
    String xhrRE = xhrBase + "xhr";
    String xhrStreamRE = xhrBase + "xhr_streaming";

    SecurityPolicyHandler xhrOptionsHandler = createCORSOptionsHandler(options, "OPTIONS, POST");

    router.optionsWithRegex(xhrRE)
      .handler(xhrOptionsHandler);

    router.optionsWithRegex(xhrStreamRE)
      .handler(xhrOptionsHandler);

    router.postWithRegex(xhrRE)
      .handler((PlatformHandler) this::handlePostPolling);
    router.postWithRegex(xhrStreamRE)
      .handler((PlatformHandler) this::handlePostStreaming);

    String xhrSendRE = COMMON_PATH_ELEMENT_RE + "xhr_send";

    router.optionsWithRegex(xhrSendRE)
      .handler(xhrOptionsHandler);

    router.postWithRegex(xhrSendRE)
      .handler((PlatformHandler) this::handlePost);
  }
  private void handlePost(RoutingContext ctx) {
    String sessionID = ctx.request().getParam("param0");
    final SockJSSession session = sessions.get(sessionID);
    if (session != null && !session.isClosed()) {
      handleSend(ctx, session);
    } else {
      ctx.response().setStatusCode(404);
      setJSESSIONID(options, ctx);
      ctx.response().end();
    }
  }

  private void handlePostStreaming(RoutingContext ctx) {
    setNoCacheHeaders(ctx);
    String sessionID = ctx.request().getParam("param0");
    SockJSSession session = getSession(ctx, options, sessionID, sockHandler);
    HttpServerRequest req = ctx.request();
    session.register(req, new XhrStreamingListener(options.getMaxBytesStreaming(), ctx, session));
  }

  private void handlePostPolling(RoutingContext ctx) {
    setNoCacheHeaders(ctx);
    String sessionID = ctx.request().getParam("param0");
    SockJSSession session = getSession(ctx, options, sessionID, sockHandler);
    HttpServerRequest req = ctx.request();
    session.register(req, new XhrPollingListener(ctx, session));
  }

  private void handleSend(RoutingContext rc, SockJSSession session) {
    final RequestBody body = rc.body();

    if (!body.available()) {
      // the body handler was not set, so we cannot securely process POST bodies
      // we could just add an ad-hoc body handler but this can lead to DDoS attacks
      // and it doesn't really cover all the uploads, such as multipart, etc...
      // as well as resource cleanup
      rc.fail(500, new NoStackTraceThrowable("BodyHandler is required to process POST requests"));
      return;
    }

    if (body.length() <= 0) {
      rc.response()
        .setStatusCode(500)
        .end("Payload expected.");
      return;
    }

    if (!session.handleMessages(body.asString())) {
      sendInvalidJSON(rc.response());
    } else {
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
      setNoCacheHeaders(rc);
      setJSESSIONID(options, rc);
      setCORSIfNeeded(rc);
      rc.response()
        .setStatusCode(204)
        .end();
    }
    if (LOG.isTraceEnabled()) LOG.trace("XHR send processed ok");
  }

  private abstract class BaseXhrListener extends BaseListener {

    boolean headersWritten;

    BaseXhrListener(RoutingContext rc, SockJSSession session) {
      super(rc, session);
    }

    final void beforeSend() {
      if (LOG.isTraceEnabled()) LOG.trace("XHR sending frame");
      if (!headersWritten) {
        HttpServerResponse resp = rc.response();
        resp.putHeader(HttpHeaders.CONTENT_TYPE, "application/javascript; charset=UTF-8");
        setJSESSIONID(options, rc);
        setCORSIfNeeded(rc);
        if (rc.request().version() != HttpVersion.HTTP_1_0) {
          resp.setChunked(true);
        }
        // NOTE that this is streaming!!!
        // Client are not expecting to see Content-Length as we don't know it's value
        headersWritten = true;
      }
    }

    @Override
    public void close() {
    }
  }

  private class XhrPollingListener extends BaseXhrListener {

    XhrPollingListener(RoutingContext rc, SockJSSession session) {
      super(rc, session);
      addCloseHandler(rc.response(), session);
    }

    @Override
    public void sendFrame(String body, Handler<AsyncResult<Void>> handler) {
      super.beforeSend();
      rc.response().write(body + "\n", handler);
      close();
    }

    @Override
    public void close() {
      if (LOG.isTraceEnabled()) LOG.trace("XHR poll closing listener");
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
    final int maxBytesStreaming;

    XhrStreamingListener(int maxBytesStreaming, RoutingContext rc, SockJSSession session) {
      super(rc, session);
      this.maxBytesStreaming = maxBytesStreaming;
      addCloseHandler(rc.response(), session);
    }

    @Override
    public void sendFrame(String body, Handler<AsyncResult<Void>> handler) {
      boolean hr = headersWritten;
      super.beforeSend();
      if (!hr) {
        rc.response().write(H_BLOCK);
      }
      String sbody = body + "\n";
      Buffer buff = buffer(sbody);
      rc.response().write(buff, handler);
      bytesSent += buff.length();
      if (bytesSent >= maxBytesStreaming) {
        close();
      }
    }

    @Override
    public void close() {
      if (LOG.isTraceEnabled()) LOG.trace("XHR stream closing listener");
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
