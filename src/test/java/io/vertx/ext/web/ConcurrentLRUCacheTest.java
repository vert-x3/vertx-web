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

package io.vertx.ext.web;

import io.vertx.ext.web.impl.ConcurrentLRUCache;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ConcurrentLRUCacheTest extends LRUCacheTestBase {

  @Override
  protected Map<String, String> createCache() {
    return new ConcurrentLRUCache<>(maxSize);
  }

  @Test
  public void testPut() {
    super.testPut();
    assertEquals(maxSize, ((ConcurrentLRUCache)cache).queueSize());
  }

  @Test
  public void testRemove() {
    super.testRemove();
    assertEquals(0, ((ConcurrentLRUCache)cache).queueSize());
  }

  @Test(expected=IllegalArgumentException.class)
  public void testCacheInvalidSize1() {
    new ConcurrentLRUCache<>(0);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testCacheInvalidSize2() {
    new ConcurrentLRUCache<>(-1);
  }

  @Test
  public void testIncreaseMaxSize() {
    for (int i = 0; i < maxSize; i++) {
      cache.put("key" + i, "value" + i);
    }
    assertEquals(maxSize, cache.size());
    ConcurrentLRUCache<String, String> ccache = (ConcurrentLRUCache<String, String>)cache;
    ccache.setMaxSize(maxSize + 10);
    for (int i = maxSize; i < maxSize + 10; i++) {
      cache.put("key" + i, "value" + i);
    }
    assertEquals(maxSize + 10, cache.size());
  }

}
