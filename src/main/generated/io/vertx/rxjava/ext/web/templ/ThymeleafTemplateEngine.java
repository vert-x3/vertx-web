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
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;

/**
 * A template engine that uses the Thymeleaf library.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.templ.ThymeleafTemplateEngine original} non RX-ified interface using Vert.x codegen.
 */

public class ThymeleafTemplateEngine extends TemplateEngine {

  final io.vertx.ext.web.templ.ThymeleafTemplateEngine delegate;

  public ThymeleafTemplateEngine(io.vertx.ext.web.templ.ThymeleafTemplateEngine delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Create a template engine using defaults
   * @return the engine
   */
  public static ThymeleafTemplateEngine create() { 
    ThymeleafTemplateEngine ret= ThymeleafTemplateEngine.newInstance(io.vertx.ext.web.templ.ThymeleafTemplateEngine.create());
    return ret;
  }

  /**
   * Set the mode for the engine
   * @param mode the mode
   * @return a reference to this for fluency
   */
  public ThymeleafTemplateEngine setMode(String mode) { 
    ThymeleafTemplateEngine ret= ThymeleafTemplateEngine.newInstance(this.delegate.setMode(mode));
    return ret;
  }


  public static ThymeleafTemplateEngine newInstance(io.vertx.ext.web.templ.ThymeleafTemplateEngine arg) {
    return new ThymeleafTemplateEngine(arg);
  }
}
