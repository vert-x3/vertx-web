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

package io.vertx.ext.apex.core.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.apex.core.Session;
import io.vertx.ext.apex.core.SessionStore;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SessionImpl implements Session, ClusterSerializable, Shareable {

  private static final Logger log = LoggerFactory.getLogger(SessionImpl.class);

  private final String id;
  private final SessionStore sessionStore;
  private final long timeout;

  private JsonObject data = new JsonObject();
  private long lastAccessed;
  private boolean destroyed;

  public SessionImpl(String id, long timeout, SessionStore sessionStore) {
    this.id = id;
    this.sessionStore = sessionStore;
    this.timeout = timeout;
    this.lastAccessed = System.currentTimeMillis();
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public long timeout() {
    return timeout;
  }

  @Override
  public JsonObject data() {
    return data;
  }

  @Override
  public long lastAccessed() {
    return lastAccessed;
  }

  @Override
  public void accessed() {
    this.lastAccessed = System.currentTimeMillis();
  }

  @Override
  public void destroy() {
    destroyed = true;
    sessionStore.delete(id, res -> {
      if (!res.succeeded()) {
        log.error("Failed to delete session", res.cause());
      }
    });
  }

  @Override
  public boolean isDestroyed() {
    return destroyed;
  }

  @Override
  public SessionStore sessionStore() {
    return sessionStore;
  }

  @Override
  public Buffer writeToBuffer() {
    Buffer buff = Buffer.buffer();
    buff.appendLong(lastAccessed);
    Buffer dataBuf = data.writeToBuffer();
    buff.appendInt(dataBuf.length());
    buff.appendBuffer(dataBuf);
    return buff;
  }

  @Override
  public void readFromBuffer(Buffer buffer) {
    lastAccessed = buffer.getLong(0);
    int len = buffer.getInt(4);
    Buffer buf = buffer.getBuffer(8, 8 + len);
    data = new JsonObject(buf.toString());
  }

}
