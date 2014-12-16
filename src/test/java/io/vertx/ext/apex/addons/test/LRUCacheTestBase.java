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

package io.vertx.ext.apex.addons.test;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class LRUCacheTestBase {

  protected Map<String, String> cache;
  protected int maxSize = 10;

  @Before
  public void setUp() {
    createCache();
  }

  protected abstract void createCache();

  @Test
  public void testPut() {
    int numEntries = 20;
    for (int i = 0; i < numEntries; i++) {
      cache.put("key" + i, "value" + i);
    }
    assertEquals(maxSize, cache.size());
    for (int i = 10; i < numEntries; i++) {
      assertTrue(cache.containsKey("key" + i));
    }
  }

  @Test
  public void testRemove() {
    for (int i = 0; i < maxSize; i++) {
      cache.put("key" + i, "value" + i);
    }
    // Now remove them all
    for (int i = 0; i < maxSize; i++) {
      cache.remove("key" + i);
    }
    assertTrue(cache.isEmpty());
  }

}
