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
import io.vertx.ext.apex.handler.LoggerHandler.Format
import io.vertx.core.Handler
/**
 * A handler which logs request information to the Vert.x logger.
*/
@CompileStatic
public class LoggerHandler implements Handler<RoutingContext> {
  final def io.vertx.ext.apex.handler.LoggerHandler delegate;
  public LoggerHandler(io.vertx.ext.apex.handler.LoggerHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.apex.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create a handler with default format
   * @return the handler
   */
  public static LoggerHandler create() {
    def ret= new io.vertx.groovy.ext.apex.handler.LoggerHandler(io.vertx.ext.apex.handler.LoggerHandler.create());
    return ret;
  }
  /**
   * Create a handler with he specified format
   * @param format the format
   * @return the handler
   */
  public static LoggerHandler create(Format format) {
    def ret= new io.vertx.groovy.ext.apex.handler.LoggerHandler(io.vertx.ext.apex.handler.LoggerHandler.create(format));
    return ret;
  }
  /**
   * Create a handler with he specified format
   * @param immediate true if logging should occur as soon as request arrives
   * @param format the format
   * @return the handler
   */
  public static LoggerHandler create(boolean immediate, Format format) {
    def ret= new io.vertx.groovy.ext.apex.handler.LoggerHandler(io.vertx.ext.apex.handler.LoggerHandler.create(immediate, format));
    return ret;
  }
}
