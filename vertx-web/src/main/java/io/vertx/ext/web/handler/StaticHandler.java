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
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.Http2PushMapping;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.StaticHandlerImpl;

import java.util.List;
import java.util.Set;

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
  String DEFAULT_WEB_ROOT = "webroot";

  /**
   * Default value of whether files are read -only and never will be updated
   */
  boolean DEFAULT_FILES_READ_ONLY = true;

  /**
   * Default max age for cache headers
   */
  long DEFAULT_MAX_AGE_SECONDS = 86400; // One day

  /**
   * Default of whether cache header handling is enabled
   */
  boolean DEFAULT_CACHING_ENABLED = true;

  /**
   * Default of whether directory listing is enabled
   */
  boolean DEFAULT_DIRECTORY_LISTING = false;

  /**
   * Default template file to use for directory listing
   */
  String DEFAULT_DIRECTORY_TEMPLATE = "META-INF/vertx/web/vertx-web-directory.html";

  /**
   * Default of whether hidden files can be served
   */
  boolean DEFAULT_INCLUDE_HIDDEN = true;

  /**
   * Default cache entry timeout, when caching
   */
  long DEFAULT_CACHE_ENTRY_TIMEOUT = 30000; // 30 seconds

  /**
   * The default index page
   */
  String DEFAULT_INDEX_PAGE = "/index.html";

  /**
   * The default max cache size
   */
  int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default of whether async filesystem access should always be used
   */
  boolean DEFAULT_ALWAYS_ASYNC_FS = false;

  /**
   * Default of whether fs async/sync tuning should be used
   */
  boolean DEFAULT_ENABLE_FS_TUNING = true;

  /**
   * Default max avg serve time, in ns, over which serving will be considered slow
   */
  long DEFAULT_MAX_AVG_SERVE_TIME_NS = 1000000; // 1ms

  /**
   * Default of whether Range request handling support should be used
   */
  boolean DEFAULT_RANGE_SUPPORT = true;

  /**
   * Default of whether access to the root of the file system should be allowed or just allow from the current working
   * directory.
   */
  boolean DEFAULT_ROOT_FILESYSTEM_ACCESS = false;

  /**
   * Default of whether vary header should be sent.
   */
  boolean DEFAULT_SEND_VARY_HEADER = true;

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
   * @param root the web-root
   * @return the handler
   */
  static StaticHandler create(String root) {
    return new StaticHandlerImpl(root, null);
  }

  /**
   * Create a handler, specifying web-root and a classloader used to load the resources.
   *
   * @param root        the web-root
   * @param classLoader the classloader used to load the resource
   * @return the handler
   */
  @GenIgnore
  static StaticHandler create(String root, ClassLoader classLoader) {
    return new StaticHandlerImpl(root, classLoader);
  }

  /**
   * Enable/Disable access to the root of the filesystem
   *
   * @param allowRootFileSystemAccess whether root access is allowed
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setAllowRootFileSystemAccess(boolean allowRootFileSystemAccess);

  /**
   * Set the web root
   *
   * @param webRoot the web root
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setWebRoot(String webRoot);

  /**
   * Set whether files are read-only and will never change
   *
   * @param readOnly whether files are read-only
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setFilesReadOnly(boolean readOnly);

  /**
   * Set value for max age in caching headers
   *
   * @param maxAgeSeconds maximum time for browser to cache, in seconds
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setMaxAgeSeconds(long maxAgeSeconds);

  /**
   * Set whether cache header handling is enabled
   *
   * @param enabled true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setCachingEnabled(boolean enabled);

  /**
   * Set whether directory listing is enabled
   *
   * @param directoryListing true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setDirectoryListing(boolean directoryListing);

  /**
   * Set whether hidden files should be served
   *
   * @param includeHidden true if hidden files should be served
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setIncludeHidden(boolean includeHidden);

  /**
   * Set the server cache entry timeout when caching is enabled
   *
   * @param timeout the timeout, in ms
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setCacheEntryTimeout(long timeout);

  /**
   * Set the index page
   *
   * @param indexPage the index page
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setIndexPage(String indexPage);

  /**
   * Set the max cache size, when caching is enabled
   *
   * @param maxCacheSize the max cache size
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setMaxCacheSize(int maxCacheSize);

  /**
   * Set the file mapping for http2push and link preload
   *
   * @param http2PushMappings the mapping for http2 push
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setHttp2PushMapping(List<Http2PushMapping> http2PushMappings);

  /**
   * Skip compression if the media type of the file to send is in the provided {@code mediaTypes} set.
   * {@code Content-Encoding} header set to {@code identity} for the types present in the {@code mediaTypes} set
   *
   * @param mediaTypes the set of mime types that are already compressed
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler skipCompressionForMediaTypes(Set<String> mediaTypes);

  /**
   * Skip compression if the suffix of the file to send is in the provided {@code fileSuffixes} set.
   * {@code Content-Encoding} header set to {@code identity} for the suffixes present in the {@code fileSuffixes} set
   *
   * @param fileSuffixes the set of file suffixes that are already compressed
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler skipCompressionForSuffixes(Set<String> fileSuffixes);

  /**
   * Set whether async filesystem access should always be used
   *
   * @param alwaysAsyncFS true for always async FS access
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS);

  /**
   * Set whether async/sync filesystem tuning should enabled
   *
   * @param enableFSTuning true to enabled FS tuning
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setEnableFSTuning(boolean enableFSTuning);

  /**
   * Set the max serve time in ns, above which serves are considered slow
   *
   * @param maxAvgServeTimeNanoSeconds max serve time, in ns
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds);

  /**
   * Set the directory template to be used when directory listing
   *
   * @param directoryTemplate the directory template
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setDirectoryTemplate(String directoryTemplate);

  /**
   * Set whether range requests (resumable downloads; media streaming) should be enabled.
   *
   * @param enableRangeSupport true to enable range support
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setEnableRangeSupport(boolean enableRangeSupport);

  /**
   * Set whether vary header should be sent with response.
   *
   * @param varyHeader true to sent vary header
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setSendVaryHeader(boolean varyHeader);

  /**
   * Set the default content encoding for text related files. This allows overriding the system settings default value.
   *
   * @param contentEncoding the desired content encoding e.g.: "UTF-8"
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  StaticHandler setDefaultContentEncoding(String contentEncoding);
}
