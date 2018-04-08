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
import io.vertx.ext.web.multipart.MultipartForm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultipartFormImpl implements MultipartForm {

  private final List<FormDataPart> parts = new ArrayList<>();

  @Override
  public MultipartForm attribute(String name, String value) {
    parts.add(new FormDataPartImpl(name, value));
    return this;
  }

  @Override
  public MultipartForm textFileUpload(String name, String filename, String pathname, String mediaType) {
    parts.add(new FormDataPartImpl(name, filename, pathname, mediaType, true));
    return this;
  }

  @Override
  public MultipartForm binaryFileUpload(String name, String filename, String pathname, String mediaType) {
    parts.add(new FormDataPartImpl(name, filename, pathname, mediaType, false));
    return this;
  }

  @Override
  public Iterator<FormDataPart> iterator() {
    return parts.iterator();
  }
}
