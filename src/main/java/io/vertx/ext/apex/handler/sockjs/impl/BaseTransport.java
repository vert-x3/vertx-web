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

package io.vertx.ext.apex.handler.sockjs.impl;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VoidHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.StringEscapeUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.apex.handler.sockjs.SockJSSocket;
import io.vertx.ext.apex.handler.sockjs.Transport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
class BaseTransport {

  private static final Logger log = LoggerFactory.getLogger(BaseTransport.class);

  protected final Vertx vertx;
  protected final LocalMap<String, SockJSSession> sessions;
  protected SockJSHandlerOptions options;

  protected static final String COMMON_PATH_ELEMENT_RE = "\\/[^\\/\\.]+\\/([^\\/\\.]+)\\/";

  private static final long RAND_OFFSET = 2L << 30;

  public BaseTransport(Vertx vertx, LocalMap<String, SockJSSession> sessions, SockJSHandlerOptions options) {
    this.vertx = vertx;
    this.sessions = sessions;
    this.options = options;
  }

  protected SockJSSession getSession(RoutingContext rc, long timeout, long heartbeatPeriod, String sessionID,
                                     Handler<SockJSSocket> sockHandler) {
    SockJSSession session = sessions.get(sessionID);
    if (session == null) {
      session = new SockJSSession(vertx, sessions, rc, sessionID, timeout, heartbeatPeriod, sockHandler);
      sessions.put(sessionID, session);
    }
    return session;
  }

  protected void sendInvalidJSON(HttpServerResponse response) {
    if (log.isTraceEnabled()) log.trace("Broken JSON");
    response.setStatusCode(500);
    response.end("Broken JSON encoding.");
  }

  protected String escapeForJavaScript(String str) {
    try {
       str = StringEscapeUtils.escapeJavaScript(str);
    } catch (Exception e) {
      log.error("Failed to escape", e);
      str = null;
    }
    return str;
  }

  protected static abstract class BaseListener implements TransportListener {
    protected final RoutingContext rc;
    protected final SockJSSession session;
    protected boolean closed;

    protected BaseListener(RoutingContext rc, SockJSSession session) {
      this.rc = rc;
      this.session = session;
    }
    protected void addCloseHandler(HttpServerResponse resp, final SockJSSession session) {
      resp.closeHandler(new VoidHandler() {
        public void handle() {
          if (log.isTraceEnabled()) log.trace("Connection closed (from client?), closing session");
          // Connection has been closed from the client or network error so
          // we remove the session
          session.shutdown();
          closed = true;
        }
      });
    }

    @Override
    public void sessionClosed() {
      session.writeClosed(this);
      close();
    }
  }

  static void setJSESSIONID(SockJSHandlerOptions options, RoutingContext rc) {
    String cookies = rc.request().getHeader("cookie");
    if (options.isInsertJSESSIONID()) {
      //Preserve existing JSESSIONID, if any
      if (cookies != null) {
        String[] parts;
        if (cookies.contains(";")) {
          parts = cookies.split(";");
        } else {
          parts = new String[] {cookies};
        }
        for (String part: parts) {
          if (part.startsWith("JSESSIONID")) {
            cookies = part + "; path=/";
            break;
          }
        }
      }
      if (cookies == null) {
        cookies = "JSESSIONID=dummy; path=/";
      }
      rc.response().putHeader("Set-Cookie", cookies);
    }
  }

  static void setCORS(RoutingContext rc) {
    String origin = rc.request().getHeader("origin");
    if (origin == null || "null".equals(origin)) {
      origin = "*";
    } else {
      rc.response().putHeader("Access-Control-Allow-Credentials", "true");
    }
    rc.response().putHeader("Access-Control-Allow-Origin", origin);
    String hdr = rc.request().getHeader("Access-Control-Request-Headers");
    if (hdr != null) {
      rc.response().putHeader("Access-Control-Allow-Headers", hdr);
    }
  }

  static Handler<RoutingContext> createInfoHandler(final SockJSHandlerOptions options) {
    return new Handler<RoutingContext>() {
      boolean websocket = !options.getDisabledTransports().contains(Transport.WEBSOCKET.toString());
      public void handle(RoutingContext rc) {
        if (log.isTraceEnabled()) log.trace("In Info handler");
        rc.response().putHeader("Content-Type", "application/json; charset=UTF-8");
        setNoCacheHeaders(rc);
        JsonObject json = new JsonObject();
        json.put("websocket", websocket);
        json.put("cookie_needed", options.isInsertJSESSIONID());
        json.put("origins", new JsonArray().add("*:*"));
        // Java ints are signed, so we need to use a long and add the offset so
        // the result is not negative
        json.put("entropy", RAND_OFFSET + new Random().nextInt());
        setCORS(rc);
        rc.response().end(json.encode());
      }
    };
  }

  static void setNoCacheHeaders(RoutingContext rc) {
    rc.response().putHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
  }

  static Handler<RoutingContext> createCORSOptionsHandler(SockJSHandlerOptions options, String methods) {
    return rc -> {
      if (log.isTraceEnabled()) log.trace("In CORS options handler");
      rc.response().putHeader("Cache-Control", "public,max-age=31536000");
      long oneYearSeconds = 365 * 24 * 60 * 60;
      long oneYearms = oneYearSeconds * 1000;
      String expires = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date(System.currentTimeMillis() + oneYearms));
      rc.response().putHeader("Expires", expires)
        .putHeader("Access-Control-Allow-Methods", methods)
        .putHeader("Access-Control-Max-Age", String.valueOf(oneYearSeconds));
      setCORS(rc);
      setJSESSIONID(options, rc);
      rc.response().setStatusCode(204);
      rc.response().end();
    };
  }

  // We remove cookie headers for security reasons. See https://github.com/sockjs/sockjs-node section on
  // Authorisation
  static MultiMap removeCookieHeaders(MultiMap headers) {
    // We don't want to remove the JSESSION cookie.
    String jsessionid = null;
    for (String cookie : headers.getAll("cookie")) {
      if (cookie.startsWith("JSESSIONID=")) {
        jsessionid = cookie;
      }
    }
    headers.remove("cookie");

    // Add back jsessionid cookie header
    if (jsessionid != null) {
      headers.add("cookie", jsessionid);
    }

    return headers;
  }
}
