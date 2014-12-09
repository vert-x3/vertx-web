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

package io.vertx.ext.apex.addons.impl.templengines;

import io.vertx.ext.apex.addons.AbstractTemplateEngine;
import io.vertx.ext.apex.core.impl.ConcurrentLRUCache;

import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class CachingTemplateEngine<T> extends AbstractTemplateEngine {

  protected final String prefix;
  protected final String extension ;
  protected final Map<String, T> cache;

  protected CachingTemplateEngine(String prefix, String ext, int maxCacheSize) {
    Objects.requireNonNull(ext);
    if (maxCacheSize < 1) {
      throw new IllegalArgumentException("maxCacheSize must be >= 1");
    }
    if (prefix != null && !prefix.endsWith("/")) {
      prefix += "/";
    }
    this.prefix = prefix;
    this.extension = ext.charAt(0) == '.' ? ext : "." + ext;
    this.cache = new ConcurrentLRUCache<>(maxCacheSize);
  }

  protected String adjustLocation(String location) {
    if (!location.endsWith(extension)) {
      location += extension;
    }
    if (prefix != null) {
      location = prefix + location;
    }
    return location;
  }

}
