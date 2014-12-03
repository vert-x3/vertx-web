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
import io.vertx.groovy.core.http.HttpServerRequest
import io.vertx.groovy.core.http.HttpServerResponse
import io.vertx.groovy.core.Vertx
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class RoutingContext {
  final def io.vertx.ext.apex.core.RoutingContext delegate;
  public RoutingContext(io.vertx.ext.apex.core.RoutingContext delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static RoutingContext getContext() {
    def ret= RoutingContext.FACTORY.apply(io.vertx.ext.apex.core.RoutingContext.getContext());
    return ret;
  }
  public HttpServerRequest request() {
    if (cached_0 != null) {
      return cached_0;
    }
    def ret= HttpServerRequest.FACTORY.apply(this.delegate.request());
    cached_0 = ret;
    return ret;
  }
  public HttpServerResponse response() {
    if (cached_1 != null) {
      return cached_1;
    }
    def ret= HttpServerResponse.FACTORY.apply(this.delegate.response());
    cached_1 = ret;
    return ret;
  }
  public void next() {
    this.delegate.next();
  }
  public void fail(int statusCode) {
    this.delegate.fail(statusCode);
  }
  public void put(String key, Object obj) {
    this.delegate.put(key, InternalHelper.unwrapObject(obj));
  }
  public <T> T get(String key) {
    // This cast is cleary flawed
    def ret = (T) InternalHelper.wrapObject(this.delegate.get(key));
    return ret;
  }
  public Vertx vertx() {
    def ret= Vertx.FACTORY.apply(this.delegate.vertx());
    return ret;
  }
  public void addHeadersEndHandler(Handler<Void> handler) {
    this.delegate.addHeadersEndHandler(handler);
  }
  public boolean removeHeadersEndHandler(Handler<Void> handler) {
    def ret = this.delegate.removeHeadersEndHandler(handler);
    return ret;
  }
  public void addBodyEndHandler(Handler<Void> handler) {
    this.delegate.addBodyEndHandler(handler);
  }
  public boolean removeBodyEndHandler(Handler<Void> handler) {
    def ret = this.delegate.removeBodyEndHandler(handler);
    return ret;
  }
  public void setHandled(boolean handled) {
    this.delegate.setHandled(handled);
  }
  public void unhandled() {
    this.delegate.unhandled();
  }
  public boolean failed() {
    def ret = this.delegate.failed();
    return ret;
  }
  public String mountPoint() {
    def ret = this.delegate.mountPoint();
    return ret;
  }
  public Route currentRoute() {
    def ret= Route.FACTORY.apply(this.delegate.currentRoute());
    return ret;
  }
  public String normalisedPath() {
    def ret = this.delegate.normalisedPath();
    return ret;
  }
  private HttpServerRequest cached_0;
  private HttpServerResponse cached_1;

  static final java.util.function.Function<io.vertx.ext.apex.core.RoutingContext, RoutingContext> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.core.RoutingContext arg -> new RoutingContext(arg);
  };
}
