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
 * Handler which adds a header `x-response-time` in the response of matching requests containing the time taken
 * in ms to process the request.
*/
@CompileStatic
public class ResponseTimeHandler implements Handler<RoutingContext> {
  private final def io.vertx.ext.web.handler.ResponseTimeHandler delegate;
  public ResponseTimeHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.ResponseTimeHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.web.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create a handler
   * @return the handler
   */
  public static ResponseTimeHandler create() {
    def ret= InternalHelper.safeCreate(io.vertx.ext.web.handler.ResponseTimeHandler.create(), io.vertx.groovy.ext.web.handler.ResponseTimeHandler.class);
    return ret;
  }
}
