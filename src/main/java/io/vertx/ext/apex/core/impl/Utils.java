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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public class Utils {

  public static String normalisePath(String path) {
    if (path == null) {
      return null;
    }
    try {
      StringBuilder result = new StringBuilder();
      byte[] escapedSeqBytes = new byte[path.length() / 3];

      for (int i = 0; i < path.length(); i++) {
        char c = path.charAt(i);

        if (c == '+') {
          result.append(' ');
        } else if (c == '/') {
          processSlash(result);
        } else if (c != '%') {
          result.append(c);
        } else {
          int used = 0;
          do {
            if (i >= path.length() - 2) {
              throw new IllegalArgumentException("Invalid position for escape character: " + i);
            }
            int unescaped = Integer.parseInt(path.substring(i + 1, i + 3), 16);
            if (unescaped < 0) {
              throw new IllegalArgumentException("Invalid escape sequence: " + path.substring(i, i + 3));
            }
            escapedSeqBytes[used++] = (byte) unescaped;
            i += 3;
          } while (i < path.length() && path.charAt(i) == '%');
          i--;

          String escapedSeq = new String(escapedSeqBytes, 0, used, "UTF-8");

          for (int j = 0; j < escapedSeq.length(); j++) {
            c = escapedSeq.charAt(j);
            if (c == '/') {
              processSlash(result);
            } else {
              result.append(c);
            }
          }
        }
      }

      if (path.charAt(0) != '/') {
        return null;
      }

      // Check for paths ending in ..
      if (path.length() > 1 && path.charAt(path.length() - 1) == '.' && path.charAt(path.length() - 2) == '.') {
        result.delete(result.length() - 2, result.length());
      }

      return result.toString();

    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Check if adding a slash causes an invalid sequence (// or ../). If it does, in the former, we just don't add the
   * slash, while in the latter, we delete the '..'
   *
   * If it does not introduce an invalid sequence, slash is added to the given path
   * 
   * @param path
   *          The partial path which is being checked
   */
  private static void processSlash(StringBuilder path) {
    if (path.length() == 0) {
      path.append('/');
    } else if (path.length() == 1) {
      if (path.charAt(0) != '/')
        path.append('/');
    } else {
      if (path.charAt(path.length() - 1) == '.' && path.charAt(path.length() - 2) == '.') {
        path.delete(path.length() - 2, path.length());
      } else if (path.charAt(path.length() - 1) != '/') {
        path.append('/');
      }
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

  public static String readResourceToString(String resource) {
    Buffer buff = readResourceToBuffer(resource);
    return buff == null ? null : buff.toString();
  }

  public static List<String> getSortedAcceptableMimeTypes(String acceptHeader) {
    // accept anything when accept is not present
    if (acceptHeader == null) {
      return Collections.emptyList();
    }

    // parse
    String[] items = acceptHeader.split(" *, *");
    // sort on quality
    Arrays.sort(items, ACCEPT_X_COMPARATOR);

    List<String> list = new ArrayList<>(items.length);

    for (String item : items) {
      // find any ; e.g.: "application/json;q=0.8"
      int space = item.indexOf(';');

      if (space != -1) {
        list.add(item.substring(0, space));
      } else {
        list.add(item);
      }
    }

    return list;
  }

  public static DateFormat createISODateTimeFormatter() {
    DateFormat dtf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
    dtf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return dtf;
  }

  private static final Comparator<String> ACCEPT_X_COMPARATOR = new Comparator<String>() {
    float getQuality(String s) {
      if (s == null) {
        return 0;
      }

      String[] params = s.split(" *; *");
      for (int i = 1; i < params.length; i++) {
        String[] q = params[1].split(" *= *");
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
}
