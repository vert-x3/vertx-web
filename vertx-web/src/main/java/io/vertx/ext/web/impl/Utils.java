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
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.impl.HttpUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class Utils extends io.vertx.core.impl.Utils {

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
    return HttpUtils.removeDots(ibuf);
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
