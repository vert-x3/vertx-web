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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

  private final int maxSize;

  public LRUCache(int initialCapacity, float loadFactor, int maxSize) {
    super(initialCapacity, loadFactor);
    this.maxSize = maxSize;
    checkSize();
  }

  public LRUCache(int initialCapacity, int maxSize) {
    super(initialCapacity);
    this.maxSize = maxSize;
    checkSize();
  }

  public LRUCache(int maxSize) {
    this.maxSize = maxSize;
    checkSize();
  }

  public LRUCache(Map<? extends K, ? extends V> m, int maxSize) {
    super(m);
    this.maxSize = maxSize;
    checkSize();
  }

  public LRUCache(int initialCapacity, float loadFactor, boolean accessOrder, int maxSize) {
    super(initialCapacity, loadFactor, accessOrder);
    this.maxSize = maxSize;
    checkSize();
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > maxSize;
  }

  private void checkSize() {
    if (maxSize < 1) {
      throw new IllegalArgumentException("maxSize must be >= 1");
    }
  }
}
