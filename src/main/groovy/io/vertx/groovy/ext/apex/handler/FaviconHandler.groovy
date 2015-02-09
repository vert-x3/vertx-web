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
 * A handler that serves favicons.
 * <p>
 * If no file system path is specified it will attempt to serve a resource called `favicon.ico` from the classpath.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class FaviconHandler {
  final def io.vertx.ext.apex.handler.FaviconHandler delegate;
  public FaviconHandler(io.vertx.ext.apex.handler.FaviconHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a handler with defaults
   *
   * @return the handler
   */
  public static FaviconHandler create() {
    def ret= FaviconHandler.FACTORY.apply(io.vertx.ext.apex.handler.FaviconHandler.create());
    return ret;
  }
  /**
   * Create a handler attempting to load favicon file from the specified path
   *
   * @param path  the path
   * @return the handler
   */
  public static FaviconHandler create(String path) {
    def ret= FaviconHandler.FACTORY.apply(io.vertx.ext.apex.handler.FaviconHandler.create(path));
    return ret;
  }
  /**
   * Create a handler attempting to load favicon file from the specified path, and with the specified max cache time
   *
   * @param path  the path
   * @param maxAgeSeconds max how long the file will be cached by browser, in seconds
   * @return the handler
   */
  public static FaviconHandler create(String path, long maxAgeSeconds) {
    def ret= FaviconHandler.FACTORY.apply(io.vertx.ext.apex.handler.FaviconHandler.create(path, maxAgeSeconds));
    return ret;
  }
  /**
   * Create a handler with the specified max cache time
   *
   * @param maxAgeSeconds max how long the file will be cached by browser, in seconds
   * @return the handler
   */
  public static FaviconHandler create(long maxAgeSeconds) {
    def ret= FaviconHandler.FACTORY.apply(io.vertx.ext.apex.handler.FaviconHandler.create(maxAgeSeconds));
    return ret;
  }
  public void handle(RoutingContext event) {
    this.delegate.handle((io.vertx.ext.apex.RoutingContext)event.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.handler.FaviconHandler, FaviconHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.handler.FaviconHandler arg -> new FaviconHandler(arg);
  };
}
