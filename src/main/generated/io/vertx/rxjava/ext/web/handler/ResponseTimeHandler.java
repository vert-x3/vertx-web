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
 * Handler which adds a header `x-response-time` in the response of matching requests containing the time taken
 * in ms to process the request.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.handler.ResponseTimeHandler original} non RX-ified interface using Vert.x codegen.
 */

public class ResponseTimeHandler implements Handler<RoutingContext> {

  final io.vertx.ext.web.handler.ResponseTimeHandler delegate;

  public ResponseTimeHandler(io.vertx.ext.web.handler.ResponseTimeHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void handle(RoutingContext arg0) { 
    this.delegate.handle((io.vertx.ext.web.RoutingContext) arg0.getDelegate());
  }

  /**
   * Create a handler
   * @return the handler
   */
  public static ResponseTimeHandler create() { 
    ResponseTimeHandler ret= ResponseTimeHandler.newInstance(io.vertx.ext.web.handler.ResponseTimeHandler.create());
    return ret;
  }


  public static ResponseTimeHandler newInstance(io.vertx.ext.web.handler.ResponseTimeHandler arg) {
    return new ResponseTimeHandler(arg);
  }
}
