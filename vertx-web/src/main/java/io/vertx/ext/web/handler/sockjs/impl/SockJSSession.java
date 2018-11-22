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

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.impl.InboundBuffer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

import java.util.Deque;
import java.util.LinkedList;

import static io.vertx.core.buffer.Buffer.buffer;

/**
 * The SockJS session implementation.
 * <p>
 * If multiple instances of the SockJS server are used then instances of this
 * class can be accessed by different threads (not concurrently), so we store
 * it in a shared data map
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
class SockJSSession extends SockJSSocketBase implements Shareable {

  private static final Logger log = LoggerFactory.getLogger(SockJSSession.class);
  private final LocalMap<String, SockJSSession> sessions;
  private final Deque<String> pendingWrites = new LinkedList<>();
  private final InboundBuffer<Buffer> pendingReads;
  private TransportListener listener;
  private boolean closed;
  private boolean openWritten;
  private final String id;
  private final long timeout;
  private final Handler<SockJSSocket> sockHandler;
  private long heartbeatID;
  private long timeoutTimerID = -1;
  private int maxQueueSize = 64 * 1024; // Message queue size is measured in *characters* (not bytes)
  private int messagesSize;
  private Handler<Void> drainHandler;
  private Handler<Void> endHandler;
  private Handler<Throwable> exceptionHandler;
  private boolean handleCalled;
  private SocketAddress localAddress;
  private SocketAddress remoteAddress;
  private String uri;
  private MultiMap headers;
  private Context transportCtx;

  SockJSSession(Vertx vertx, LocalMap<String, SockJSSession> sessions, RoutingContext rc, long heartbeatInterval,
                Handler<SockJSSocket> sockHandler) {
    this(vertx, sessions, rc, null, -1, heartbeatInterval, sockHandler);
  }

  SockJSSession(Vertx vertx, LocalMap<String, SockJSSession> sessions, RoutingContext rc, String id, long timeout, long heartbeatInterval,
                Handler<SockJSSocket> sockHandler) {
    super(vertx, rc.session(), rc.user());
    this.sessions = sessions;
    this.id = id;
    this.timeout = timeout;
    this.sockHandler = sockHandler;
    this.pendingReads = new InboundBuffer<>(vertx.getOrCreateContext());

    // Start a heartbeat

    heartbeatID = vertx.setPeriodic(heartbeatInterval, tid -> {
      if (listener != null) {
        listener.sendFrame("h");
      }
    });
  }

  @Override
  public SockJSSocket write(Buffer buffer) {
    synchronized (this) {
      String msgStr = buffer.toString();
      pendingWrites.add(msgStr);
      this.messagesSize += msgStr.length();
      if (listener != null) {
        Context ctx = transportCtx;
        if (Vertx.currentContext() != ctx) {
          ctx.runOnContext(v -> writePendingMessages());
        } else {
          writePendingMessages();
        }
      }
    }
    return this;
  }

  @Override
  public synchronized SockJSSession handler(Handler<Buffer> handler) {
    pendingReads.handler(handler);
    return this;
  }

  @Override
  public ReadStream<Buffer> fetch(long amount) {
    pendingReads.fetch(amount);
    return this;
  }

  @Override
  public synchronized SockJSSession pause() {
    pendingReads.pause();
    return this;
  }

  @Override
  public synchronized SockJSSession resume() {
    pendingReads.resume();
    return this;
  }

  @Override
  public synchronized SockJSSession setWriteQueueMaxSize(int maxQueueSize) {
    if (maxQueueSize < 1) {
      throw new IllegalArgumentException("maxQueueSize must be >= 1");
    }
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  @Override
  public synchronized boolean writeQueueFull() {
    return messagesSize >= maxQueueSize;
  }

  @Override
  public synchronized SockJSSession drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  @Override
  public synchronized SockJSSession exceptionHandler(Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  @Override
  public synchronized SockJSSession endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  // When the user calls close() we don't actually close the session - unless it's a websocket one
  // Yes, SockJS is weird, but it's hard to work out expected server behaviour when there's no spec
  @Override
  public void close() {
    synchronized (this) {
      if (endHandler != null) {
        endHandler.handle(null);
      }
      closed = true;
      doClose();
    }
  }

  private synchronized void doClose() {
    Context ctx = transportCtx;
    if (ctx != Vertx.currentContext()) {
      ctx.runOnContext(v -> doClose());
    } else {
      if (listener != null && handleCalled) {
        listener.sessionClosed();
      }
    }
  }

  @Override
  public synchronized SocketAddress remoteAddress() {
    return remoteAddress;
  }

  @Override
  public synchronized SocketAddress localAddress() {
    return localAddress;
  }

  @Override
  public synchronized MultiMap headers() {
    return headers;
  }

  @Override
  public synchronized String uri() {
    return uri;
  }

  synchronized boolean isClosed() {
    return closed;
  }

  synchronized void resetListener() {
    listener = null;
    // We set a timer that will kick in and close the session if the client doesn't come back
    // We MUST ALWAYS do this or we can get a memory leak on the server
    setTimer();
  }

  private void cancelTimer() {
    if (timeoutTimerID != -1) {
      vertx.cancelTimer(timeoutTimerID);
    }
  }

  private void setTimer() {
    if (timeout != -1) {
      cancelTimer();
      timeoutTimerID = vertx.setTimer(timeout, id1 -> {
        vertx.cancelTimer(heartbeatID);
        if (listener == null) {
          shutdown();
        }
        if (listener != null) {
          listener.close();
        }
      });
    }
  }

  private synchronized void writePendingMessages() {
    String json = JsonCodec.encode(pendingWrites.toArray());
    listener.sendFrame("a" + json);
    pendingWrites.clear();
    messagesSize = 0;
    if (drainHandler != null && messagesSize <= maxQueueSize / 2) {
      Handler<Void> dh = drainHandler;
      drainHandler = null;
      dh.handle(null);
    }
  }

  synchronized Context context() {
    return transportCtx;
  }

  synchronized void register(HttpServerRequest req, TransportListener lst) {
    this.transportCtx = vertx.getOrCreateContext();
    this.localAddress = req.localAddress();
    this.remoteAddress = req.remoteAddress();
    this.uri = req.uri();
    this.headers = BaseTransport.removeCookieHeaders(req.headers());
    if (closed) {
      // Closed by the application
      writeClosed(lst);
      // And close the listener request
      lst.close();
    } else if (this.listener != null) {
      writeClosed(lst, 2010, "Another connection still open");
      // And close the listener request
      lst.close();
    } else {

      cancelTimer();

      this.listener = lst;

      if (!openWritten) {
        writeOpen(lst);
        sockHandler.handle(this);
        handleCalled = true;
      }

      if (listener != null) {
        if (closed) {
          // Could have already been closed by the user
          writeClosed(lst);
          listener = null;
          lst.close();
        } else {
          if (!pendingWrites.isEmpty()) {
            writePendingMessages();
          }
        }
      }
    }
  }

  // Actually close the session - when the user calls close() the session actually continues to exist until timeout
  // Yes, I know it's weird but that's the way SockJS likes it.
  void shutdown() {
    super.close(); // We must call this or handlers don't get unregistered and we get a leak
    if (heartbeatID != -1) {
      vertx.cancelTimer(heartbeatID);
    }
    if (timeoutTimerID != -1) {
      vertx.cancelTimer(timeoutTimerID);
    }
    if (id != null) {
      // Can be null if websocket session
      sessions.remove(id);
    }

    if (!closed) {
      closed = true;
      if (endHandler != null) {
        endHandler.handle(null);
      }
    }
  }

  private String[] parseMessageString(String msgs) {
    try {
      String[] parts;
      if (msgs.startsWith("[")) {
        //JSON array
        parts = JsonCodec.decodeValue(msgs, String[].class);
      } else {
        //JSON string
        String str = JsonCodec.decodeValue(msgs, String.class);
        parts = new String[] { str };
      }
      return parts;
    } catch (DecodeException e) {
      return null;
    }
  }

  synchronized boolean handleMessages(String messages) {

    String[] msgArr = parseMessageString(messages);

    if (msgArr == null) {
      return false;
    } else {
      for (String msg : msgArr) {
        pendingReads.write(buffer(msg));
      }
      return true;
    }
  }

  synchronized void handleException(Throwable t) {
    if (exceptionHandler != null) {
      exceptionHandler.handle(t);
    } else {
      log.error("Unhandled exception", t);
    }
  }

  void writeClosed(TransportListener lst) {
    writeClosed(lst, 3000, "Go away!");
  }

  private void writeClosed(TransportListener lst, int code, String msg) {
    String sb = "c[" + String.valueOf(code) + ",\"" +
      msg + "\"]";
    lst.sendFrame(sb);
  }

  private synchronized void writeOpen(TransportListener lst) {
    lst.sendFrame("o");
    openWritten = true;
  }
}
