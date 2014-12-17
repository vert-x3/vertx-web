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
import io.vertx.groovy.core.buffer.Buffer
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class TemplateEngine {
  final def io.vertx.ext.apex.addons.TemplateEngine delegate;
  public TemplateEngine(io.vertx.ext.apex.addons.TemplateEngine delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    this.delegate.render((io.vertx.ext.apex.core.RoutingContext)context.getDelegate(), templateFileName, new Handler<AsyncResult<io.vertx.core.buffer.Buffer>>() {
      public void handle(AsyncResult<io.vertx.core.buffer.Buffer> event) {
        AsyncResult<Buffer> f
        if (event.succeeded()) {
          f = InternalHelper.<Buffer>result(new Buffer(event.result()))
        } else {
          f = InternalHelper.<Buffer>failure(event.cause())
        }
        handler.handle(f)
      }
    });
  }
  public void renderResponse(RoutingContext context, String templateFileName, String contentType) {
    this.delegate.renderResponse((io.vertx.ext.apex.core.RoutingContext)context.getDelegate(), templateFileName, contentType);
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.TemplateEngine, TemplateEngine> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.TemplateEngine arg -> new TemplateEngine(arg);
  };
}
