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
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Utils {

  public static String normalisePath(String path) {
    if (path == null || path.charAt(0) != '/') {
      return null;
    }
    try {
      path = URLDecoder.decode(path, "UTF-8");
      Path p = Paths.get(path).normalize();
      return p.toString();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static Buffer readResourceToBuffer(String resource) {
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    ClassLoader cl = tccl == null ? Utils.class.getClassLoader() : tccl;
    try {
      Buffer buffer = Buffer.buffer();
      try (InputStream in = cl.getResourceAsStream(resource)) {
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
}
