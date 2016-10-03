/*
 * Copyright 2014 Red Hat, Inc.
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

package io.vertx.groovy.ext.web.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
/**
 * A handler for serving static resources from the file system or classpath.
*/
@CompileStatic
public class StaticHandler implements Handler<RoutingContext> {
  private final def io.vertx.ext.web.handler.StaticHandler delegate;
  public StaticHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.StaticHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) delegate).handle(arg0 != null ? (io.vertx.ext.web.RoutingContext)arg0.getDelegate() : null);
  }
  /**
   * Create a handler using defaults
   * @return the handler
   */
  public static StaticHandler create() {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.StaticHandler.create(), io.vertx.groovy.ext.web.handler.StaticHandler.class);
    return ret;
  }
  /**
   * Create a handler, specifying web-root
   * @param root the web-root
   * @return the handler
   */
  public static StaticHandler create(String root) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.StaticHandler.create(root), io.vertx.groovy.ext.web.handler.StaticHandler.class);
    return ret;
  }
  /**
   * Enable/Disable access to the root of the filesystem
   * @param allowRootFileSystemAccess whether root access is allowed
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setAllowRootFileSystemAccess(boolean allowRootFileSystemAccess) {
    delegate.setAllowRootFileSystemAccess(allowRootFileSystemAccess);
    return this;
  }
  /**
   * Set the web root
   * @param webRoot the web root
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setWebRoot(String webRoot) {
    delegate.setWebRoot(webRoot);
    return this;
  }
  /**
   * Set whether files are read-only and will never change
   * @param readOnly whether files are read-only
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setFilesReadOnly(boolean readOnly) {
    delegate.setFilesReadOnly(readOnly);
    return this;
  }
  /**
   * Set value for max age in caching headers
   * @param maxAgeSeconds maximum time for browser to cache, in seconds
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setMaxAgeSeconds(long maxAgeSeconds) {
    delegate.setMaxAgeSeconds(maxAgeSeconds);
    return this;
  }
  /**
   * Set whether cache header handling is enabled
   * @param enabled true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setCachingEnabled(boolean enabled) {
    delegate.setCachingEnabled(enabled);
    return this;
  }
  /**
   * Set whether directory listing is enabled
   * @param directoryListing true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setDirectoryListing(boolean directoryListing) {
    delegate.setDirectoryListing(directoryListing);
    return this;
  }
  /**
   * Set whether hidden files should be served
   * @param includeHidden true if hidden files should be served
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setIncludeHidden(boolean includeHidden) {
    delegate.setIncludeHidden(includeHidden);
    return this;
  }
  /**
   * Set the server cache entry timeout when caching is enabled
   * @param timeout the timeout, in ms
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setCacheEntryTimeout(long timeout) {
    delegate.setCacheEntryTimeout(timeout);
    return this;
  }
  /**
   * Set the index page
   * @param indexPage the index page
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setIndexPage(String indexPage) {
    delegate.setIndexPage(indexPage);
    return this;
  }
  /**
   * Set the max cache size, when caching is enabled
   * @param maxCacheSize the max cache size
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setMaxCacheSize(int maxCacheSize) {
    delegate.setMaxCacheSize(maxCacheSize);
    return this;
  }
  /**
   * Set whether async filesystem access should always be used
   * @param alwaysAsyncFS true for always async FS access
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS) {
    delegate.setAlwaysAsyncFS(alwaysAsyncFS);
    return this;
  }
  /**
   * Set whether async/sync filesystem tuning should enabled
   * @param enableFSTuning true to enabled FS tuning
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setEnableFSTuning(boolean enableFSTuning) {
    delegate.setEnableFSTuning(enableFSTuning);
    return this;
  }
  /**
   * Set the max serve time in ns, above which serves are considered slow
   * @param maxAvgServeTimeNanoSeconds max serve time, in ns
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds) {
    delegate.setMaxAvgServeTimeNs(maxAvgServeTimeNanoSeconds);
    return this;
  }
  /**
   * Set the directory template to be used when directory listing
   * @param directoryTemplate the directory template
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setDirectoryTemplate(String directoryTemplate) {
    delegate.setDirectoryTemplate(directoryTemplate);
    return this;
  }
  /**
   * Set whether range requests (resumable downloads; media streaming) should be enabled.
   * @param enableRangeSupport true to enable range support
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setEnableRangeSupport(boolean enableRangeSupport) {
    delegate.setEnableRangeSupport(enableRangeSupport);
    return this;
  }
  /**
   * Set whether vary header should be send with response.
   * @param varyHeader true to sent vary header
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setSendVaryHeader(boolean varyHeader) {
    delegate.setSendVaryHeader(varyHeader);
    return this;
  }
}
