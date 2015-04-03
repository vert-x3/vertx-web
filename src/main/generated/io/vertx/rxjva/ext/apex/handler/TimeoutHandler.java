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

package io.vertx.rxjava.ext.apex.handler;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.rxjava.ext.apex.RoutingContext;
import io.vertx.core.Handler;

/**
 * Handler that will timeout requests if the response has not been written after a certain time.
 * Timeout requests will be ended with an HTTP status code `408`.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.apex.handler.TimeoutHandler original} non RX-ified interface using Vert.x codegen.
 */

public class TimeoutHandler implements Handler<RoutingContext> {

  final io.vertx.ext.apex.handler.TimeoutHandler delegate;

  public TimeoutHandler(io.vertx.ext.apex.handler.TimeoutHandler delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public void handle(RoutingContext arg0) { 
    this.delegate.handle((io.vertx.ext.apex.RoutingContext) arg0.getDelegate());
  }

  /**
   * Create a handler
   * @return the handler
   */
  public static TimeoutHandler create() { 
    TimeoutHandler ret= TimeoutHandler.newInstance(io.vertx.ext.apex.handler.TimeoutHandler.create());
    return ret;
  }

  /**
   * Create a handler
   * @param timeout the timeout, in ms
   * @return the handler
   */
  public static TimeoutHandler create(long timeout) { 
    TimeoutHandler ret= TimeoutHandler.newInstance(io.vertx.ext.apex.handler.TimeoutHandler.create(timeout));
    return ret;
  }


  public static TimeoutHandler newInstance(io.vertx.ext.apex.handler.TimeoutHandler arg) {
    return new TimeoutHandler(arg);
  }
}
