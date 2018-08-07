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

package io.vertx.ext.web.sstore;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The abstract session class provides a barebones implementation for session storage implementors.
 *
 * This class will contain all the related data required for a session plus a couple of helper methods to verify the
 * integrity and versioning of the data. This checksum is important to reduce the amount of times data is pushed to
 * be stored on a backend.
 *
 * As a Vert.x Web user, you should not have to deal with this class directly but with the public interface that it
 * implements.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public abstract class AbstractSession implements Session {

  private static final char[] HEX = "0123456789abcdef".toCharArray();

  private PRNG prng;

  private String id;
  private long timeout;
  private volatile Map<String, Object> data;
  private long lastAccessed;
  private int version;

  protected void setId(String id) {
    this.id = id;
  }

  protected void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  protected void setData(Map<String, Object> data) {
    if (data != null) {
      this.data = data;
      this.crc = checksum();
    }
  }

  protected void setData(JsonObject data) {
    if (data != null) {
      setData(data.getMap());
    }
  }

  protected void setLastAccessed(long lastAccessed) {
    this.lastAccessed = lastAccessed;
  }

  protected void setVersion(int version) {
    this.version = version;
  }

  // state management
  private boolean destroyed;
  private boolean renewed;
  private String oldId;
  private int crc;

  /**
   * This constructor is <b>mandatory</b> (even though not referenced anywhere) is required for
   * serialization purposes. Do not remove. It is required as part of the contract of the ClusterSerializable
   * interface which some implementations might require.
   */
  public AbstractSession() {
  }

  public AbstractSession(PRNG random) {
    this.prng = random;
  }

  public AbstractSession(PRNG random, long timeout, int length) {
    this.prng = random;
    this.id = generateId(prng, length);
    this.timeout = timeout;
    this.lastAccessed = System.currentTimeMillis();
  }

  public void setPRNG(PRNG prng) {
    this.prng = prng;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public Session regenerateId() {
    if (oldId == null) {
      // keep track of just the first one since the
      // regeneration during the remaining lifecycle are ephemeral
      oldId = id;
    }
    // ids are stored in hex, so the original size is half of the hex encoded length
    id = generateId(prng, oldId.length() / 2);
    renewed = true;
    return this;
  }

  @Override
  public long timeout() {
    return timeout;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    if (isEmpty()) {
      return null;
    }
    Object obj = data().get(key);
    return (T) obj;
  }

  @Override
  public Session put(String key, Object obj) {
    final Map<String, Object> data = data();
    // nulls are handled as remove actions
    if (obj == null) {
      data.remove(key);
    } else {
      data.put(key, obj);
    }
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T remove(String key) {
    if (isEmpty()) {
      return null;
    }
    Object obj = data().remove(key);
    return (T) obj;
  }

  @Override
  public Map<String, Object> data() {
    if (data == null) {
      synchronized (this) {
        // double check since there could already been someone in the lock
        if (data == null) {
          data = new ConcurrentHashMap<>();
          if (destroyed) {
            // pretty much should behave as a regeneration
            regenerateId();
            destroyed = false;
          }
        }
      }
    }
    return data;
  }

  @Override
  public boolean isEmpty() {
    return data == null || data.size() == 0;
  }

  @Override
  public long lastAccessed() {
    return lastAccessed;
  }

  @Override
  public void setAccessed() {
    this.lastAccessed = System.currentTimeMillis();
  }

  @Override
  public void destroy() {
    synchronized (this) {
      destroyed = true;
      data = null;
    }
  }

  @Override
  public boolean isDestroyed() {
    return destroyed;
  }

  @Override
  public boolean isRegenerated() {
    return renewed;
  }

  @Override
  public String oldId() {
    return oldId;
  }

  public int version() {
    return version;
  }

  public void incrementVersion() {
    int old = this.crc;
    // update the checksum
    crc = checksum();

    if (this.crc != old) {
      ++version;
    }
  }

  private static String generateId(PRNG rng, int length) {
    final byte[] bytes = new byte[length];
    rng.nextBytes(bytes);

    final char[] hex = new char[length * 2];
    for (int j = 0; j < length; j++) {
      int v = bytes[j] & 0xFF;
      hex[j * 2] = HEX[v >>> 4];
      hex[j * 2 + 1] = HEX[v & 0x0F];
    }

    return new String(hex);
  }

  protected int crc() {
    return crc;
  }

  protected int checksum() {
    if (isEmpty()) {
      return 0x0000;
    } else {
      int result = 1;

      for (Map.Entry<String, Object> kv : data.entrySet()) {
        String key = kv.getKey();
        result = 31 * result + key.hashCode();
        Object value = kv.getValue();
        if (value != null) {
          result = 31 * result + value.hashCode();
        }
      }

      return result;
    }
  }
}

