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
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class BodyHandler {
  final def io.vertx.ext.apex.core.BodyHandler delegate;
  public BodyHandler(io.vertx.ext.apex.core.BodyHandler delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static BodyHandler bodyHandler() {
    def ret= BodyHandler.FACTORY.apply(io.vertx.ext.apex.core.BodyHandler.bodyHandler());
    return ret;
  }
  public static BodyHandler bodyHandler(long bodyLimit) {
    def ret= BodyHandler.FACTORY.apply(io.vertx.ext.apex.core.BodyHandler.bodyHandler(bodyLimit));
    return ret;
  }
  public static BodyHandler bodyHandler(String uploadsDirectory) {
    def ret= BodyHandler.FACTORY.apply(io.vertx.ext.apex.core.BodyHandler.bodyHandler(uploadsDirectory));
    return ret;
  }
  public static BodyHandler bodyHandler(long bodyLimit, String uploadsDirectory) {
    def ret= BodyHandler.FACTORY.apply(io.vertx.ext.apex.core.BodyHandler.bodyHandler(bodyLimit, uploadsDirectory));
    return ret;
  }
  public void handle(RoutingContext context) {
    this.delegate.handle((io.vertx.ext.apex.core.RoutingContext)context.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.core.BodyHandler, BodyHandler> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.core.BodyHandler arg -> new BodyHandler(arg);
  };
}
