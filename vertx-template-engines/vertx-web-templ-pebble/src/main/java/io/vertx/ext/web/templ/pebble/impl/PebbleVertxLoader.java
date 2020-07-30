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
package io.vertx.ext.web.templ.pebble.impl;

import com.mitchellbosecke.pebble.error.LoaderException;
import com.mitchellbosecke.pebble.loader.Loader;
import io.vertx.core.Vertx;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * A PebbleLoader based on Vertx
 *
 * @author Paulo Lopes
 */
public class PebbleVertxLoader implements Loader<String> {

  private final Vertx vertx;

  private Charset charset = Charset.defaultCharset();

  public PebbleVertxLoader(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Reader getReader(String s) throws LoaderException {
    try {
      return new StringReader(
        vertx.fileSystem()
          .readFileBlocking(s)
          .toString(charset));
    } catch (RuntimeException e) {
      throw new LoaderException(e, e.getMessage());
    }
  }

  @Override
  public void setCharset(String s) {
    this.charset = Charset.forName(s);
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
