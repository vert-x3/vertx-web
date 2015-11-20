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

package io.vertx.rxjava.ext.web.handler;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.core.Handler;

/**
 * A handler for serving static resources from the file system or classpath.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.StaticHandler original} non RX-ified interface using Vert.x codegen.
 */

public class StaticHandler implements Handler<RoutingContext> {

  final io.vertx.ext.web.handler.StaticHandler delegate;

  public StaticHandler(io.vertx.ext.web.handler.StaticHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void handle(RoutingContext arg0) { 
    this.delegate.handle((io.vertx.ext.web.RoutingContext) arg0.getDelegate());
  }

  /**
   * Create a handler using defaults
   * @return the handler
   */
  public static StaticHandler create() { 
    StaticHandler ret= StaticHandler.newInstance(io.vertx.ext.web.handler.StaticHandler.create());
    return ret;
  }

  /**
   * Create a handler, specifying web-root
   * @param root the web-root
   * @return the handler
   */
  public static StaticHandler create(String root) { 
    StaticHandler ret= StaticHandler.newInstance(io.vertx.ext.web.handler.StaticHandler.create(root));
    return ret;
  }

  /**
   * Enable/Disable access to the root of the filesystem
   * @param allowRootFileSystemAccess whether root access is allowed
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setAllowRootFileSystemAccess(boolean allowRootFileSystemAccess) { 
    this.delegate.setAllowRootFileSystemAccess(allowRootFileSystemAccess);
    return this;
  }

  /**
   * Set the web root
   * @param webRoot the web root
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setWebRoot(String webRoot) { 
    this.delegate.setWebRoot(webRoot);
    return this;
  }

  /**
   * Set whether files are read-only and will never change
   * @param readOnly whether files are read-only
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setFilesReadOnly(boolean readOnly) { 
    this.delegate.setFilesReadOnly(readOnly);
    return this;
  }

  /**
   * Set value for max age in caching headers
   * @param maxAgeSeconds maximum time for browser to cache, in seconds
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setMaxAgeSeconds(long maxAgeSeconds) { 
    this.delegate.setMaxAgeSeconds(maxAgeSeconds);
    return this;
  }

  /**
   * Set whether cache header handling is enabled
   * @param enabled true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setCachingEnabled(boolean enabled) { 
    this.delegate.setCachingEnabled(enabled);
    return this;
  }

  /**
   * Set whether directory listing is enabled
   * @param directoryListing true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setDirectoryListing(boolean directoryListing) { 
    this.delegate.setDirectoryListing(directoryListing);
    return this;
  }

  /**
   * Set whether hidden files should be served
   * @param includeHidden true if hidden files should be served
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setIncludeHidden(boolean includeHidden) { 
    this.delegate.setIncludeHidden(includeHidden);
    return this;
  }

  /**
   * Set the server cache entry timeout when caching is enabled
   * @param timeout the timeout, in ms
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setCacheEntryTimeout(long timeout) { 
    this.delegate.setCacheEntryTimeout(timeout);
    return this;
  }

  /**
   * Set the index page
   * @param indexPage the index page
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setIndexPage(String indexPage) { 
    this.delegate.setIndexPage(indexPage);
    return this;
  }

  /**
   * Set the max cache size, when caching is enabled
   * @param maxCacheSize the max cache size
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setMaxCacheSize(int maxCacheSize) { 
    this.delegate.setMaxCacheSize(maxCacheSize);
    return this;
  }

  /**
   * Set whether async filesystem access should always be used
   * @param alwaysAsyncFS true for always async FS access
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS) { 
    this.delegate.setAlwaysAsyncFS(alwaysAsyncFS);
    return this;
  }

  /**
   * Set whether async/sync filesystem tuning should enabled
   * @param enableFSTuning true to enabled FS tuning
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setEnableFSTuning(boolean enableFSTuning) { 
    this.delegate.setEnableFSTuning(enableFSTuning);
    return this;
  }

  /**
   * Set the max serve time in ns, above which serves are considered slow
   * @param maxAvgServeTimeNanoSeconds max serve time, in ns
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds) { 
    this.delegate.setMaxAvgServeTimeNs(maxAvgServeTimeNanoSeconds);
    return this;
  }

  /**
   * Set the directory template to be used when directory listing
   * @param directoryTemplate the directory template
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setDirectoryTemplate(String directoryTemplate) { 
    this.delegate.setDirectoryTemplate(directoryTemplate);
    return this;
  }

  /**
   * Set whether range requests (resumable downloads; media streaming) should be enabled.
   * @param enableRangeSupport true to enable range support
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setEnableRangeSupport(boolean enableRangeSupport) { 
    this.delegate.setEnableRangeSupport(enableRangeSupport);
    return this;
  }


  public static StaticHandler newInstance(io.vertx.ext.web.handler.StaticHandler arg) {
    return arg != null ? new StaticHandler(arg) : null;
  }
}
