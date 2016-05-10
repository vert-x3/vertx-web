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
import org.thymeleaf.templatemode.TemplateMode;

/**
 * A template engine that uses the Thymeleaf library.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.web.templ.Thymeleaf3TemplateEngine original} non RX-ified interface using Vert.x codegen.
 */

public class Thymeleaf3TemplateEngine extends TemplateEngine {

  final io.vertx.ext.web.templ.Thymeleaf3TemplateEngine delegate;

  public Thymeleaf3TemplateEngine(io.vertx.ext.web.templ.Thymeleaf3TemplateEngine delegate) {
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
  public static Thymeleaf3TemplateEngine create() { 
    Thymeleaf3TemplateEngine ret= Thymeleaf3TemplateEngine.newInstance(io.vertx.ext.web.templ.Thymeleaf3TemplateEngine.create());
    return ret;
  }

  /**
   * Set the mode for the engine
   * @param mode the mode
   * @return a reference to this for fluency
   */
  public Thymeleaf3TemplateEngine setMode(TemplateMode mode) { 
    Thymeleaf3TemplateEngine ret= Thymeleaf3TemplateEngine.newInstance(this.delegate.setMode(mode));
    return ret;
  }


  public static Thymeleaf3TemplateEngine newInstance(io.vertx.ext.web.templ.Thymeleaf3TemplateEngine arg) {
    return arg != null ? new Thymeleaf3TemplateEngine(arg) : null;
  }
}
