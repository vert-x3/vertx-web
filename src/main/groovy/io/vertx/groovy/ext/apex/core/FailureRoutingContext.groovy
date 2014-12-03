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
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class FailureRoutingContext extends RoutingContext {
  final def io.vertx.ext.apex.core.FailureRoutingContext delegate;
  public FailureRoutingContext(io.vertx.ext.apex.core.FailureRoutingContext delegate) {
    super(delegate);
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public Throwable failure() {
    if (cached_0 != null) {
      return cached_0;
    }
    def ret = this.delegate.failure();
    cached_0 = ret;
    return ret;
  }
  public int statusCode() {
    if (cached_1 != null) {
      return cached_1;
    }
    def ret = this.delegate.statusCode();
    cached_1 = ret;
    return ret;
  }
  private java.lang.Throwable cached_0;
  private int cached_1;

  static final java.util.function.Function<io.vertx.ext.apex.core.FailureRoutingContext, FailureRoutingContext> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.core.FailureRoutingContext arg -> new FailureRoutingContext(arg);
  };
}
