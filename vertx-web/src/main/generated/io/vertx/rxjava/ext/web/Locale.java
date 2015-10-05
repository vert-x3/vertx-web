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

package io.vertx.rxjava.ext.web;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;


public class Locale {

  final io.vertx.ext.web.Locale delegate;

  public Locale(io.vertx.ext.web.Locale delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public static Locale create() { 
    Locale ret= Locale.newInstance(io.vertx.ext.web.Locale.create());
    return ret;
  }

  public static Locale create(String language) { 
    Locale ret= Locale.newInstance(io.vertx.ext.web.Locale.create(language));
    return ret;
  }

  public static Locale create(String language, String country) { 
    Locale ret= Locale.newInstance(io.vertx.ext.web.Locale.create(language, country));
    return ret;
  }

  public static Locale create(String language, String country, String variant) { 
    Locale ret= Locale.newInstance(io.vertx.ext.web.Locale.create(language, country, variant));
    return ret;
  }

  /**
   * Returns the language as reported by the HTTP client.
   * @return language
   */
  public String language() { 
    String ret = this.delegate.language();
    return ret;
  }

  /**
   * Returns the country as reported by the HTTP client.
   * @return variant
   */
  public String country() { 
    String ret = this.delegate.country();
    return ret;
  }

  /**
   * Returns the variant as reported by the HTTP client.
   * @return variant
   */
  public String variant() { 
    String ret = this.delegate.variant();
    return ret;
  }


  public static Locale newInstance(io.vertx.ext.web.Locale arg) {
    return arg != null ? new Locale(arg) : null;
  }
}
