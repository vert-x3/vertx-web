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

package io.vertx.groovy.ext.web.templ;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.buffer.Buffer
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * A template engine uses a specific template and the data in a routing context to render a resource into a buffer.
 * <p>
 * Concrete implementations exist for several well-known template engines.
*/
@CompileStatic
public class TemplateEngine {
  private final def io.vertx.ext.web.templ.TemplateEngine delegate;
  public TemplateEngine(Object delegate) {
    this.delegate = (io.vertx.ext.web.templ.TemplateEngine) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Render
   * @param context the routing context
   * @param templateFileName the template file name to use
   * @param handler the handler that will be called with a result containing the buffer or a failure.
   */
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    delegate.render(context != null ? (io.vertx.ext.web.RoutingContext)context.getDelegate() : null, templateFileName, handler != null ? new Handler<AsyncResult<io.vertx.core.buffer.Buffer>>() {
      public void handle(AsyncResult<io.vertx.core.buffer.Buffer> ar) {
        if (ar.succeeded()) {
          handler.handle(io.vertx.core.Future.succeededFuture(InternalHelper.safeCreate(ar.result(), io.vertx.groovy.core.buffer.Buffer.class)));
        } else {
          handler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  /**
   * Returns true if the template engine caches template files. If false, then template files are freshly loaded each
   * time they are used.
   * @return True if template files are cached; otherwise, false.
   */
  public boolean isCachingEnabled() {
    def ret = delegate.isCachingEnabled();
    return ret;
  }
}
