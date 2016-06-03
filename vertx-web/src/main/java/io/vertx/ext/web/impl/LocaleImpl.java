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
package io.vertx.ext.web.impl;

import io.vertx.ext.web.Locale;

import java.util.Objects;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class LocaleImpl implements Locale {

  private final String language;
  private final String country;
  private final String variant;

  public LocaleImpl(String language, String country, String variant) {
    this.language = Objects.requireNonNull(language).toLowerCase();
    this.country = country != null ? country.toUpperCase() : "";
    this.variant = variant != null ? variant.toUpperCase() : "";
  }

  @Override
  public String language() {
    return language;
  }

  @Override
  public String country() {
    return country;
  }

  @Override
  public String variant() {
    return variant;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(language);
    if (country.length() > 0) {
      sb.append("-").append(country);
    }
    if (variant.length() > 0) {
      sb.append("-").append(variant);
    }

    return sb.toString();
  }
}
