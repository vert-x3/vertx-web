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
/**
 * Thread-safe but best performance is to be achieved by not sharing between contexts
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class HandlebarsTemplateEngine extends TemplateEngine {
  final def io.vertx.ext.apex.addons.HandlebarsTemplateEngine delegate;
  public HandlebarsTemplateEngine(io.vertx.ext.apex.addons.HandlebarsTemplateEngine delegate) {
    super(delegate);
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static HandlebarsTemplateEngine create() {
    def ret= HandlebarsTemplateEngine.FACTORY.apply(io.vertx.ext.apex.addons.HandlebarsTemplateEngine.create());
    return ret;
  }
  public static HandlebarsTemplateEngine create(String resourcePrefix, String extension) {
    def ret= HandlebarsTemplateEngine.FACTORY.apply(io.vertx.ext.apex.addons.HandlebarsTemplateEngine.create(resourcePrefix, extension));
    return ret;
  }
  public static HandlebarsTemplateEngine create(String resourcePrefix, String extension, int maxCacheSize) {
    def ret= HandlebarsTemplateEngine.FACTORY.apply(io.vertx.ext.apex.addons.HandlebarsTemplateEngine.create(resourcePrefix, extension, maxCacheSize));
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.HandlebarsTemplateEngine, HandlebarsTemplateEngine> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.HandlebarsTemplateEngine arg -> new HandlebarsTemplateEngine(arg);
  };
}
