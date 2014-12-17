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
import java.util.List
import io.vertx.core.http.HttpMethod
import io.vertx.groovy.core.Vertx
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class Router {
  final def io.vertx.ext.apex.core.Router delegate;
  public Router(io.vertx.ext.apex.core.Router delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static Router router(Vertx vertx) {
    def ret= Router.FACTORY.apply(io.vertx.ext.apex.core.Router.router((io.vertx.core.Vertx)vertx.getDelegate()));
    return ret;
  }
  public void accept(HttpServerRequest request) {
    this.delegate.accept((io.vertx.core.http.HttpServerRequest)request.getDelegate());
  }
  public void handleContext(RoutingContext context) {
    this.delegate.handleContext((io.vertx.ext.apex.core.RoutingContext)context.getDelegate());
  }
  public void handleFailure(FailureRoutingContext context) {
    this.delegate.handleFailure((io.vertx.ext.apex.core.FailureRoutingContext)context.getDelegate());
  }
  public Route route() {
    def ret= Route.FACTORY.apply(this.delegate.route());
    return ret;
  }
  public Route route(HttpMethod method, String path) {
    def ret= Route.FACTORY.apply(this.delegate.route(method, path));
    return ret;
  }
  public Route route(String path) {
    def ret= Route.FACTORY.apply(this.delegate.route(path));
    return ret;
  }
  public Route routeWithRegex(HttpMethod method, String regex) {
    def ret= Route.FACTORY.apply(this.delegate.routeWithRegex(method, regex));
    return ret;
  }
  public Route routeWithRegex(String regex) {
    def ret= Route.FACTORY.apply(this.delegate.routeWithRegex(regex));
    return ret;
  }
  public List<Route> getRoutes() {
    def ret = this.delegate.getRoutes()?.collect({underpants -> Route.FACTORY.apply(underpants)});
      return ret;
  }
  public Router clear() {
    def ret= Router.FACTORY.apply(this.delegate.clear());
    return ret;
  }
  public Router mountSubRouter(String mountPoint, Router subRouter) {
    def ret= Router.FACTORY.apply(this.delegate.mountSubRouter(mountPoint, (io.vertx.ext.apex.core.Router)subRouter.getDelegate()));
    return ret;
  }
  public Router exceptionHandler(Handler<Throwable> exceptionHandler) {
    def ret= Router.FACTORY.apply(this.delegate.exceptionHandler(exceptionHandler));
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.core.Router, Router> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.core.Router arg -> new Router(arg);
  };
}
