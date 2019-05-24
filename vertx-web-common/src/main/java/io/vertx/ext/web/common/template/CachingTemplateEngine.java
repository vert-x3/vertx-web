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

import io.vertx.core.Vertx;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.common.WebEnvironment;
import io.vertx.ext.web.common.template.impl.TemplateHolder;

import java.util.Objects;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class CachingTemplateEngine<T> implements TemplateEngine {

  // should not be static, so at at creation time the value is evaluated
  private final boolean enableCache = !WebEnvironment.development();

  private final LocalMap<String, TemplateHolder<T>> cache;

  protected String extension;

  @Deprecated
  protected CachingTemplateEngine(String ext, int maxCacheSize) {
    this(maxCacheSize);
    Objects.requireNonNull(ext);
    doSetExtension(ext);
  }

  protected CachingTemplateEngine(Vertx vertx, String ext) {
    if (isCachingEnabled()) {
      cache = vertx.sharedData().getLocalMap("__vertx.web.template.cache");
    } else {
      cache = null;
    }

    Objects.requireNonNull(ext);
    doSetExtension(ext);
  }

  protected CachingTemplateEngine(int maxCacheSize) {
    if (maxCacheSize < 1) {
      throw new IllegalArgumentException("maxCacheSize must be >= 1");
    }
    this.cache = null;
  }

  public TemplateHolder<T> getTemplate(String filename) {
    if (isCachingEnabled()) {
      return cache.get(filename);
    }

    return null;
  }

  public TemplateHolder<T> putTemplate(String filename, TemplateHolder<T> templateHolder) {
    if (isCachingEnabled()) {
      return cache.put(filename, templateHolder);
    }

    return null;
  }

  @Override
  public boolean isCachingEnabled() {
      return enableCache;
  }

  protected String adjustLocation(String location) {
    if (extension != null) {
      if (!location.endsWith(extension)) {
        location += extension;
      }
    }
    return location;
  }

  protected void doSetExtension(String ext) {
    this.extension = ext.charAt(0) == '.' ? ext : "." + ext;
  }
}
