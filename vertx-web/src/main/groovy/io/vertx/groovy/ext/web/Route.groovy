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

package io.vertx.groovy.ext.web;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.core.http.HttpMethod
import io.vertx.core.Handler
@CompileStatic
public class Route {
  private final def io.vertx.ext.web.Route delegate;
  public Route(Object delegate) {
    this.delegate = (io.vertx.ext.web.Route) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public Route method(HttpMethod arg0) {
    delegate.method(arg0);
    return this;
  }
  public Route path(String arg0) {
    delegate.path(arg0);
    return this;
  }
  public Route pathRegex(String arg0) {
    delegate.pathRegex(arg0);
    return this;
  }
  public Route produces(String arg0) {
    delegate.produces(arg0);
    return this;
  }
  public Route consumes(String arg0) {
    delegate.consumes(arg0);
    return this;
  }
  public Route order(int arg0) {
    delegate.order(arg0);
    return this;
  }
  public Route last() {
    delegate.last();
    return this;
  }
  public Route handler(Handler<RoutingContext> arg0) {
    delegate.handler(arg0 != null ? new Handler<io.vertx.ext.web.RoutingContext>(){
      public void handle(io.vertx.ext.web.RoutingContext event) {
        arg0.handle(InternalHelper.safeCreate(event, io.vertx.groovy.ext.web.RoutingContext.class));
      }
    } : null);
    return this;
  }
  public Route blockingHandler(Handler<RoutingContext> arg0) {
    delegate.blockingHandler(arg0 != null ? new Handler<io.vertx.ext.web.RoutingContext>(){
      public void handle(io.vertx.ext.web.RoutingContext event) {
        arg0.handle(InternalHelper.safeCreate(event, io.vertx.groovy.ext.web.RoutingContext.class));
      }
    } : null);
    return this;
  }
  public Route blockingHandler(Handler<RoutingContext> arg0, boolean arg1) {
    delegate.blockingHandler(arg0 != null ? new Handler<io.vertx.ext.web.RoutingContext>(){
      public void handle(io.vertx.ext.web.RoutingContext event) {
        arg0.handle(InternalHelper.safeCreate(event, io.vertx.groovy.ext.web.RoutingContext.class));
      }
    } : null, arg1);
    return this;
  }
  public Route failureHandler(Handler<RoutingContext> arg0) {
    delegate.failureHandler(arg0 != null ? new Handler<io.vertx.ext.web.RoutingContext>(){
      public void handle(io.vertx.ext.web.RoutingContext event) {
        arg0.handle(InternalHelper.safeCreate(event, io.vertx.groovy.ext.web.RoutingContext.class));
      }
    } : null);
    return this;
  }
  public Route remove() {
    delegate.remove();
    return this;
  }
  public Route disable() {
    delegate.disable();
    return this;
  }
  public Route enable() {
    delegate.enable();
    return this;
  }
  public Route useNormalisedPath(boolean arg0) {
    delegate.useNormalisedPath(arg0);
    return this;
  }
  public String getPath() {
    def ret = delegate.getPath();
    return ret;
  }
}
