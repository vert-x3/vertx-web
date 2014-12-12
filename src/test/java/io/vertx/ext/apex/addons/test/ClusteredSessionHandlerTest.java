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

import io.vertx.core.VertxOptions;
import io.vertx.ext.apex.addons.impl.ClusteredSessionStore;
import io.vertx.test.fakecluster.FakeClusterManager;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ClusteredSessionHandlerTest extends SessionHandlerTestBase {

  int numNodes = 2;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    VertxOptions options = new VertxOptions();
    options.setClustered(true);
    options.setClusterManager(new FakeClusterManager());
    startNodes(numNodes);
    store = ClusteredSessionStore.clusteredSessionStore(vertices[0]);
  }



}
