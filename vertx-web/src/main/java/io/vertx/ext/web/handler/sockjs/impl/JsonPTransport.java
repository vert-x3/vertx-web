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
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.net.impl.URIDecoder;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

import java.util.regex.Pattern;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
class JsonPTransport extends BaseTransport {

  private static final Logger LOG = LoggerFactory.getLogger(JsonPTransport.class);

  private static final Pattern CALLBACK_VALIDATION = Pattern.compile("[^a-zA-Z0-9-_.]");

  private final Handler<SockJSSocket> sockHandler;

  JsonPTransport(Vertx vertx, Router router, LocalMap<String, SockJSSession> sessions, SockJSHandlerOptions options, Handler<SockJSSocket> sockHandler) {
    super(vertx, sessions, options);

    this.sockHandler = sockHandler;

    String jsonpRE = COMMON_PATH_ELEMENT_RE + "jsonp";

    router.getWithRegex(jsonpRE)
      .handler(this::handleGet);

    String jsonpSendRE = COMMON_PATH_ELEMENT_RE + "jsonp_send";

    router.postWithRegex(jsonpSendRE)
      .handler(this::handlePost);
  }

  private void handleGet(RoutingContext ctx) {
    String callback = ctx.request().getParam("callback");
    if (callback == null) {
      callback = ctx.request().getParam("c");
      if (callback == null) {
        ctx.response().setStatusCode(500);
        ctx.response().end("\"callback\" parameter required\n");
        return;
      }
    }

    // avoid SWF exploit
    if (callback.length() > 32 || CALLBACK_VALIDATION.matcher(callback).find()) {
      ctx.response().setStatusCode(500);
      ctx.response().end("invalid \"callback\" parameter\n");
      return;
    }

    HttpServerRequest req = ctx.request();
    String sessionID = req.params().get("param0");
    SockJSSession session = getSession(ctx, options, sessionID, sockHandler);
    session.register(req, new JsonPListener(ctx, session, callback));
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

    boolean urlEncoded;
    String ct = rc.request().getHeader(HttpHeaders.CONTENT_TYPE);
    if ("application/x-www-form-urlencoded".equalsIgnoreCase(ct)) {
      urlEncoded = true;
    } else if ("text/plain".equalsIgnoreCase(ct)) {
      urlEncoded = false;
    } else {
      rc.response().setStatusCode(500);
      rc.response().end("Invalid Content-Type");
      return;
    }

    String stringBody = body.asString();

    if (body.length() <= 0 || urlEncoded && (body.length() <= 2 || !stringBody.startsWith("d="))) {
      rc.response().setStatusCode(500).end("Payload expected.");
      return;
    }

    if (urlEncoded) {
      stringBody = URIDecoder.decodeURIComponent(stringBody, true).substring(2);
    }

    if (!session.handleMessages(stringBody)) {
      sendInvalidJSON(rc.response());
    } else {
      setJSESSIONID(options, rc);
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
      setNoCacheHeaders(rc);
      rc.response().end("ok");
      if (LOG.isTraceEnabled()) LOG.trace("send handled ok");
    }
  }

  private class JsonPListener extends BaseListener {

    final String callback;
    boolean headersWritten;

    JsonPListener(RoutingContext rc, SockJSSession session, String callback) {
      super(rc, session);
      this.callback = callback;
      addCloseHandler(rc.response(), session);
    }

    @Override
    public void sendFrame(String body, Handler<AsyncResult<Void>> handler) {
      if (LOG.isTraceEnabled()) LOG.trace("JsonP, sending frame");

      if (!headersWritten) {
        rc.response()
          .setChunked(true)
          // protect against SWF JSONP exploit
          .putHeader("X-Content-Type-Options", "nosniff")
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/javascript; charset=UTF-8");
        setNoCacheHeaders(rc);
        setJSESSIONID(options, rc);
        headersWritten = true;
      }

      body = escapeForJavaScript(body);

      // prepend comment to avoid SWF exploit https://github.com/sockjs/sockjs-node/issues/163
      String sb = "/**/" + callback + "(\"" +
        body +
        "\");\r\n";

      //End the response and close the HTTP connection

      rc.response().write(sb, handler);
      close();
    }

    public void close() {
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
}
