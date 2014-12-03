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

package io.vertx.groovy.ext.apex.core;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.http.HttpMethod
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class Route {
  final def io.vertx.ext.apex.core.Route delegate;
  public Route(io.vertx.ext.apex.core.Route delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public Route method(HttpMethod method) {
    def ret= Route.FACTORY.apply(this.delegate.method(method));
    return ret;
  }
  public Route path(String path) {
    def ret= Route.FACTORY.apply(this.delegate.path(path));
    return ret;
  }
  public Route pathRegex(String path) {
    def ret= Route.FACTORY.apply(this.delegate.pathRegex(path));
    return ret;
  }
  public Route produces(String contentType) {
    def ret= Route.FACTORY.apply(this.delegate.produces(contentType));
    return ret;
  }
  public Route consumes(String contentType) {
    def ret= Route.FACTORY.apply(this.delegate.consumes(contentType));
    return ret;
  }
  public Route order(int order) {
    def ret= Route.FACTORY.apply(this.delegate.order(order));
    return ret;
  }
  public Route last(boolean last) {
    def ret= Route.FACTORY.apply(this.delegate.last(last));
    return ret;
  }
  public Route handler(Handler<RoutingContext> requestHandler) {
    def ret= Route.FACTORY.apply(this.delegate.handler(new Handler<io.vertx.ext.apex.core.RoutingContext>() {
      public void handle(io.vertx.ext.apex.core.RoutingContext event) {
        requestHandler.handle(RoutingContext.FACTORY.apply(event));
      }
    }));
    return ret;
  }
  public Route failureHandler(Handler<FailureRoutingContext> exceptionHandler) {
    def ret= Route.FACTORY.apply(this.delegate.failureHandler(new Handler<io.vertx.ext.apex.core.FailureRoutingContext>() {
      public void handle(io.vertx.ext.apex.core.FailureRoutingContext event) {
        exceptionHandler.handle(FailureRoutingContext.FACTORY.apply(event));
      }
    }));
    return ret;
  }
  public Route remove() {
    def ret= Route.FACTORY.apply(this.delegate.remove());
    return ret;
  }
  public Route disable() {
    def ret= Route.FACTORY.apply(this.delegate.disable());
    return ret;
  }
  public Route enable() {
    def ret= Route.FACTORY.apply(this.delegate.enable());
    return ret;
  }
  public String getPath() {
    def ret = this.delegate.getPath();
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.core.Route, Route> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.core.Route arg -> new Route(arg);
  };
}
