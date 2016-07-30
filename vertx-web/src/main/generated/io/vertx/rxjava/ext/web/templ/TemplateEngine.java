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

package io.vertx.rxjava.ext.web.templ;

import java.util.Map;
import rx.Observable;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A template engine uses a specific template and the data in a routing context to render a resource into a buffer.
 * <p>
 * Concrete implementations exist for several well-known template engines.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.templ.TemplateEngine original} non RX-ified interface using Vert.x codegen.
 */

public class TemplateEngine {

  final io.vertx.ext.web.templ.TemplateEngine delegate;

  public TemplateEngine(io.vertx.ext.web.templ.TemplateEngine delegate) {
    this.delegate = delegate;
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
    delegate.render((io.vertx.ext.web.RoutingContext)context.getDelegate(), templateFileName, new Handler<AsyncResult<io.vertx.core.buffer.Buffer>>() {
      public void handle(AsyncResult<io.vertx.core.buffer.Buffer> ar) {
        if (ar.succeeded()) {
          handler.handle(io.vertx.core.Future.succeededFuture(Buffer.newInstance(ar.result())));
        } else {
          handler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    });
  }

  /**
   * Render
   * @param context the routing context
   * @param templateFileName the template file name to use
   * @return 
   */
  public Observable<Buffer> renderObservable(RoutingContext context, String templateFileName) { 
    io.vertx.rx.java.ObservableFuture<Buffer> handler = io.vertx.rx.java.RxHelper.observableFuture();
    render(context, templateFileName, handler.toHandler());
    return handler;
  }

  /**
   * Returns true if the template engine caches template files. If false, then template files are freshly loaded each
   * time they are used.
   * @return True if template files are cached; otherwise, false.
   */
  public boolean isCachingEnabled() { 
    boolean ret = delegate.isCachingEnabled();
    return ret;
  }


  public static TemplateEngine newInstance(io.vertx.ext.web.templ.TemplateEngine arg) {
    return arg != null ? new TemplateEngine(arg) : null;
  }
}
