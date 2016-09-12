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
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A template engine that uses the Pebble library.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.templ.PebbleTemplateEngine original} non RX-ified interface using Vert.x codegen.
 */

public class PebbleTemplateEngine extends TemplateEngine {

  final io.vertx.ext.web.templ.PebbleTemplateEngine delegate;

  public PebbleTemplateEngine(io.vertx.ext.web.templ.PebbleTemplateEngine delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Create a template engine using defaults
   * @param vertx 
   * @return the engine
   */
  public static PebbleTemplateEngine create(Vertx vertx) { 
    PebbleTemplateEngine ret = PebbleTemplateEngine.newInstance(io.vertx.ext.web.templ.PebbleTemplateEngine.create((io.vertx.core.Vertx)vertx.getDelegate()));
    return ret;
  }

  /**
   * Set the extension for the engine
   * @param extension the extension
   * @return a reference to this for fluency
   */
  public PebbleTemplateEngine setExtension(String extension) { 
    PebbleTemplateEngine ret = PebbleTemplateEngine.newInstance(delegate.setExtension(extension));
    return ret;
  }

  /**
   * Set the max cache size for the engine
   * @param maxCacheSize the maxCacheSize
   * @return a reference to this for fluency
   */
  public PebbleTemplateEngine setMaxCacheSize(int maxCacheSize) { 
    PebbleTemplateEngine ret = PebbleTemplateEngine.newInstance(delegate.setMaxCacheSize(maxCacheSize));
    return ret;
  }


  public static PebbleTemplateEngine newInstance(io.vertx.ext.web.templ.PebbleTemplateEngine arg) {
    return arg != null ? new PebbleTemplateEngine(arg) : null;
  }
}
