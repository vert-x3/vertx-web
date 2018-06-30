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

package io.vertx.ext.web.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.CharsetUtil;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class Utils extends io.vertx.core.impl.Utils {

  private static final Pattern COMMA_SPLITTER = Pattern.compile(" *, *");
  private static final Pattern SEMICOLON_SPLITTER = Pattern.compile(" *; *");
  private static final Pattern EQUAL_SPLITTER = Pattern.compile(" *= *");

  private static int indexOfSlash(CharSequence str, int start) {
    for (int i = start; i < str.length(); i++) {
      if (str.charAt(i) == '/') {
        return i;
      }
    }

    return -1;
  }

  private static boolean matches(CharSequence path, int start, String what) {
    return matches(path, start, what, false);
  }

  private static boolean matches(CharSequence path, int start, String what, boolean exact) {
    if (exact) {
      if (path.length() - start != what.length()) {
        return false;
      }
    }

    if (path.length() - start >= what.length()) {
      for (int i = 0; i < what.length(); i++) {
        if (path.charAt(start + i) != what.charAt(i)) {
          return false;
        }
      }
      return true;
    }

    return false;
  }

  private static void decodeUnreserved(StringBuilder path, int start) {
    if (start + 3 <= path.length()) {
      // these are latin chars so there is no danger of falling into some special unicode char that requires more
      // than 1 byte
      final String escapeSequence = path.substring(start + 1, start + 3);
      int unescaped;
      try {
        unescaped = Integer.parseInt(escapeSequence, 16);
        if (unescaped < 0) {
          throw new IllegalArgumentException("Invalid escape sequence: %" + escapeSequence);
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid escape sequence: %" + escapeSequence);
      }
      // validate if the octet is within the allowed ranges
      if (
          // ALPHA
          (unescaped >= 0x41 && unescaped <= 0x5A) ||
          (unescaped >= 0x61 && unescaped <= 0x7A) ||
          // DIGIT
          (unescaped >= 0x30 && unescaped <= 0x39) ||
          // HYPHEN
          (unescaped == 0x2D) ||
          // PERIOD
          (unescaped == 0x2E) ||
          // UNDERSCORE
          (unescaped == 0x5F) ||
          // TILDE
          (unescaped == 0x7E)) {

        path.setCharAt(start, (char) unescaped);
        path.delete(start + 1, start + 3);
      }
    } else {
      throw new IllegalArgumentException("Invalid position for escape character: " + start);
    }
  }

  /**
   * Normalizes a path as per <a href="http://tools.ietf.org/html/rfc3986#section-5.2.4>rfc3986</a>.
   *
   * There are 2 extra transformations that are not part of the spec but kept for backwards compatibility:
   *
   * double slash // will be converted to single slash and the path will always start with slash.
   *
   * @param pathname raw path
   * @return normalized path
   */
  public static String normalizePath(String pathname) {
    // add trailing slash if not set
    if (pathname == null || pathname.length() == 0) {
      return "/";
    }

    StringBuilder ibuf = new StringBuilder(pathname.length() + 1);

    // Not standard!!!
    if (pathname.charAt(0) != '/') {
      ibuf.append('/');
    }

    ibuf.append(pathname);
    int i = 0;

    while (i < ibuf.length()) {
      // decode unreserved chars described in
      // http://tools.ietf.org/html/rfc3986#section-2.4
      if (ibuf.charAt(i) == '%') {
        decodeUnreserved(ibuf, i);
      }

      i++;
    }

    // remove dots as described in
    // http://tools.ietf.org/html/rfc3986#section-5.2.4
    return removeDots(ibuf);
  }

  /**
   * Removed dots as per <a href="http://tools.ietf.org/html/rfc3986#section-5.2.4>rfc3986</a>.
   *
   * There are 2 extra transformations that are not part of the spec but kept for backwards compatibility:
   *
   * double slash // will be converted to single slash and the path will always start with slash.
   *
   * @param path raw path
   * @return normalized path
   */
  public static String removeDots(CharSequence path) {

    if (path == null) {
      return null;
    }

    final StringBuilder obuf = new StringBuilder(path.length());

    int i = 0;
    while (i < path.length()) {
      // remove dots as described in
      // http://tools.ietf.org/html/rfc3986#section-5.2.4
      if (matches(path, i, "./")) {
        i += 2;
      } else if (matches(path, i, "../")) {
        i += 3;
      } else if (matches(path, i, "/./")) {
        // preserve last slash
        i += 2;
      } else if (matches(path, i,"/.", true)) {
        path = "/";
        i = 0;
      } else if (matches(path, i, "/../")) {
        // preserve last slash
        i += 3;
        int pos = obuf.lastIndexOf("/");
        if (pos != -1) {
          obuf.delete(pos, obuf.length());
        }
      } else if (matches(path, i, "/..", true)) {
        path = "/";
        i = 0;
        int pos = obuf.lastIndexOf("/");
        if (pos != -1) {
          obuf.delete(pos, obuf.length());
        }
      } else if (matches(path, i, ".", true) || matches(path, i, "..", true)) {
        break;
      } else {
        if (path.charAt(i) == '/') {
          i++;
          // Not standard!!!
          // but common // -> /
          if (obuf.length() == 0 || obuf.charAt(obuf.length() - 1) != '/') {
            obuf.append('/');
          }
        }
        int pos = indexOfSlash(path, i);
        if (pos != -1) {
          obuf.append(path, i, pos);
          i = pos;
        } else {
          obuf.append(path, i, path.length());
          break;
        }
      }
    }

    return obuf.toString();
  }

  /**
   * Decodes a bit of an URL encoded by a browser.
   *
   * The string is expected to be encoded as per RFC 3986, Section 2. This is the encoding used by JavaScript functions
   * encodeURI and encodeURIComponent, but not escape. For example in this encoding, é (in Unicode U+00E9 or in
   * UTF-8 0xC3 0xA9) is encoded as %C3%A9 or %c3%a9.
   *
   * @param s string to decode
   * @param plus weather or not to transform plus signs into spaces
   *
   * @return decoded string
   */
  public static String urlDecode(final String s, boolean plus) {
    if (s == null) {
      return null;
    }

    final int size = s.length();
    boolean modified = false;
    for (int i = 0; i < size; i++) {
      final char c = s.charAt(i);
      if (c == '%' || (plus && c == '+')) {
        modified = true;
        break;
      }
    }
    if (!modified) {
      return s;
    }
    final byte[] buf = new byte[size];
    int pos = 0;  // position in `buf'.
    for (int i = 0; i < size; i++) {
      char c = s.charAt(i);
      if (c == '%') {
        if (i == size - 1) {
          throw new IllegalArgumentException("unterminated escape"
            + " sequence at end of string: " + s);
        }
        c = s.charAt(++i);
        if (c == '%') {
          buf[pos++] = '%';  // "%%" -> "%"
          break;
        }
        if (i == size - 1) {
          throw new IllegalArgumentException("partial escape"
            + " sequence at end of string: " + s);
        }
        c = decodeHexNibble(c);
        final char c2 = decodeHexNibble(s.charAt(++i));
        if (c == Character.MAX_VALUE || c2 == Character.MAX_VALUE) {
          throw new IllegalArgumentException(
            "invalid escape sequence `%" + s.charAt(i - 1)
              + s.charAt(i) + "' at index " + (i - 2)
              + " of: " + s);
        }
        c = (char) (c * 16 + c2);
        // shouldn't check for plus since it would be a double decoding
        buf[pos++] = (byte) c;
      } else {
        buf[pos++] = (byte) (plus && c == '+' ? ' ' : c);
      }
    }
    return new String(buf, 0, pos, CharsetUtil.UTF_8);
  }

  /**
   * Helper to decode half of a hexadecimal number from a string.
   * @param c The ASCII character of the hexadecimal number to decode.
   * Must be in the range {@code [0-9a-fA-F]}.
   * @return The hexadecimal value represented in the ASCII character
   * given, or {@link Character#MAX_VALUE} if the character is invalid.
   */
  private static char decodeHexNibble(final char c) {
    if ('0' <= c && c <= '9') {
      return (char) (c - '0');
    } else if ('a' <= c && c <= 'f') {
      return (char) (c - 'a' + 10);
    } else if ('A' <= c && c <= 'F') {
      return (char) (c - 'A' + 10);
    } else {
      return Character.MAX_VALUE;
    }
  }

  public static ClassLoader getClassLoader() {
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    return tccl == null ? Utils.class.getClassLoader() : tccl;
  }

  public static Buffer readResourceToBuffer(String resource) {
    ClassLoader cl = getClassLoader();
    try {
      Buffer buffer = Buffer.buffer();
      try (InputStream in = cl.getResourceAsStream(resource)) {
        if (in == null) {
          return null;
        }
        int read;
        byte[] data = new byte[4096];
        while ((read = in.read(data, 0, data.length)) != -1) {
          if (read == data.length) {
            buffer.appendBytes(data);
          } else {
            byte[] slice = new byte[read];
            System.arraycopy(data, 0, slice, 0, slice.length);
            buffer.appendBytes(slice);
          }
        }
      }
      return buffer;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /*
  Reads from file or classpath using UTF-8
   */
  public static String readFileToString(Vertx vertx, String resource) {
    return readFileToString(vertx, resource, StandardCharsets.UTF_8);
  }

  /*
  Reads from file or classpath using the provided charset
   */
  public static String readFileToString(Vertx vertx, String resource, Charset charset) {
    try {
      Buffer buff = vertx.fileSystem().readFileBlocking(resource);
      return buff.toString(charset);
    } catch (Exception e) {
      throw new VertxException(e);
    }
  }

  public static DateFormat createRFC1123DateTimeFormatter() {
    DateFormat dtf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
    dtf.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dtf;
  }

  public static String pathOffset(String path, RoutingContext context) {
    int prefixLen = 0;
    String mountPoint = context.mountPoint();
    if (mountPoint != null) {
      prefixLen = mountPoint.length();
    }
    String routePath = context.currentRoute().getPath();
    if (routePath != null) {
      prefixLen += routePath.length();
      // special case we need to verify if a trailing slash  is present and exclude
      if (routePath.charAt(routePath.length() - 1) == '/') {
        prefixLen--;
      }
    }
    return prefixLen != 0 ? path.substring(prefixLen) : path;
  }

  private static final Comparator<String> ACCEPT_X_COMPARATOR = new Comparator<String>() {
    float getQuality(String s) {
      if (s == null) {
        return 0;
      }

      String[] params = SEMICOLON_SPLITTER.split(s);
      for (int i = 1; i < params.length; i++) {
        String[] q = EQUAL_SPLITTER.split(params[1]);
        if ("q".equals(q[0])) {
          return Float.parseFloat(q[1]);
        }
      }
      return 1;
    }

    @Override
    public int compare(String o1, String o2) {
      float f1 = getQuality(o1);
      float f2 = getQuality(o2);
      return Float.compare(f2, f1);
    }
  };

  public static long secondsFactor(long millis) {
    return millis - (millis % 1000);
  }

  public static JsonNode toJsonNode(String object) {
    try {
      return new ObjectMapper().readTree(object);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static JsonNode toJsonNode(JsonObject object) {
    try {
      return new ObjectMapper().readTree(object.encode());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static JsonObject toJsonObject(JsonNode node) {
    return new JsonObject(node.toString());
  }

  public static JsonArray toJsonArray(JsonNode node) {
    return new JsonArray(node.toString());
  }

  public static Object toVertxJson(JsonNode node) {
    if (node.isArray())
      return toJsonArray(node);
    else if (node.isObject())
      return toJsonObject(node);
    else
      return node.toString();
  }

  public static boolean isJsonContentType(String contentType) {
    return contentType.contains("application/json") || contentType.contains("+json");
  }

  public static boolean isXMLContentType(String contentType) {
    return contentType.contains("application/xml") || contentType.contains("text/xml") || contentType.contains("+xml");
  }
  
  public static void addToMapIfAbsent(MultiMap map, String key, String value) {
    if (!map.contains(key)) {
    	map.set(key, value);
    }
  }

}
