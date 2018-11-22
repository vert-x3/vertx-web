/*
 * Copyright 2016 Red Hat, Inc.
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
package io.vertx.ext.web.templ.freemarker.impl;

import freemarker.cache.TemplateLoader;
import io.vertx.core.Vertx;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
class FreeMarkerTemplateLoader implements TemplateLoader {

  private final Vertx vertx;

  FreeMarkerTemplateLoader(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Object findTemplateSource(String name) throws IOException {
    try {
      // check if exists on file system
      if (vertx.fileSystem().existsBlocking(name)) {
        String templ = vertx.fileSystem()
          .readFileBlocking(name)
          .toString(Charset.defaultCharset());
        return new StringTemplateSource(name, templ, System.currentTimeMillis());
      } else {
        return null;
      }

    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  public long getLastModified(Object templateSource) {
    return ((StringTemplateSource) templateSource).lastModified;
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    return new StringReader(((StringTemplateSource) templateSource).source);
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {

  }

  private static class StringTemplateSource {
    private final String name;
    private final String source;
    private final long lastModified;

    StringTemplateSource(String name, String source, long lastModified) {
      if (name == null) {
        throw new IllegalArgumentException("name == null");
      }
      if (source == null) {
        throw new IllegalArgumentException("source == null");
      }
      if (lastModified < -1L) {
        throw new IllegalArgumentException("lastModified < -1L");
      }
      this.name = name;
      this.source = source;
      this.lastModified = lastModified;
    }

    public boolean equals(Object obj) {
      return obj instanceof StringTemplateSource && name.equals(((StringTemplateSource) obj).name);
    }

    public int hashCode() {
      return name.hashCode();
    }
  }
}
