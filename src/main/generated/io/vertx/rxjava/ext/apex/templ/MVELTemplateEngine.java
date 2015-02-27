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

package io.vertx.rxjava.ext.apex.templ;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;

/**
 * A template engine that uses the Handlebars library.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 *
 * NOTE: This class has been automatically generated from the original non RX-ified interface using Vert.x codegen.
 */

public class MVELTemplateEngine extends TemplateEngine {

  final io.vertx.ext.apex.templ.MVELTemplateEngine delegate;

  public MVELTemplateEngine(io.vertx.ext.apex.templ.MVELTemplateEngine delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  public static MVELTemplateEngine create() {
    MVELTemplateEngine ret= MVELTemplateEngine.newInstance(io.vertx.ext.apex.templ.MVELTemplateEngine.create());
    return ret;
  }

  /**
   * Set the extension for the engine
   *
   * @param extension  the extension
   * @return a reference to this for fluency
   */
  public MVELTemplateEngine setExtension(String extension) {
    MVELTemplateEngine ret= MVELTemplateEngine.newInstance(this.delegate.setExtension(extension));
    return ret;
  }

  /**
   * Set the max cache size for the engine
   *
   * @param maxCacheSize  the maxCacheSize
   * @return a reference to this for fluency
   */
  public MVELTemplateEngine setMaxCacheSize(int maxCacheSize) {
    MVELTemplateEngine ret= MVELTemplateEngine.newInstance(this.delegate.setMaxCacheSize(maxCacheSize));
    return ret;
  }


  public static MVELTemplateEngine newInstance(io.vertx.ext.apex.templ.MVELTemplateEngine arg) {
    return new MVELTemplateEngine(arg);
  }
}
