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

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.ClusterSerializable;
import io.vertx.ext.auth.VertxContextPRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.sstore.impl.SessionInternal;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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
public abstract class AbstractSession implements Session, SessionInternal {

  private static final Charset UTF8 = StandardCharsets.UTF_8;

  private static final byte TYPE_LONG = 1;
  private static final byte TYPE_INT = 2;
  private static final byte TYPE_SHORT = 3;
  private static final byte TYPE_BYTE = 4;
  private static final byte TYPE_DOUBLE = 5;
  private static final byte TYPE_FLOAT = 6;
  private static final byte TYPE_CHAR = 7;
  private static final byte TYPE_BOOLEAN = 8;
  private static final byte TYPE_STRING = 9;
  private static final byte TYPE_BUFFER = 10;
  private static final byte TYPE_BYTES = 11;
  private static final byte TYPE_CLUSTER_SERIALIZABLE = 13;

  private static final char[] HEX = "0123456789abcdef".toCharArray();

  private VertxContextPRNG prng;

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

  public AbstractSession(VertxContextPRNG random) {
    this.prng = random;
  }

  public AbstractSession(VertxContextPRNG random, long timeout, int length) {
    this.prng = random;
    this.id = generateId(prng, length);
    this.timeout = timeout;
    this.lastAccessed = System.currentTimeMillis();
  }

  public void setPRNG(VertxContextPRNG prng) {
    this.prng = prng;
  }

  @Override
  public void flushed(boolean skipCrc) {
    renewed = false;
    if (oldId != null) {
      if (!skipCrc) {
        crc = checksum();
      }
      oldId = null;
    }
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
  public <T> @Nullable T get(String key) {
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
  public Session putIfAbsent(String key, Object obj) {
    data()
      .putIfAbsent(key, obj);
    return this;
  }

  @Override
  public Session computeIfAbsent(String key, Function<String, Object> mappingFunction) {
    data()
      .computeIfAbsent(key, mappingFunction);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> @Nullable T remove(String key) {
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
          if (destroyed) {
            // pretty much should behave as a regeneration
            regenerateId();
            destroyed = false;
          }
          // delay the assignment to avoid potential races
          data = new ConcurrentHashMap<>();
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

  private static String generateId(VertxContextPRNG rng, int length) {
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

  protected void writeDataToBuffer(Buffer buffer) {
    if (isEmpty()) {
      buffer.appendInt(0);
    } else {
      final Map<String, Object> data = data();
      buffer.appendInt(data.size());
      for (Map.Entry<String, Object> entry : data.entrySet()) {
        String key = entry.getKey();
        byte[] keyBytes = key.getBytes(UTF8);
        buffer.appendInt(keyBytes.length).appendBytes(keyBytes);
        Object val = entry.getValue();
        if (val instanceof Long) {
          buffer.appendByte(TYPE_LONG).appendLong((long) val);
        } else if (val instanceof Integer) {
          buffer.appendByte(TYPE_INT).appendInt((int) val);
        } else if (val instanceof Short) {
          buffer.appendByte(TYPE_SHORT).appendShort((short) val);
        } else if (val instanceof Byte) {
          buffer.appendByte(TYPE_BYTE).appendByte((byte) val);
        } else if (val instanceof Double) {
          buffer.appendByte(TYPE_DOUBLE).appendDouble((double) val);
        } else if (val instanceof Float) {
          buffer.appendByte(TYPE_FLOAT).appendFloat((float) val);
        } else if (val instanceof Character) {
          buffer.appendByte(TYPE_CHAR).appendShort((short) ((Character) val).charValue());
        } else if (val instanceof Boolean) {
          buffer.appendByte(TYPE_BOOLEAN).appendByte((byte) ((boolean) val ? 1 : 0));
        } else if (val instanceof String) {
          byte[] bytes = ((String) val).getBytes(UTF8);
          buffer.appendByte(TYPE_STRING).appendInt(bytes.length).appendBytes(bytes);
        } else if (val instanceof Buffer) {
          Buffer buff = (Buffer) val;
          buffer.appendByte(TYPE_BUFFER).appendInt(buff.length()).appendBuffer(buff);
        } else if (val instanceof byte[]) {
          byte[] bytes = (byte[]) val;
          buffer.appendByte(TYPE_BYTES).appendInt(bytes.length).appendBytes(bytes);
        } else if (val instanceof ClusterSerializable) {
          buffer.appendByte(TYPE_CLUSTER_SERIALIZABLE);
          String className = val.getClass().getName();
          byte[] classNameBytes = className.getBytes(UTF8);
          buffer.appendInt(classNameBytes.length).appendBytes(classNameBytes);
          ((ClusterSerializable) val).writeToBuffer(buffer);
        } else {
          if (val != null) {
            throw new IllegalStateException("Invalid type for data in session: " + val.getClass());
          }
        }
      }
    }
  }

  protected int readDataFromBuffer(int pos, Buffer buffer) {
    try {
      int entries = buffer.getInt(pos);
      pos += 4;
      if (entries > 0) {
        final Map<String, Object> data = new ConcurrentHashMap<>(entries);

        for (int i = 0; i < entries; i++) {
          int keylen = buffer.getInt(pos);
          pos += 4;
          byte[] keyBytes = buffer.getBytes(pos, pos + keylen);
          pos += keylen;
          String key = new String(keyBytes, UTF8);
          byte type = buffer.getByte(pos++);
          Object val;
          switch (type) {
            case TYPE_LONG:
              val = buffer.getLong(pos);
              pos += 8;
              break;
            case TYPE_INT:
              val = buffer.getInt(pos);
              pos += 4;
              break;
            case TYPE_SHORT:
              val = buffer.getShort(pos);
              pos += 2;
              break;
            case TYPE_BYTE:
              val = buffer.getByte(pos);
              pos++;
              break;
            case TYPE_FLOAT:
              val = buffer.getFloat(pos);
              pos += 4;
              break;
            case TYPE_DOUBLE:
              val = buffer.getDouble(pos);
              pos += 8;
              break;
            case TYPE_CHAR:
              short s = buffer.getShort(pos);
              pos += 2;
              val = (char) s;
              break;
            case TYPE_BOOLEAN:
              byte b = buffer.getByte(pos);
              pos++;
              val = b == 1;
              break;
            case TYPE_STRING:
              int len = buffer.getInt(pos);
              pos += 4;
              byte[] bytes = buffer.getBytes(pos, pos + len);
              val = new String(bytes, UTF8);
              pos += len;
              break;
            case TYPE_BUFFER:
              len = buffer.getInt(pos);
              pos += 4;
              bytes = buffer.getBytes(pos, pos + len);
              val = Buffer.buffer(bytes);
              pos += len;
              break;
            case TYPE_BYTES:
              len = buffer.getInt(pos);
              pos += 4;
              val = buffer.getBytes(pos, pos + len);
              pos += len;
              break;
            case TYPE_CLUSTER_SERIALIZABLE:
              int classNameLen = buffer.getInt(pos);
              pos += 4;
              byte[] classNameBytes = buffer.getBytes(pos, pos + classNameLen);
              pos += classNameLen;
              String className = new String(classNameBytes, UTF8);
              Class<?> clazz = Utils.getClassLoader().loadClass(className);
              if (!ClusterSerializable.class.isAssignableFrom(clazz)) {
                throw new ClassCastException(new String(classNameBytes, StandardCharsets.UTF_8) + " is not assignable from ClusterSerializable");
              }
              ClusterSerializable obj = (ClusterSerializable) clazz.getDeclaredConstructor().newInstance();
              pos = obj.readFromBuffer(pos, buffer);
              val = obj;
              break;
            default:
              throw new IllegalStateException("Invalid serialized type: " + type);
          }
          data.put(key, val);
        }
        setData(data);
      }
      return pos;
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
      throw new VertxException(e);
    }
  }

  protected void readDataFromBuffer(Buffer buffer) {
    readDataFromBuffer(0, buffer);
  }
}

