/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web;

/**
 * Represent a Locale as reported by the HTTP client.
 * <p>
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.impl.LocaleImpl;

@VertxGen
public interface Locale {

  static Locale create() {
    final java.util.Locale locale = java.util.Locale.getDefault();
    return new LocaleImpl(locale.getLanguage(), locale.getCountry(), locale.getVariant());
  }

  static Locale create(String language) {
    return new LocaleImpl(language, null, null);
  }

  static Locale create(String language, String country) {
    return new LocaleImpl(language, country, null);
  }

  static Locale create(String language, String country, String variant) {
    return new LocaleImpl(language, country, variant);
  }

  /**
   * Returns the language as reported by the HTTP client.
   *
   * @return language
   */
  String language();

  /**
   * Returns the country as reported by the HTTP client.
   *
   * @return variant
   */
  String country();

  /**
   * Returns the variant as reported by the HTTP client.
   *
   * @return variant
   */
  String variant();
}
