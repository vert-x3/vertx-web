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

package io.vertx.ext.apex.middleware;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.middleware.impl.StaticImpl;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Static extends Handler<RoutingContext> {

  static final boolean DEFAULT_FILES_READ_ONLY = true;
  static final long DEFAULT_MAX_AGE_SECONDS = 86400; // One day
  static final boolean DEFAULT_CACHING_ENABLED = true;
  static final boolean DEFAULT_DIRECTORY_LISTING = false;
  static final boolean DEFAULT_INCLUDE_HIDDEN = true;
  static final long DEFAULT_CACHE_ENTRY_TIMEOUT = 30000; // 30 seconds
  static final String DEFAULT_INDEX_PAGE = "/index.html";

  static Static staticSite(String root) {
    return new StaticImpl(root);
  }

  @Override
  void handle(RoutingContext event);

  Static setFilesReadOnly(boolean readOnly);

  Static setMaxAgeSeconds(long maxAgeSeconds);

  Static setCachingEnabled(boolean enabled);

  Static setDirectoryListing(boolean directoryListing);

  Static setIncludeHidden(boolean includeHidden);

  Static setCacheEntryTimeout(long timeout);

  Static setIndexPage(String indexPage);

}
