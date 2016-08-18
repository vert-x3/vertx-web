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

package io.vertx.groovy.ext.web;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
/**
 * Represents a file-upload from an HTTP multipart form submission.
 * <p>
*/
@CompileStatic
public class FileUpload {
  private final def io.vertx.ext.web.FileUpload delegate;
  public FileUpload(Object delegate) {
    this.delegate = (io.vertx.ext.web.FileUpload) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * @return the name of the upload as provided in the form submission
   */
  public String name() {
    def ret = delegate.name();
    return ret;
  }
  /**
   * @return the actual temporary file name on the server where the file was uploaded to.
   */
  public String uploadedFileName() {
    def ret = delegate.uploadedFileName();
    return ret;
  }
  /**
   * @return the file name of the upload as provided in the form submission
   */
  public String fileName() {
    def ret = delegate.fileName();
    return ret;
  }
  /**
   * @return the size of the upload, in bytes
   */
  public long size() {
    def ret = delegate.size();
    return ret;
  }
  /**
   * @return the content type (MIME type) of the upload
   */
  public String contentType() {
    def ret = delegate.contentType();
    return ret;
  }
  /**
   * @return the content transfer encoding of the upload - this describes how the upload was encoded in the form submission.
   */
  public String contentTransferEncoding() {
    def ret = delegate.contentTransferEncoding();
    return ret;
  }
  /**
   * @return the charset of the upload
   */
  public String charSet() {
    def ret = delegate.charSet();
    return ret;
  }
}
