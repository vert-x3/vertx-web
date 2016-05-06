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
 * A handler that serves favicons.
 * <p>
 * If no file system path is specified it will attempt to serve a resource called `favicon.ico` from the classpath.
*/
@CompileStatic
public class FaviconHandler implements Handler<RoutingContext> {
  private final def io.vertx.ext.web.handler.FaviconHandler delegate;
  public FaviconHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.FaviconHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) delegate).handle(arg0 != null ? (io.vertx.ext.web.RoutingContext)arg0.getDelegate() : null);
  }
  /**
   * Create a handler with defaults
   * @return the handler
   */
  public static FaviconHandler create() {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.FaviconHandler.create(), io.vertx.groovy.ext.web.handler.FaviconHandler.class);
    return ret;
  }
  /**
   * Create a handler attempting to load favicon file from the specified path
   * @param path the path
   * @return the handler
   */
  public static FaviconHandler create(String path) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.FaviconHandler.create(path), io.vertx.groovy.ext.web.handler.FaviconHandler.class);
    return ret;
  }
  /**
   * Create a handler attempting to load favicon file from the specified path, and with the specified max cache time
   * @param path the path
   * @param maxAgeSeconds max how long the file will be cached by browser, in seconds
   * @return the handler
   */
  public static FaviconHandler create(String path, long maxAgeSeconds) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.FaviconHandler.create(path, maxAgeSeconds), io.vertx.groovy.ext.web.handler.FaviconHandler.class);
    return ret;
  }
  /**
   * Create a handler with the specified max cache time
   * @param maxAgeSeconds max how long the file will be cached by browser, in seconds
   * @return the handler
   */
  public static FaviconHandler create(long maxAgeSeconds) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.web.handler.FaviconHandler.create(maxAgeSeconds), io.vertx.groovy.ext.web.handler.FaviconHandler.class);
    return ret;
  }
}
