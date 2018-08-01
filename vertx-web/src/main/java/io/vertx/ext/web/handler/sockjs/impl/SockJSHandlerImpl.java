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
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.handler.sockjs.Transport;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.vertx.core.buffer.Buffer.*;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class SockJSHandlerImpl implements SockJSHandler, Handler<RoutingContext> {

  private static final Logger log = LoggerFactory.getLogger(SockJSHandlerImpl.class);

  private Vertx vertx;
  private Router router;
  private LocalMap<String, SockJSSession> sessions;
  private SockJSHandlerOptions options;

  public SockJSHandlerImpl(Vertx vertx, SockJSHandlerOptions options) {
    this.vertx = vertx;
    // TODO use clustered map
    this.sessions = vertx.sharedData().getLocalMap("_vertx.sockjssessions");
    this.router = Router.router(vertx);
    this.options = options;
  }

  @Override
  public void handle(RoutingContext context) {
    if (log.isTraceEnabled()) {
      log.trace("Got request in sockjs server: " + context.request().uri());
    }
    router.handleContext(context);
  }

  @Override
  public SockJSHandler bridge(BridgeOptions bridgeOptions) {
    return bridge(bridgeOptions, null);
  }

  @Override
  public SockJSHandler bridge(BridgeOptions bridgeOptions, Handler<BridgeEvent> bridgeEventHandler) {
    socketHandler(new EventBusBridgeImpl(vertx, bridgeOptions, bridgeEventHandler));
    return this;
  }

  @Override
  public SockJSHandler socketHandler(Handler<SockJSSocket> sockHandler) {

    router.route("/").useNormalisedPath(false).handler(rc -> {
      if (log.isTraceEnabled()) log.trace("Returning welcome response");
      rc.response().putHeader("Content-Type", "text/plain; charset=UTF-8").end("Welcome to SockJS!\n");
    });

    // Iframe handlers
    String iframeHTML = IFRAME_TEMPLATE.replace("{{ sockjs_url }}", options.getLibraryURL());
    Handler<RoutingContext> iframeHandler = createIFrameHandler(iframeHTML);

    // Request exactly for iframe.html
    router.get("/iframe.html").handler(iframeHandler);

    // Versioned
    router.getWithRegex("\\/iframe-[^\\/]*\\.html").handler(iframeHandler);

    // Chunking test
    router.post("/chunking_test").handler(createChunkingTestHandler());
    router.options("/chunking_test").handler(BaseTransport.createCORSOptionsHandler(options, "OPTIONS, POST"));

    // Info
    router.get("/info").handler(BaseTransport.createInfoHandler(options));
    router.options("/info").handler(BaseTransport.createCORSOptionsHandler(options, "OPTIONS, GET"));

    // Transports

    Set<String> enabledTransports = new HashSet<>();
    enabledTransports.add(Transport.EVENT_SOURCE.toString());
    enabledTransports.add(Transport.HTML_FILE.toString());
    enabledTransports.add(Transport.JSON_P.toString());
    enabledTransports.add(Transport.WEBSOCKET.toString());
    enabledTransports.add(Transport.XHR.toString());
    Set<String> disabledTransports = options.getDisabledTransports();
    if (disabledTransports == null) {
      disabledTransports = new HashSet<>();
    }
    enabledTransports.removeAll(disabledTransports);

    if (enabledTransports.contains(Transport.XHR.toString())) {
      new XhrTransport(vertx, router, sessions, options, sockHandler);
    }
    if (enabledTransports.contains(Transport.EVENT_SOURCE.toString())) {
      new EventSourceTransport(vertx, router, sessions, options, sockHandler);
    }
    if (enabledTransports.contains(Transport.HTML_FILE.toString())) {
      new HtmlFileTransport(vertx, router, sessions, options, sockHandler);
    }
    if (enabledTransports.contains(Transport.JSON_P.toString())) {
      new JsonPTransport(vertx, router, sessions, options, sockHandler);
    }
    if (enabledTransports.contains(Transport.WEBSOCKET.toString())) {
      new WebSocketTransport(vertx, router, sessions, options, sockHandler);
      new RawWebSocketTransport(vertx, router, sockHandler);
    }

    return this;
  }

  private Handler<RoutingContext> createChunkingTestHandler() {
    return new Handler<RoutingContext>() {

      class TimeoutInfo {
        long timeout;
        Buffer buff;

        TimeoutInfo(long timeout, Buffer buff) {
          this.timeout = timeout;
          this.buff = buff;
        }
      }

      private void setTimeout(List<TimeoutInfo> timeouts, long delay, Buffer buff) {
        timeouts.add(new TimeoutInfo(delay, buff));
      }

      private void runTimeouts(List<TimeoutInfo> timeouts, HttpServerResponse response) {
        Iterator<TimeoutInfo> iter = timeouts.iterator();
        nextTimeout(timeouts, iter, response);
      }

      private void nextTimeout(List<TimeoutInfo> timeouts, Iterator<TimeoutInfo> iter, HttpServerResponse response) {
        if (iter.hasNext()) {
          TimeoutInfo timeout = iter.next();
          vertx.setTimer(timeout.timeout, id -> {
            response.write(timeout.buff);
            nextTimeout(timeouts, iter, response);
          });
        } else {
          timeouts.clear();
        }
      }

      public void handle(RoutingContext rc) {
        rc.response().headers().set("Content-Type", "application/javascript; charset=UTF-8");

        BaseTransport.setCORS(rc);
        rc.response().setChunked(true);

        Buffer h = buffer(2);
        h.appendString("h\n");

        Buffer hs = buffer(2050);
        for (int i = 0; i < 2048; i++) {
          hs.appendByte((byte) ' ');
        }
        hs.appendString("h\n");

        List<TimeoutInfo> timeouts = new ArrayList<>();

        setTimeout(timeouts, 0, h);
        setTimeout(timeouts, 1, hs);
        setTimeout(timeouts, 5, h);
        setTimeout(timeouts, 25, h);
        setTimeout(timeouts, 125, h);
        setTimeout(timeouts, 625, h);
        setTimeout(timeouts, 3125, h);

        runTimeouts(timeouts, rc.response());

      }
    };
  }

  private Handler<RoutingContext> createIFrameHandler(String iframeHTML) {
    String etag = getMD5String(iframeHTML);
    return rc -> {
      try {
        if (log.isTraceEnabled()) log.trace("In Iframe handler");
        if (etag != null && etag.equals(rc.request().getHeader("if-none-match"))) {
          rc.response().setStatusCode(304);
          rc.response().end();
        } else {
          long oneYear = 365 * 24 * 60 * 60 * 1000L;
          String expires = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date(System.currentTimeMillis() + oneYear));
          rc.response().putHeader("Content-Type", "text/html; charset=UTF-8")
            .putHeader("Cache-Control", "public,max-age=31536000")
            .putHeader("Expires", expires).putHeader("ETag", etag).end(iframeHTML);
        }
      } catch (Exception e) {
        log.error("Failed to server iframe", e);
      }
    };
  }

  private static String getMD5String(String str) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
          sb.append(Integer.toHexString(b + 127));
        }
        return sb.toString();
    }
    catch (Exception e) {
        log.error("Failed to generate MD5 for iframe, If-None-Match headers will be ignored");
        return null;
    }
  }

  private static final String IFRAME_TEMPLATE =
      "<!DOCTYPE html>\n" +
      "<html>\n" +
      "<head>\n" +
      "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
      "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
      "  <script src=\"{{ sockjs_url }}\"></script>\n" +
      "  <script>\n" +
      "    document.domain = document.domain;\n" +
      "    SockJS.bootstrap_iframe();\n" +
      "  </script>\n" +
      "</head>\n" +
      "<body>\n" +
      "  <h2>Don't panic!</h2>\n" +
      "  <p>This is a SockJS hidden iframe. It's used for cross domain magic.</p>\n" +
      "</body>\n" +
      "</html>";
}

