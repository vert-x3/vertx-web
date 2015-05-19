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

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.StaticHandlerImpl;

/**
 * A handler for serving static resources from the file system or classpath.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface StaticHandler extends Handler<RoutingContext> {

  /**
   * Default value of the web-root, where files are served from
   */
  static final String DEFAULT_WEB_ROOT = "webroot";

  /**
   * Default value of whether files are read -only and never will be updated
   */
  static final boolean DEFAULT_FILES_READ_ONLY = true;

  /**
   * Default max age for cache headers
   */
  static final long DEFAULT_MAX_AGE_SECONDS = 86400; // One day

  /**
   * Default of whether cache header handling is enabled
   */
  static final boolean DEFAULT_CACHING_ENABLED = true;

  /**
   * Default of whether directory listing is enabled
   */
  static final boolean DEFAULT_DIRECTORY_LISTING = false;

  /**
   * Default template file to use for directory listing
   */
  static final String DEFAULT_DIRECTORY_TEMPLATE = "vertx-web-directory.html";

  /**
   * Default of whether hidden files can be served
   */
  static final boolean DEFAULT_INCLUDE_HIDDEN = true;

  /**
   * Default cache entry timeout, when caching
   */
  static final long DEFAULT_CACHE_ENTRY_TIMEOUT = 30000; // 30 seconds

  /**
   * The default index page
   */
  static final String DEFAULT_INDEX_PAGE = "/index.html";

  /**
   * The default max cache size
   */
  static final int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default of whether async filesystem access should always be used
   */
  static final boolean DEFAULT_ALWAYS_ASYNC_FS = false;

  /**
   * Default of whether fs async/sync tuning should be used
   */
  static final boolean DEFAULT_ENABLE_FS_TUNING = true;

  /**
   * Default max avg serve time, in ns, over which serving will be considered slow
   */
  static final long DEFAULT_MAX_AVG_SERVE_TIME_NS = 1000000; // 1ms

  /**
   * Create a handler using defaults
   *
   * @return the handler
   */
  static StaticHandler create() {
    return new StaticHandlerImpl();
  }

  /**
   * Create a handler, specifying web-root
   *
   * @param root  the web-root
   * @return the handler
   */
  static StaticHandler create(String root) {
    return new StaticHandlerImpl(root);
  }

  /**
   * Set the web root
   *
   * @param webRoot  the web root
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setWebRoot(String webRoot);

  /**
   * Set whether files are read-only and will never change
   *
   * @param readOnly  whether files are read-only
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setFilesReadOnly(boolean readOnly);

  /**
   * Set value for max age in caching headers
   *
   * @param maxAgeSeconds  maximum time for browser to cache, in seconds
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setMaxAgeSeconds(long maxAgeSeconds);

  /**
   * Set whether cache header handling is enabled
   *
   * @param enabled  true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setCachingEnabled(boolean enabled);

  /**
   * Set whether directory listing is enabled
   *
   * @param directoryListing  true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setDirectoryListing(boolean directoryListing);

  /**
   * Set whether hidden files should be served
   *
   * @param includeHidden  true if hidden files should be served
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setIncludeHidden(boolean includeHidden);

  /**
   * Set the server cache entry timeout when caching is enabled
   *
   * @param timeout  the timeout, in ms
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setCacheEntryTimeout(long timeout);

  /**
   * Set the index page
   *
   * @param indexPage  the index page
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setIndexPage(String indexPage);

  /**
   * Set the max cache size, when caching is enabled
   *
   * @param maxCacheSize  the max cache size
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setMaxCacheSize(int maxCacheSize);

  /**
   * Set whether async filesystem access should always be used
   *
   * @param alwaysAsyncFS  true for always async FS access
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS);

  /**
   * Set whether async/sync filesystem tuning should enabled
   *
   * @param enableFSTuning  true to enabled FS tuning
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setEnableFSTuning(boolean enableFSTuning);

  /**
   * Set the max serve time in ns, above which serves are considered slow
   *
   * @param maxAvgServeTimeNanoSeconds  max serve time, in ns
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds);

  /**
   * Set the directory template to be used when directory listing
   *
   * @param directoryTemplate  the directory template
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setDirectoryTemplate(String directoryTemplate);

}
