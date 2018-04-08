/*
 * Copyright (c) 2011-2018 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.multipart.impl;

import io.vertx.ext.web.multipart.FormDataPart;

public class FormDataPartImpl implements FormDataPart {

  private final String name;
  private final String value;
  private final String filename;
  private final String mediaType;
  private final String pathname;
  private final Boolean text;

  public FormDataPartImpl(String name, String value) {
    if (name == null) {
      throw new NullPointerException();
    }
    if (value == null) {
      throw new NullPointerException();
    }
    this.name = name;
    this.value = value;
    this.filename = null;
    this.pathname = null;
    this.mediaType = null;
    this.text = null;
  }

  public FormDataPartImpl(String name, String filename, String pathname, String mediaType, boolean text) {
    if (name == null) {
      throw new NullPointerException();
    }
    if (filename == null) {
      throw new NullPointerException();
    }
    if (pathname == null) {
      throw new NullPointerException();
    }
    if (mediaType == null) {
      throw new NullPointerException();
    }
    this.name = name;
    this.value = null;
    this.filename = filename;
    this.pathname = pathname;
    this.mediaType = mediaType;
    this.text = text;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean isAttribute() {
    return value != null;
  }

  @Override
  public boolean isFileUpload() {
    return value == null;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public String filename() {
    return filename;
  }

  @Override
  public String pathname() {
    return pathname;
  }

  @Override
  public String mediaType() {
    return mediaType;
  }

  @Override
  public Boolean isText() {
    return text;
  }
}
