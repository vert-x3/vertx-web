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

package io.vertx.ext.web.common.template;

import io.vertx.ext.web.common.template.impl.ConcurrentLRUCache;

import java.util.Objects;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class CachingTemplateEngine<T> implements TemplateEngine {

  public static final String DISABLE_TEMPL_CACHING_PROP_NAME = "io.vertx.ext.web.TemplateEngine.disableCache";
  // should not be static, so at at creation time the value is evaluated
  private final boolean enableCache = !Boolean.getBoolean(DISABLE_TEMPL_CACHING_PROP_NAME);

  protected final ConcurrentLRUCache<String, T> cache;

  @Deprecated
  protected String extension;

  @Deprecated
  protected CachingTemplateEngine(String ext, int maxCacheSize) {
    this(maxCacheSize);
    Objects.requireNonNull(ext);
    doSetExtension(ext);
  }

  protected CachingTemplateEngine(int maxCacheSize) {
    if (maxCacheSize < 1) {
      throw new IllegalArgumentException("maxCacheSize must be >= 1");
    }
    this.cache = new ConcurrentLRUCache<>(maxCacheSize);
  }

  @Override
  public boolean isCachingEnabled() {
      return enableCache;
  }

  @Deprecated
  protected String adjustLocation(String location) {
    if (extension != null) {
      if (!location.endsWith(extension)) {
        location += extension;
      }
    }
    return location;
  }

  @Deprecated
  protected void doSetExtension(String ext) {
    this.extension = ext.charAt(0) == '.' ? ext : "." + ext;
  }

}
