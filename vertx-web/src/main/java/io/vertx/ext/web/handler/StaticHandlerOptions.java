/*
 * Copyright 2024 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Http2PushMapping;
import io.vertx.ext.web.common.WebEnvironment;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Options for the {@link StaticHandler}.
 */
@DataObject
@JsonGen(publicConverter = false)
public class StaticHandlerOptions {

  /**
   * Default value of whether files are read-only and will never be updated.
   */
  public static final boolean DEFAULT_FILES_READ_ONLY = true;

  /**
   * Default max age for cache headers.
   */
  public static final long DEFAULT_MAX_AGE_SECONDS = 86400; // One day

  /**
   * Default of whether cache header handling is enabled.
   */
  public static final boolean DEFAULT_CACHING_ENABLED = !WebEnvironment.development();

  /**
   * Default of whether directory listing is enabled.
   */
  public static final boolean DEFAULT_DIRECTORY_LISTING = false;

  /**
   * Default template file to use for directory listing.
   */
  public static final String DEFAULT_DIRECTORY_TEMPLATE = "META-INF/vertx/web/vertx-web-directory.html";

  /**
   * Default of whether hidden files can be served.
   */
  public static final boolean DEFAULT_INCLUDE_HIDDEN = true;

  /**
   * Default cache entry timeout, when caching.
   */
  public static final long DEFAULT_CACHE_ENTRY_TIMEOUT = 30000; // 30 seconds

  /**
   * The default index page.
   */
  public static final String DEFAULT_INDEX_PAGE = "index.html";

  /**
   * The default max cache size.
   */
  public static final int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default of whether async filesystem access should always be used.
   */
  public static final boolean DEFAULT_ALWAYS_ASYNC_FS = false;

  /**
   * Default of whether fs async/sync tuning should be used.
   */
  public static final boolean DEFAULT_ENABLE_FS_TUNING = true;

  /**
   * Default max avg serve time, in ns, over which serving will be considered slow.
   */
  public static final long DEFAULT_MAX_AVG_SERVE_TIME_NS = 1000000; // 1ms

  /**
   * Default of whether Range request handling support should be used
   */
  public static final boolean DEFAULT_RANGE_SUPPORT = true;

  /**
   * Default of whether vary header should be sent.
   */
  public static final boolean DEFAULT_SEND_VARY_HEADER = true;

  private boolean filesReadOnly;
  private long maxAgeSeconds;
  private boolean cachingEnabled;
  private boolean directoryListing;
  private boolean includeHidden;
  private long cacheEntryTimeout;
  private String indexPage;
  private int maxCacheSize;
  private List<Http2PushMapping> http2PushMappings;
  private Set<String> compressedMediaTypes;
  private Set<String> compressedFileSuffixes;
  private boolean alwaysAsyncFS;
  private boolean enableFSTuning;
  private long maxAvgServeTimeNs;
  private String directoryTemplate;
  private boolean enableRangeSupport;
  private boolean sendVaryHeader;
  private String defaultContentEncoding;

  /**
   * Default constructor.
   */
  public StaticHandlerOptions() {
    filesReadOnly = DEFAULT_FILES_READ_ONLY;
    maxAgeSeconds = DEFAULT_MAX_AGE_SECONDS;
    cachingEnabled = DEFAULT_CACHING_ENABLED;
    directoryListing = DEFAULT_DIRECTORY_LISTING;
    includeHidden = DEFAULT_INCLUDE_HIDDEN;
    cacheEntryTimeout = DEFAULT_CACHE_ENTRY_TIMEOUT;
    indexPage = DEFAULT_INDEX_PAGE;
    maxCacheSize = DEFAULT_MAX_CACHE_SIZE;
    alwaysAsyncFS = DEFAULT_ALWAYS_ASYNC_FS;
    enableFSTuning = DEFAULT_ENABLE_FS_TUNING;
    maxAvgServeTimeNs = DEFAULT_MAX_AVG_SERVE_TIME_NS;
    directoryTemplate = DEFAULT_DIRECTORY_TEMPLATE;
    enableRangeSupport = DEFAULT_RANGE_SUPPORT;
    sendVaryHeader = DEFAULT_SEND_VARY_HEADER;
    defaultContentEncoding = Charset.defaultCharset().name();
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public StaticHandlerOptions(StaticHandlerOptions other) {
    this();
    filesReadOnly = other.filesReadOnly;
    maxAgeSeconds = other.maxAgeSeconds;
    cachingEnabled = other.cachingEnabled;
    directoryListing = other.directoryListing;
    includeHidden = other.includeHidden;
    cacheEntryTimeout = other.cacheEntryTimeout;
    indexPage = other.indexPage;
    maxCacheSize = other.maxCacheSize;
    if (other.http2PushMappings != null) {
      http2PushMappings = new ArrayList<>(other.http2PushMappings.size());
      for (Http2PushMapping mapping : other.http2PushMappings) {
        http2PushMappings.add(new Http2PushMapping(mapping));
      }
    }
    if (other.compressedMediaTypes != null) {
      compressedMediaTypes = new HashSet<>(other.compressedMediaTypes);
    }
    if (other.compressedFileSuffixes != null) {
      compressedFileSuffixes = new HashSet<>(other.compressedFileSuffixes);
    }
    alwaysAsyncFS = other.alwaysAsyncFS;
    enableFSTuning = other.enableFSTuning;
    maxAvgServeTimeNs = other.maxAvgServeTimeNs;
    directoryTemplate = other.directoryTemplate;
    enableRangeSupport = other.enableRangeSupport;
    sendVaryHeader = other.sendVaryHeader;
    defaultContentEncoding = other.defaultContentEncoding;
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public StaticHandlerOptions(JsonObject json) {
    this();
    StaticHandlerOptionsConverter.fromJson(json, this);
  }


  /**
   * Whether files are read-only and will never change.
   *
   * @param filesReadOnly whether files are read-only
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setFilesReadOnly(boolean filesReadOnly) {
    this.filesReadOnly = filesReadOnly;
    return this;
  }

  /**
   * Set value for max age in caching headers.
   *
   * @param maxAgeSeconds maximum time for browser to cache, in seconds
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setMaxAgeSeconds(long maxAgeSeconds) {
    if (maxAgeSeconds < 0) {
      throw new IllegalArgumentException("timeout must be >= 0");
    }
    this.maxAgeSeconds = maxAgeSeconds;
    return this;
  }

  /**
   * Whether cache header handling is enabled.
   *
   * @param cachingEnabled true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setCachingEnabled(boolean cachingEnabled) {
    this.cachingEnabled = cachingEnabled;
    return this;
  }

  /**
   * Whether directory listing is enabled.
   *
   * @param directoryListing true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setDirectoryListing(boolean directoryListing) {
    this.directoryListing = directoryListing;
    return this;
  }

  /**
   * Whether hidden files should be served.
   *
   * @param includeHidden true if hidden files should be served
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setIncludeHidden(boolean includeHidden) {
    this.includeHidden = includeHidden;
    return this;
  }

  /**
   * Set the server cache entry timeout when caching is enabled.
   *
   * @param cacheEntryTimeout the timeout, in ms
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setCacheEntryTimeout(long cacheEntryTimeout) {
    if (cacheEntryTimeout < 1) {
      throw new IllegalArgumentException("timeout must be >= 1");
    }
    this.cacheEntryTimeout = cacheEntryTimeout;
    return this;
  }

  /**
   * Set the index page.
   *
   * @param indexPage the index page
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setIndexPage(String indexPage) {
    Objects.requireNonNull(indexPage);
    if (indexPage.charAt(0) == '/') {
      this.indexPage = indexPage.substring(1);
    } else {
      this.indexPage = indexPage;
    }
    return this;
  }

  /**
   * Set the max cache size, when caching is enabled.
   *
   * @param maxCacheSize the max cache size
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setMaxCacheSize(int maxCacheSize) {
    if (maxCacheSize < 1) {
      throw new IllegalArgumentException("maxCacheSize must be >= 1");
    }
    this.maxCacheSize = maxCacheSize;
    return this;
  }

  /**
   * Set the file mapping for HTTP/2 push and link preload.
   *
   * @param http2PushMappings the mapping for http2 push
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setHttp2PushMapping(List<Http2PushMapping> http2PushMappings) {
    this.http2PushMappings = http2PushMappings;
    return this;
  }

  /**
   * Skip compression if the media type of the file to send is in the provided {@code compressedMediaTypes} set.
   * <p>
   * {@code Content-Encoding} header set to {@code identity} for the types present in the {@code compressedMediaTypes} set.
   *
   * @param compressedMediaTypes the set of mime types that are already compressed
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setCompressedMediaTypes(Set<String> compressedMediaTypes) {
    this.compressedMediaTypes = compressedMediaTypes;
    return this;
  }

  /**
   * @see #setCompressedMediaTypes(Set)
   */
  public StaticHandlerOptions addCompressedMediaType(String compressedMediaType) {
    if (compressedMediaTypes == null) {
      compressedMediaTypes = new HashSet<>();
    }
    compressedMediaTypes.add(compressedMediaType);
    return this;
  }

  /**
   * Skip compression if the suffix of the file to send is in the provided {@code compressedFileSuffixes} set.
   * <p>
   * {@code Content-Encoding} header set to {@code identity} for the suffixes present in the {@code compressedFileSuffixes} set.
   *
   * @param compressedFileSuffixes the set of file suffixes that are already compressed
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setCompressedFileSuffixes(Set<String> compressedFileSuffixes) {
    this.compressedFileSuffixes = compressedFileSuffixes;
    return this;
  }

  /**
   * @see #setCompressedFileSuffixes(Set)
   */
  public StaticHandlerOptions addCompressedFileSuffix(String compressedFileSuffix) {
    if (compressedFileSuffixes == null) {
      compressedFileSuffixes = new HashSet<>();
    }
    compressedFileSuffixes.add(compressedFileSuffix);
    return this;
  }

  /**
   * Whether async filesystem access should always be used.
   *
   * @param alwaysAsyncFS true for always async FS access
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setAlwaysAsyncFS(boolean alwaysAsyncFS) {
    this.alwaysAsyncFS = alwaysAsyncFS;
    return this;
  }

  /**
   * Whether async/sync filesystem tuning should be enabled.
   *
   * @param enableFSTuning true to enabled FS tuning
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setEnableFSTuning(boolean enableFSTuning) {
    this.enableFSTuning = enableFSTuning;
    return this;
  }

  /**
   * Set the max serve time in ns, above which serves are considered slow.
   *
   * @param maxAvgServeTimeNs max serve time, in ns
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setMaxAvgServeTimeNs(long maxAvgServeTimeNs) {
    this.maxAvgServeTimeNs = maxAvgServeTimeNs;
    return this;
  }

  /**
   * Set the directory template to be used when directory listing.
   *
   * @param directoryTemplate the directory template
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setDirectoryTemplate(String directoryTemplate) {
    this.directoryTemplate = directoryTemplate;
    return this;
  }

  /**
   * Whether range requests (resumable downloads; media streaming) should be enabled.
   *
   * @param enableRangeSupport true to enable range support
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setEnableRangeSupport(boolean enableRangeSupport) {
    this.enableRangeSupport = enableRangeSupport;
    return this;
  }

  /**
   * Whether vary header should be sent with response.
   *
   * @param sendVaryHeader true to sent vary header
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setSendVaryHeader(boolean sendVaryHeader) {
    this.sendVaryHeader = sendVaryHeader;
    return this;
  }

  /**
   * Set the default content encoding for text related files.
   * <p>
   * This allows overriding the system settings default value.
   *
   * @param defaultContentEncoding the desired content encoding e.g.: "UTF-8"
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandlerOptions setDefaultContentEncoding(String defaultContentEncoding) {
    this.defaultContentEncoding = defaultContentEncoding;
    return this;
  }

  public boolean isFilesReadOnly() {
    return filesReadOnly;
  }

  public long getMaxAgeSeconds() {
    return maxAgeSeconds;
  }

  public boolean isCachingEnabled() {
    return cachingEnabled;
  }

  public boolean isDirectoryListing() {
    return directoryListing;
  }

  public boolean isIncludeHidden() {
    return includeHidden;
  }

  public long getCacheEntryTimeout() {
    return cacheEntryTimeout;
  }

  public String getIndexPage() {
    return indexPage;
  }

  public int getMaxCacheSize() {
    return maxCacheSize;
  }

  public List<Http2PushMapping> getHttp2PushMappings() {
    return http2PushMappings;
  }

  public Set<String> getCompressedMediaTypes() {
    return compressedMediaTypes;
  }

  public Set<String> getCompressedFileSuffixes() {
    return compressedFileSuffixes;
  }

  public boolean isAlwaysAsyncFS() {
    return alwaysAsyncFS;
  }

  public boolean isEnableFSTuning() {
    return enableFSTuning;
  }

  public long getMaxAvgServeTimeNs() {
    return maxAvgServeTimeNs;
  }

  public String getDirectoryTemplate() {
    return directoryTemplate;
  }

  public boolean isEnableRangeSupport() {
    return enableRangeSupport;
  }

  public boolean isSendVaryHeader() {
    return sendVaryHeader;
  }

  public String getDefaultContentEncoding() {
    return defaultContentEncoding;
  }
}
