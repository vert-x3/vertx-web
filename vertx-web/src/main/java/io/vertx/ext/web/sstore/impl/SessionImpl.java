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

package io.vertx.ext.web.sstore.impl;

import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.impl.Utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SessionImpl implements Session, ClusterSerializable, Shareable {

  private static final Charset UTF8 = Charset.forName("UTF-8");
  private static final char[] HEX = "0123456789abcdef".toCharArray();

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
  private static final byte TYPE_SERIALIZABLE = 12;
  private static final byte TYPE_CLUSTER_SERIALIZABLE = 13;

  private PRNG prng;

  private String id;
  private long timeout;
  private volatile Map<String, Object> data;
  private long lastAccessed;
  private int version;
  // state management
  private boolean destroyed;
  private boolean renewed;
  private String oldId;
  private int crc;
  // cache
  private Buffer buffer;

  /**
   * Important note: This constructor (even though not referenced anywhere) is required for serialization purposes. Do
   * not remove.
   */
  public SessionImpl() {
  }

  public SessionImpl(PRNG random) {
    this.prng = random;
  }

  public SessionImpl(PRNG random, long timeout, int length) {
    this.prng = random;
    this.id = generateId(prng, length);
    this.timeout = timeout;
    this.lastAccessed = System.currentTimeMillis();
  }

  void setPRNG(PRNG prng) {
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
    if (data == null) {
      return null;
    }
    Object obj = getData().get(key);
    return (T) obj;
  }

  @Override
  public Session put(String key, Object obj) {
    final Map<String, Object> data = getData();
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
    if (data == null) {
      return null;
    }
    Object obj = getData().remove(key);
    return (T) obj;
  }

  @Override
  public Map<String, Object> data() {
    return getData();
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
    buffer = writeDataToBuffer();
    int crc = crc16(buffer, 0, buffer.length());
    if (this.crc != crc) {
      ++version;
    }
    // save the current computed crc
    this.crc = crc;
  }

  @Override
  public void writeToBuffer(Buffer buff) {
    byte[] bytes = id.getBytes(UTF8);
    buff.appendInt(bytes.length).appendBytes(bytes);
    buff.appendLong(timeout);
    buff.appendLong(lastAccessed);
    buff.appendInt(version);
    // use cache
    Buffer dataBuf = buffer != null ? buffer : writeDataToBuffer();
    buff.appendBuffer(dataBuf);
  }

  @Override
  public int readFromBuffer(int pos, Buffer buffer) {
    int len = buffer.getInt(pos);
    pos += 4;
    byte[] bytes = buffer.getBytes(pos, pos + len);
    pos += len;
    id = new String(bytes, UTF8);
    timeout = buffer.getLong(pos);
    pos += 8;
    lastAccessed = buffer.getLong(pos);
    pos += 8;
    version = buffer.getInt(pos);
    pos += 4;
    int start = pos;
    pos = readDataFromBuffer(pos, buffer);
    int end = pos;
    // calculate the checksum
    crc = crc16(buffer, start, end);
    return pos;
  }

  private Map<String, Object> getData() {
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

  private Buffer writeDataToBuffer() {
    try {
      Buffer buffer = Buffer.buffer();
      if (data == null || data.size() == 0) {
        buffer.appendInt(0);
      } else {
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
          } else if (val instanceof Serializable) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(baos));
            oos.writeObject(val);
            oos.flush();
            byte[] bytes = baos.toByteArray();
            buffer.appendByte(TYPE_SERIALIZABLE).appendInt(bytes.length).appendBytes(bytes);
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
      return buffer;
    } catch (IOException e) {
      throw new VertxException(e);
    }
  }

  private int readDataFromBuffer(int pos, Buffer buffer) {
    try {
      int entries = buffer.getInt(pos);
      pos += 4;
      if (entries != 0) {
        data = new ConcurrentHashMap<>(entries);
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
            case TYPE_SERIALIZABLE:
              len = buffer.getInt(pos);
              pos += 4;
              bytes = buffer.getBytes(pos, pos + len);
              ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
              ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(bais));
              val = ois.readObject();
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
                throw new ClassCastException(className + " is not ClusterSerializable");
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
      }
      return pos;
    } catch (Exception e) {
      throw new VertxException(e);
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

  private static final int[] CRC16_TABLE = {
    0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
    0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
    0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
    0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
    0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
    0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
    0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
    0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
    0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
    0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
    0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
    0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
    0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
    0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
    0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
    0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
    0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
    0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
    0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
    0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
    0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
    0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
    0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
    0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
    0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
    0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
    0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
    0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
    0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
    0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
    0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
    0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
  };

  private static int crc16(Buffer data, int start, int end) {
    int crc = 0x0000;
    for (int i = start; i < end; i++) {
      crc = (crc >>> 8) ^ CRC16_TABLE[(crc ^ data.getByte(i)) & 0xff];
    }

    return crc;
  }
}

