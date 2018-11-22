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

package io.vertx.ext.web.templ;

import io.vertx.ext.web.impl.ConcurrentLRUCache;
import io.vertx.ext.web.templ.TemplateEngine;

import java.util.Objects;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class CachingTemplateEngine<T> implements TemplateEngine {

  public static final String DISABLE_TEMPL_CACHING_PROP_NAME = "io.vertx.ext.web.TemplateEngine.disableCache";
  // should not be static, so at at creation time the value is evaluated
  private final boolean enableCache = !Boolean.getBoolean(DISABLE_TEMPL_CACHING_PROP_NAME);

  protected final ConcurrentLRUCache<String, T> cache;
  protected String extension;

  protected CachingTemplateEngine(String ext, int maxCacheSize) {
    Objects.requireNonNull(ext);
    if (maxCacheSize < 1) {
      throw new IllegalArgumentException("maxCacheSize must be >= 1");
    }
    doSetExtension(ext);
    this.cache = new ConcurrentLRUCache<>(maxCacheSize);
  }

  @Override
  public boolean isCachingEnabled() {
      return enableCache;
  }

  protected String adjustLocation(String location) {
    if (!location.endsWith(extension)) {
      location += extension;
    }
    return location;
  }

  protected void doSetExtension(String ext) {
    this.extension = ext.charAt(0) == '.' ? ext : "." + ext;
  }

}
