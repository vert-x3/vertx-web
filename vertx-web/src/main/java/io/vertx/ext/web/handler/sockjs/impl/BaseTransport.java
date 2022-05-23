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

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.PlatformHandler;
import io.vertx.ext.web.handler.SecurityPolicyHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.handler.sockjs.Transport;
import io.vertx.ext.web.impl.RoutingContextInternal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
class BaseTransport {

  private static final Logger LOG = LoggerFactory.getLogger(BaseTransport.class);

  protected final Vertx vertx;
  protected final LocalMap<String, SockJSSession> sessions;
  protected final SockJSHandlerOptions options;

  static final String COMMON_PATH_ELEMENT_RE = "\\/[^\\/\\.]+\\/([^\\/\\.]+)\\/";

  public BaseTransport(Vertx vertx, LocalMap<String, SockJSSession> sessions, SockJSHandlerOptions options) {
    this.vertx = vertx;
    this.sessions = sessions;
    this.options = options;
  }

  protected SockJSSession getSession(RoutingContext rc, SockJSHandlerOptions options, String sessionID, Handler<SockJSSocket> sockHandler) {
    return sessions.computeIfAbsent(sessionID, s -> new SockJSSession(vertx, sessions, rc, s, options, sockHandler));
  }

  protected void sendInvalidJSON(HttpServerResponse response) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Broken JSON");
    }
    response.setStatusCode(500);
    response.end("Broken JSON encoding.");
  }

  protected String escapeForJavaScript(String str) {
    try {
       str = StringEscapeUtils.escapeJavaScript(str);
    } catch (Exception e) {
      LOG.error("Failed to escape", e);
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
      resp.closeHandler(v -> {
          if (LOG.isTraceEnabled()) {
            LOG.trace("Connection closed (from client?), closing session");
          }
          // Connection has been closed from the client or network error so
          // we remove the session
          session.shutdown();
          closed = true;
      });
    }

    @Override
    public void sessionClosed() {
      session.writeClosed(this);
      close();
    }
  }

  static void setJSESSIONID(SockJSHandlerOptions options, RoutingContext rc) {
    String cookies = rc.request().getHeader(COOKIE);
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
      rc.response().putHeader(SET_COOKIE, cookies);
    }
  }

  static void setCORSIfNeeded(RoutingContext rc) {
    if (!((RoutingContextInternal) rc).seenHandler(RoutingContextInternal.CORS_HANDLER)) {
      HttpServerRequest req = rc.request();
      String origin = req.getHeader(ORIGIN);
      if (origin == null) {
        origin = "*";
      }
      req.response().headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
      if ("*".equals(origin)) {
        req.response().headers().set(ACCESS_CONTROL_ALLOW_CREDENTIALS, "false");
      } else {
        req.response().headers().set(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
      }
      String hdr = req.headers().get(ACCESS_CONTROL_REQUEST_HEADERS);
      if (hdr != null) {
        req.response().headers().set(ACCESS_CONTROL_ALLOW_HEADERS, hdr);
      }
    }
  }

  static PlatformHandler createInfoHandler(final SockJSHandlerOptions options, final VertxContextPRNG prng) {
    final long offset = 2L << 30;

    return new PlatformHandler() {
      final boolean websocket = !options.getDisabledTransports().contains(Transport.WEBSOCKET.toString());

      @Override
      public void handle(RoutingContext rc) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("In Info handler");
        }
        rc.response().putHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
        setNoCacheHeaders(rc);
        JsonObject json = new JsonObject();
        json.put("websocket", websocket);
        json.put("cookie_needed", options.isInsertJSESSIONID());
        json.put("origins", new JsonArray().add("*:*"));
        // Java ints are signed, so we need to use a long and add the offset so
        // the result is not negative
        json.put("entropy", offset + prng.nextInt());
        setCORSIfNeeded(rc);
        rc.response().end(json.encode());
      }
    };
  }

  static void setNoCacheHeaders(RoutingContext rc) {
    rc.response().putHeader(CACHE_CONTROL, "no-store, no-cache, no-transform, must-revalidate, max-age=0");
  }

  static SecurityPolicyHandler createCORSOptionsHandler(SockJSHandlerOptions options, String methods) {
    return rc -> {
      if (LOG.isTraceEnabled()) {
        LOG.trace("In CORS options handler");
      }
      rc.response().putHeader(CACHE_CONTROL, "public,max-age=31536000");
      long oneYearSeconds = 365 * 24 * 60 * 60;
      long oneYearms = oneYearSeconds * 1000;
      String expires = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date(System.currentTimeMillis() + oneYearms));
      rc.response()
        .putHeader(EXPIRES, expires)
        .putHeader(ACCESS_CONTROL_ALLOW_METHODS, methods)
        .putHeader(ACCESS_CONTROL_MAX_AGE, String.valueOf(oneYearSeconds));
      setCORSIfNeeded(rc);
      setJSESSIONID(options, rc);
      rc.response().setStatusCode(204);
      rc.response().end();
    };
  }

  // We remove cookie headers for security reasons. See https://github.com/sockjs/sockjs-node section on
  // Authorization
  static MultiMap removeCookieHeaders(MultiMap headers) {
    // We don't want to remove the JSESSION cookie.
    String cookieHeader = headers.get(COOKIE);
    if (cookieHeader != null) {
      headers.remove(COOKIE);
      Set<Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
      for (Cookie cookie: nettyCookies) {
        if (cookie.name().equals("JSESSIONID")) {
          headers.add(COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
          break;
        }
      }
    }
    return headers;
  }
}
