/*
 * Copyright 2016 Original authors.
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
package io.vertx.ext.web.templ.impl;

import com.mitchellbosecke.pebble.error.LoaderException;
import com.mitchellbosecke.pebble.loader.Loader;
import io.vertx.core.Vertx;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * A PebbleLoader based on Vertx
 *
 * @author Paulo Lopes
 */
class PebbleVertxLoader implements Loader<String> {

  private final Vertx vertx;

  private String charset = Charset.defaultCharset().toString();

  public PebbleVertxLoader(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Reader getReader(String s) throws LoaderException {
    try {
      final char[] buffer = vertx.fileSystem().readFileBlocking(s).toString(charset).toCharArray();
      final int[] pos = {0};

      return new Reader() {
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
          if (pos[0] == buffer.length) {
            return -1;
          }
          final int end = Math.min(buffer.length, pos[0] + len);
          System.arraycopy(buffer, pos[0], cbuf, off, end);
          final int read = end - pos[0];
          pos[0] = end;
          return read;
        }

        @Override
        public void close() throws IOException {

        }
      };
    } catch (RuntimeException e) {
      throw new LoaderException(e, e.getMessage());
    }
  }

  @Override
  public void setCharset(String s) {
    this.charset = s;
  }

  @Override
  public void setPrefix(String s) {
  }

  @Override
  public void setSuffix(String s) {
  }

  @Override
  public String resolveRelativePath(String s, String anchorPath) {
    File resolved = new File(new File(anchorPath).getParentFile(), s);

    return resolved.getPath();
  }

  @Override
  public String createCacheKey(String s) {
    return s;
  }
}
