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

import io.vertx.ext.apex.core.impl.LRUCache;
import org.junit.Test;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class LRUCacheTest extends LRUCacheTestBase {

  @Override
  protected void createCache() {
    cache = new LRUCache<>(maxSize);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testCacheInvalidSize1() {
    new LRUCache<>(0);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testCacheInvalidSize2() {
    new LRUCache<>(-1);
  }


}
