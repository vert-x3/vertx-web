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

import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.web.FileUpload;

/**
 * {@link FileUpload} implementation for a non-multipart request body that was streamed to a temporary file
 * by the {@link io.vertx.ext.web.handler.BodyHandler}.
 */
public class BodyFileUploadImpl implements FileUpload {

  private final FileSystem fs;
  private final String uploadedFileName;
  private final String contentType;
  private final long size;

  public BodyFileUploadImpl(FileSystem fs, String uploadedFileName, String contentType, long size) {
    this.fs = fs;
    this.uploadedFileName = uploadedFileName;
    this.contentType = contentType;
    this.size = size;
  }

  @Override
  public String name() {
    return "body";
  }

  @Override
  public String uploadedFileName() {
    return uploadedFileName;
  }

  @Override
  public String fileName() {
    return null;
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public String contentType() {
    return contentType;
  }

  @Override
  public String contentTransferEncoding() {
    return "binary";
  }

  @Override
  public String charSet() {
    return null;
  }

  @Override
  public boolean cancel() {
    return false;
  }

  @Override
  public Future<Void> delete() {
    return fs.delete(uploadedFileName);
  }
}
