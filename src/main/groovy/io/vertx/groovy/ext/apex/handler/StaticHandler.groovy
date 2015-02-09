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

package io.vertx.groovy.ext.apex.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.RoutingContext
import io.vertx.core.Handler
/**
 * A handler for serving static resources from the file system or classpath.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class StaticHandler {
  final def io.vertx.ext.apex.handler.StaticHandler delegate;
  public StaticHandler(io.vertx.ext.apex.handler.StaticHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a handler using defaults
   *
   * @return the handler
   */
  public static StaticHandler create() {
    def ret= StaticHandler.FACTORY.apply(io.vertx.ext.apex.handler.StaticHandler.create());
    return ret;
  }
  /**
   * Create a handler, specifying web-root
   *
   * @param root  the web-root
   * @return the handler
   */
  public static StaticHandler create(String root) {
    def ret= StaticHandler.FACTORY.apply(io.vertx.ext.apex.handler.StaticHandler.create(root));
    return ret;
  }
  public void handle(RoutingContext context) {
    this.delegate.handle((io.vertx.ext.apex.RoutingContext)context.getDelegate());
  }
  /**
   * Set the web root
   *
   * @param webRoot  the web root
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setWebRoot(String webRoot) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setWebRoot(webRoot));
    return ret;
  }
  /**
   * Set whether files are read-only and will never change
   *
   * @param readOnly  whether files are read-only
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setFilesReadOnly(boolean readOnly) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setFilesReadOnly(readOnly));
    return ret;
  }
  /**
   * Set value for max age in caching headers
   *
   * @param maxAgeSeconds  maximum time for browser to cache, in seconds
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setMaxAgeSeconds(long maxAgeSeconds) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setMaxAgeSeconds(maxAgeSeconds));
    return ret;
  }
  /**
   * Set whether cache header handling is enabled
   *
   * @param enabled  true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setCachingEnabled(boolean enabled) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setCachingEnabled(enabled));
    return ret;
  }
  /**
   * Set whether directory listing is enabled
   *
   * @param directoryListing  true if enabled
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setDirectoryListing(boolean directoryListing) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setDirectoryListing(directoryListing));
    return ret;
  }
  /**
   * Set whether hidden files should be served
   *
   * @param includeHidden  true if hidden files should be served
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setIncludeHidden(boolean includeHidden) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setIncludeHidden(includeHidden));
    return ret;
  }
  /**
   * Set the server cache entry timeout when caching is enabled
   *
   * @param timeout  the timeout, in ms
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setCacheEntryTimeout(long timeout) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setCacheEntryTimeout(timeout));
    return ret;
  }
  /**
   * Set the index page
   *
   * @param indexPage  the index page
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setIndexPage(String indexPage) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setIndexPage(indexPage));
    return ret;
  }
  /**
   * Set the max cache size, when caching is enabled
   *
   * @param maxCacheSize  the max cache size
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setMaxCacheSize(int maxCacheSize) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setMaxCacheSize(maxCacheSize));
    return ret;
  }
  /**
   * Set whether async filesystem access should always be used
   *
   * @param alwaysAsyncFS  true for always async FS access
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setAlwaysAsyncFS(boolean alwaysAsyncFS) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setAlwaysAsyncFS(alwaysAsyncFS));
    return ret;
  }
  /**
   * Set whether async/sync filesystem tuning should enabled
   *
   * @param enableFSTuning  true to enabled FS tuning
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setEnableFSTuning(boolean enableFSTuning) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setEnableFSTuning(enableFSTuning));
    return ret;
  }
  /**
   * Set the max serve time in ns, above which serves are considered slow
   *
   * @param maxAvgServeTimeNanoSeconds  max serve time, in ns
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setMaxAvgServeTimeNs(long maxAvgServeTimeNanoSeconds) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setMaxAvgServeTimeNs(maxAvgServeTimeNanoSeconds));
    return ret;
  }
  /**
   * Set the directory template to be used when directory listing
   *
   * @param directoryTemplate  the directory template
   * @return a reference to this, so the API can be used fluently
   */
  public StaticHandler setDirectoryTemplate(String directoryTemplate) {
    def ret= StaticHandler.FACTORY.apply(this.delegate.setDirectoryTemplate(directoryTemplate));
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.handler.StaticHandler, StaticHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.handler.StaticHandler arg -> new StaticHandler(arg);
  };
}
