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

package io.vertx.groovy.ext.apex.addons;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@CompileStatic
public class FileUpload {
  final def io.vertx.ext.apex.addons.FileUpload delegate;
  public FileUpload(io.vertx.ext.apex.addons.FileUpload delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public String name() {
    def ret = this.delegate.name();
    return ret;
  }
  public String uploadedFileName() {
    def ret = this.delegate.uploadedFileName();
    return ret;
  }
  public String fileName() {
    def ret = this.delegate.fileName();
    return ret;
  }
  public long size() {
    def ret = this.delegate.size();
    return ret;
  }
  public String contentType() {
    def ret = this.delegate.contentType();
    return ret;
  }
  public String contentTransferEncoding() {
    def ret = this.delegate.contentTransferEncoding();
    return ret;
  }
  public String charSet() {
    def ret = this.delegate.charSet();
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.addons.FileUpload, FileUpload> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.addons.FileUpload arg -> new FileUpload(arg);
  };
}
