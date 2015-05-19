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

import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.ext.web.FileUpload;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class FileUploadImpl implements FileUpload {

  private final String uploadedFileName;
  private final HttpServerFileUpload upload;

  public FileUploadImpl(String uploadedFileName, HttpServerFileUpload upload) {
    this.uploadedFileName = uploadedFileName;
    this.upload = upload;
  }

  @Override
  public String name() {
    return upload.name();
  }

  @Override
  public String uploadedFileName() {
    return uploadedFileName;
  }

  @Override
  public String fileName() {
    return upload.filename();
  }

  @Override
  public long size() {
    return upload.size();
  }

  @Override
  public String contentType() {
    return upload.contentType();
  }

  @Override
  public String contentTransferEncoding() {
    return upload.contentTransferEncoding();
  }

  @Override
  public String charSet() {
    return upload.charset();
  }

}
