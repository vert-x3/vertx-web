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

package io.vertx.groovy.ext.web.handler;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.core.Handler
/**
 * A handler which gathers the entire request body and sets it on the .
 * <p>
 * It also handles HTTP file uploads and can be used to limit body sizes.
*/
@CompileStatic
public class BodyHandler implements Handler<RoutingContext> {
  private final def io.vertx.ext.web.handler.BodyHandler delegate;
  public BodyHandler(Object delegate) {
    this.delegate = (io.vertx.ext.web.handler.BodyHandler) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void handle(RoutingContext arg0) {
    ((io.vertx.core.Handler) this.delegate).handle((io.vertx.ext.web.RoutingContext)arg0.getDelegate());
  }
  /**
   * Create a body handler with defaults
   * @return the body handler
   */
  public static BodyHandler create() {
    def ret= InternalHelper.safeCreate(io.vertx.ext.web.handler.BodyHandler.create(), io.vertx.groovy.ext.web.handler.BodyHandler.class);
    return ret;
  }
  /**
   * Create a body handler and use the given upload directory.
   * @param uploadDirectory the uploads directory
   * @return the body handler
   */
  public static BodyHandler create(String uploadDirectory) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.web.handler.BodyHandler.create(uploadDirectory), io.vertx.groovy.ext.web.handler.BodyHandler.class);
    return ret;
  }
  /**
   * Set the maximum body size -1 means unlimited
   * @param bodyLimit the max size
   * @return reference to this for fluency
   */
  public BodyHandler setBodyLimit(long bodyLimit) {
    this.delegate.setBodyLimit(bodyLimit);
    return this;
  }
  /**
   * Set the uploads directory to use
   * @param uploadsDirectory the uploads directory
   * @return reference to this for fluency
   */
  public BodyHandler setUploadsDirectory(String uploadsDirectory) {
    this.delegate.setUploadsDirectory(uploadsDirectory);
    return this;
  }
  /**
   * Set whether form attributes will be added to the request parameters
   * @param mergeFormAttributes true if they should be merged
   * @return reference to this for fluency
   */
  public BodyHandler setMergeFormAttributes(boolean mergeFormAttributes) {
    this.delegate.setMergeFormAttributes(mergeFormAttributes);
    return this;
  }
  /**
   * Set whether uploaded files should be removed after handling the request
   * @param deleteUploadedFilesOnEnd true if uploaded files should be removed after handling the request
   * @return reference to this for fluency
   */
  public BodyHandler setDeleteUploadedFilesOnEnd(boolean deleteUploadedFilesOnEnd) {
    this.delegate.setDeleteUploadedFilesOnEnd(deleteUploadedFilesOnEnd);
    return this;
  }
}
