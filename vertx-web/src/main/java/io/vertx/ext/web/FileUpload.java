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

import io.vertx.codegen.annotations.VertxGen;

/**
 * Represents a file-upload from an HTTP multipart form submission.
 * <p>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface FileUpload {

  /**
   * @return the name of the upload as provided in the form submission
   */
  String name();

  /**
   * @return the actual temporary file name on the server where the file was uploaded to.
   */
  String uploadedFileName();

  /**
   * @return the file name of the upload as provided in the form submission
   */
  String fileName();

  /**
   * @return the size of the upload, in bytes
   */
  long size();

  /**
   * @return the content type (MIME type) of the upload
   */
  String contentType();

  /**
   * @return the content transfer encoding of the upload - this describes how the upload was encoded in the form submission.
   */
  String contentTransferEncoding();

  /**
   * @return the charset of the upload
   */
  String charSet();


}
