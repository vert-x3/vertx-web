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
 * Handler which adds a header `x-response-time` in the response of matching requests containing the time taken
 * in ms to process the request.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class ResponseTimeHandler {
  final def io.vertx.ext.apex.handler.ResponseTimeHandler delegate;
  public ResponseTimeHandler(io.vertx.ext.apex.handler.ResponseTimeHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Create a handler
   *
   * @return the handler
   */
  public static ResponseTimeHandler create() {
    def ret= ResponseTimeHandler.FACTORY.apply(io.vertx.ext.apex.handler.ResponseTimeHandler.create());
    return ret;
  }
  public void handle(RoutingContext event) {
    this.delegate.handle((io.vertx.ext.apex.RoutingContext)event.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.handler.ResponseTimeHandler, ResponseTimeHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.handler.ResponseTimeHandler arg -> new ResponseTimeHandler(arg);
  };
}
