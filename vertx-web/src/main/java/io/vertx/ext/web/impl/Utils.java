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

import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

  /**
   * Please see {@link #normalizePath(String, boolean)}
   * @deprecated
   * @param path raw path
   * @return normalized path
   */
  @Deprecated
  public static String normalisePath(String path) {
    return normalizePath(path, true);
  }

  private static int indexOfSlash(String str, int start) {
    for (int i = start; i < str.length(); i++) {
      if (str.charAt(i) == '/') {
        return i;
      }
    }

    return -1;
  }

  private static boolean matches(String path, int start, String what) {
    return matches(path, start, what, false);
  }

  private static boolean matches(String path, int start, String what, boolean exact) {
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

  /**
   * Normalizes a path (remove_dot_segments) as per <a href="http://tools.ietf.org/html/rfc3986#section-5.2.4>rfc3986#section-5.2.4</a>.
   *
   * There are 2 extra transformations that are not part of the spec but kept for backwards compatibility:
   *
   * double slash // will be converted to single slash and the path will always start with slash.
   *
   * @param ibuf raw path
   * @param unescape unescape
   * @return normalized path
   */
  public static String normalizePath(String ibuf, boolean unescape) {
    // add trailing slash if not set
    if (ibuf == null || ibuf.length() == 0) {
      return "/";
    }

    // Not standard!!!
    if (ibuf.charAt(0) != '/') {
      ibuf = "/" + ibuf;
    }

    // remove dots as described in
    // http://tools.ietf.org/html/rfc3986#section-5.2.4
    StringBuilder obuf = new StringBuilder(ibuf.length());
    int i = 0;
    while (i < ibuf.length()) {
      if (matches(ibuf, i, "./")) {
        i += 2;
      } else if (matches(ibuf, i, "../")) {
        i += 3;
      } else if (matches(ibuf, i, "/./")) {
        // preserve last slash
        i += 2;
      } else if (matches(ibuf, i,"/.", true)) {
        ibuf = "/";
        i = 0;
      } else if (matches(ibuf, i, "/../")) {
        // preserve last slash
        i += 3;
        int pos = obuf.lastIndexOf("/");
        if (pos != -1) {
          obuf.delete(pos, obuf.length());
        }
      } else if (matches(ibuf, i, "/..", true)) {
        ibuf = "/";
        i = 0;
        int pos = obuf.lastIndexOf("/");
        if (pos != -1) {
          obuf.delete(pos - 1, obuf.length());
        }
      } else if (matches(ibuf, i, ".", true) || matches(ibuf, i, "..", true)) {
        break;
      } else {
        if (ibuf.charAt(i) == '/') {
          i++;
          // Not standard!!!
          // but common // -> /
          if (obuf.length() == 0 || obuf.charAt(obuf.length() - 1) != '/') {
            obuf.append('/');
          }
        }
        int pos = indexOfSlash(ibuf, i);
        if (pos != -1) {
          if (unescape) {
            try {
              for (int j = i ; j < pos; j++) {
                if (ibuf.charAt(j) == '%') {
                  j = processEscapeSequence(ibuf, obuf, j);
                } else {
                  obuf.append(ibuf.charAt(j));
                }
              }
            } catch (UnsupportedEncodingException e) {
              throw new RuntimeException(e);
            }
          } else {
            obuf.append(ibuf, i, pos);
          }
          i = pos;
        } else {
          if (unescape) {
            try {
              for (int j = i; j < ibuf.length(); j++) {
                if (ibuf.charAt(j) == '%') {
                  j = processEscapeSequence(ibuf, obuf, j);
                } else {
                  obuf.append(ibuf.charAt(j));
                }
              }
            } catch (UnsupportedEncodingException e) {
              throw new RuntimeException(e);
            }
          } else {
            obuf.append(ibuf, i, ibuf.length());
          }
          break;
        }
      }
    }

    return obuf.toString();
  }

  /**
   * Please see {@link #normalizePath(String, boolean)}
   * @deprecated
   * @param path raw path
   * @param urldecode unescape
   * @return normalized path
   */
  @Deprecated
  public static String normalisePath(String path, boolean urldecode) {
    return normalizePath(path, urldecode);
  }

  /**
   * Processes a escape sequence in path
   *
   * @param path   The original path
   * @param result The result of unescaping the escape sequence (and removing dangerous constructs)
   * @param i      The index of path where the escape sequence begins
   * @return The index of path where the escape sequence ends
   * @throws UnsupportedEncodingException If the escape sequence does not represent a valid UTF-8 string
   */
  private static int processEscapeSequence(String path, StringBuilder result, int i) throws UnsupportedEncodingException {
    Buffer buf = Buffer.buffer(2);
    do {
      if (i >= path.length() - 2) {
        throw new IllegalArgumentException("Invalid position for escape character: " + i);
      }
      int unescaped = Integer.parseInt(path.substring(i + 1, i + 3), 16);
      if (unescaped < 0) {
        throw new IllegalArgumentException("Invalid escape sequence: " + path.substring(i, i + 3));
      }
      buf.appendByte((byte) unescaped);
      i += 3;
    } while (i < path.length() && path.charAt(i) == '%');

    String escapedSeq = new String(buf.getBytes(), StandardCharsets.UTF_8);

    for (int j = 0; j < escapedSeq.length(); j++) {
      char c = escapedSeq.charAt(j);
      if (c == '/') {
        if (j == 0 || result.charAt(result.length() - 1) != '/')
          result.append(c);
      } else if (c == '.') {
        if (j == 0 || result.charAt(result.length() - 1) != '.')
          result.append(c);
        else
          result.deleteCharAt(result.length() - 1);
      } else {
        result.append(c);
      }
    }
    return i - 1;
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
  Reads from file or classpath
   */
  public static String readFileToString(Vertx vertx, String resource) {
    try {
      Buffer buff = vertx.fileSystem().readFileBlocking(resource);
      return buff.toString();
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
      if (f1 < f2) {
        return 1;
      }
      if (f1 > f2) {
        return -1;
      }
      return 0;
    }
  };

  public static long secondsFactor(long millis) {
    return millis - (millis % 1000);
  }
}
