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

package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.addons.impl.StaticServerImpl;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface StaticServer extends Handler<RoutingContext> {

  static final String DEFAULT_WEB_ROOT = "webroot";
  static final boolean DEFAULT_FILES_READ_ONLY = true;
  static final long DEFAULT_MAX_AGE_SECONDS = 86400; // One day
  static final boolean DEFAULT_CACHING_ENABLED = true;
  static final boolean DEFAULT_DIRECTORY_LISTING = false;
  static final boolean DEFAULT_INCLUDE_HIDDEN = true;
  static final long DEFAULT_CACHE_ENTRY_TIMEOUT = 30000; // 30 seconds
  static final String DEFAULT_INDEX_PAGE = "/index.html";
  static final int DEFAULT_MAX_CACHE_SIZE = 10000;

  static StaticServer staticServer(String root) {
    return new StaticServerImpl(root);
  }

  static StaticServer staticServer() {
    return new StaticServerImpl();
  }

  @Override
  void handle(RoutingContext event);

  StaticServer setWebRoot(String webRoot);

  StaticServer setFilesReadOnly(boolean readOnly);

  StaticServer setMaxAgeSeconds(long maxAgeSeconds);

  StaticServer setCachingEnabled(boolean enabled);

  StaticServer setDirectoryListing(boolean directoryListing);

  StaticServer setIncludeHidden(boolean includeHidden);

  StaticServer setCacheEntryTimeout(long timeout);

  StaticServer setIndexPage(String indexPage);

  StaticServer setMaxCacheSize(int maxCacheSize);

}
