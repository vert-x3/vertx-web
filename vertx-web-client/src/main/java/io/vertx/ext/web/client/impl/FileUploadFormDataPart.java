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
package io.vertx.ext.web.client.impl;

import io.vertx.ext.web.client.FormDataPart;

public class FileUploadFormDataPart implements FormDataPart {
  private final String name;
  private final String filename;
  private final String mediaType;
  private final String pathname;
  private final boolean isText;

  public FileUploadFormDataPart(String name, String filename, String pathname, String mediaType, boolean isText) {
    this.name = name;
    this.filename = filename;
    this.pathname = pathname;
    this.mediaType = mediaType;
    this.isText = isText;
  }

  public String getName() {
    return name;
  }

  public String getFilename() {
    return filename;
  }

  public String getPathname() {
    return pathname;
  }

  public String getMediaType() {
    return mediaType;
  }

  public boolean isText() {
    return isText;
  }
}
