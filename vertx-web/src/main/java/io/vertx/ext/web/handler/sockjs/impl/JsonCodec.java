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

import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * SockJS requires a special JSON codec - it requires that many other characters,
 * over and above what is required by the JSON spec are escaped.
 * To satisfy this we escape any character that escapable with short escapes and
 * any other non ASCII character we unicode escape it
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class JsonCodec {

  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

  // Escape table for the first 128 characters (replicates Jackson CharTypes.get7BitOutputEscapes()).
  // 0 = no escaping, -1 = full unicode escape, positive = short escape char code.
  private static final int[] ESCAPE_CODES;

  static {
    ESCAPE_CODES = new int[128];
    for (int i = 0; i < 32; i++) {
      ESCAPE_CODES[i] = -1;
    }
    ESCAPE_CODES['"'] = '"';
    ESCAPE_CODES['\\'] = '\\';
    ESCAPE_CODES[0x08] = 'b';
    ESCAPE_CODES[0x09] = 't';
    ESCAPE_CODES[0x0C] = 'f';
    ESCAPE_CODES[0x0A] = 'n';
    ESCAPE_CODES[0x0D] = 'r';
  }

  private static void writeUnicodeEscape(StringBuilder sb, char c) {
    sb.append('\\');
    sb.append('u');
    sb.append(HEX_CHARS[(c >> 12) & 0xF]);
    sb.append(HEX_CHARS[(c >> 8) & 0xF]);
    sb.append(HEX_CHARS[(c >> 4) & 0xF]);
    sb.append(HEX_CHARS[c & 0xF]);
  }

  private static void writeShortEscape(StringBuilder sb, char c) {
    sb.append('\\');
    sb.append(c);
  }

  public static String encode(String[] messages) throws EncodeException {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      boolean first = true;
      for (String message : messages) {
        if (first) {
          first = false;
        } else {
          sb.append(',');
        }
        sb.append('"');
        for (int i = 0; i < message.length(); i++) {
          char c = message.charAt(i);
          if (c >= 0x80) {
            writeUnicodeEscape(sb, c);
          } else {
            int code = (c < ESCAPE_CODES.length ? ESCAPE_CODES[c] : 0);
            if (code == 0) {
              sb.append(c);
            } else if (code == -1) {
              writeUnicodeEscape(sb, c);
            } else {
              writeShortEscape(sb, (char) code);
            }
          }
        }
        sb.append('"');
      }
      sb.append(']');
      return sb.toString();
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON", e);
    }
  }

  public static List<String> decodeValues(String messages) {
    if (messages == null || messages.isEmpty()) {
      return Collections.emptyList();
    }
    try {
      Object decoded = Json.decodeValue(messages);
      if (decoded instanceof JsonArray) {
        JsonArray array = (JsonArray) decoded;
        List<String> result = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
          result.add(array.getString(i));
        }
        return result;
      } else if (decoded instanceof String) {
        return Collections.singletonList((String) decoded);
      }
      return Collections.emptyList();
    } catch (Exception ignore) {
      return null;
    }
  }
}
