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

package io.vertx.groovy.ext.apex.addons;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.groovy.ext.apex.core.RoutingContext
import io.vertx.ext.apex.addons.Logger.Format
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
@CompileStatic
public class Logger {
  final def io.vertx.ext.apex.addons.Logger delegate;
  public Logger(io.vertx.ext.apex.addons.Logger delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static Logger logger() {
    def ret= Logger.FACTORY.apply(io.vertx.ext.apex.addons.Logger.logger());
    return ret;
  }
  public static Logger logger(Format format) {
    def ret= Logger.FACTORY.apply(io.vertx.ext.apex.addons.Logger.logger(format));
    return ret;
  }
  public static Logger logger(boolean immediate, Format format) {
    def ret= Logger.FACTORY.apply(io.vertx.ext.apex.addons.Logger.logger(immediate, format));
    return ret;
  }
  public void handle(RoutingContext event) {
    this.delegate.handle((io.vertx.ext.apex.core.RoutingContext)event.getDelegate());
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.Logger, Logger> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.Logger arg -> new Logger(arg);
  };
}
