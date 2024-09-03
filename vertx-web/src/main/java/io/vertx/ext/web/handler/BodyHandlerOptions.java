/*
 * Copyright 2024 Red Hat, Inc.
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

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

/**
 * Options for the {@link io.vertx.ext.web.handler.BodyHandler}.
 */
@DataObject
@JsonGen(publicConverter = false)
public class BodyHandlerOptions {

  /**
   * Default max size for a request body in bytes = {@code 10485760}, i.e. 10 megabytes.
   */
  public static final long DEFAULT_BODY_LIMIT = 10 * 1024 * 1024;

  /**
   * Default value of whether file uploads should be handled.
   */
  public static final boolean DEFAULT_HANDLE_FILE_UPLOADS = true;

  /**
   * Default uploads directory on server for file uploads.
   */
  public static final String DEFAULT_UPLOADS_DIRECTORY = "file-uploads";

  /**
   * Default value of whether form attributes should be merged into request params.
   */
  public static final boolean DEFAULT_MERGE_FORM_ATTRIBUTES = true;

  /**
   * Default value of whether uploaded files should be removed after handling the request.
   */
  public static final boolean DEFAULT_DELETE_UPLOADED_FILES_ON_END = false;

  /**
   * Default value of whether to pre-allocate the body buffer size according to the content-length HTTP request header.
   */
  public static final boolean DEFAULT_PREALLOCATE_BODY_BUFFER = false;

  private long bodyLimit;
  private boolean handleFileUploads;
  private String uploadsDirectory;
  private boolean mergeFormAttributes;
  private boolean deleteUploadedFilesOnEnd;
  private boolean preallocateBodyBuffer;

  /**
   * Default constructor.
   */
  public BodyHandlerOptions() {
    bodyLimit = DEFAULT_BODY_LIMIT;
    handleFileUploads = DEFAULT_HANDLE_FILE_UPLOADS;
    uploadsDirectory = DEFAULT_UPLOADS_DIRECTORY;
    mergeFormAttributes = DEFAULT_MERGE_FORM_ATTRIBUTES;
    deleteUploadedFilesOnEnd = DEFAULT_DELETE_UPLOADED_FILES_ON_END;
    preallocateBodyBuffer = DEFAULT_PREALLOCATE_BODY_BUFFER;
  }

  /**
   * Copy constructor.
   *
   * @param other the options to copy
   */
  public BodyHandlerOptions(BodyHandlerOptions other) {
    this();
    bodyLimit = other.bodyLimit;
    handleFileUploads = other.handleFileUploads;
    uploadsDirectory = other.uploadsDirectory;
    mergeFormAttributes = other.mergeFormAttributes;
    deleteUploadedFilesOnEnd = other.deleteUploadedFilesOnEnd;
    preallocateBodyBuffer = other.preallocateBodyBuffer;
  }

  /**
   * Constructor to create options from JSON.
   *
   * @param json the JSON
   */
  public BodyHandlerOptions(JsonObject json) {
    this();
    BodyHandlerOptionsConverter.fromJson(json, this);
  }

  public long getBodyLimit() {
    return bodyLimit;
  }

  /**
   * Set the maximum body size in bytes, {@code -1} means no limit.
   *
   * @param bodyLimit the max size in bytes
   * @return a reference to this, so the API can be used fluently
   */
  public BodyHandlerOptions setBodyLimit(long bodyLimit) {
    this.bodyLimit = bodyLimit;
    return this;
  }

  public boolean isHandleFileUploads() {
    return handleFileUploads;
  }

  /**
   * Set whether file uploads will be handled.
   *
   * @param handleFileUploads {@code true} if they should be handled
   * @return a reference to this, so the API can be used fluently
   */
  public BodyHandlerOptions setHandleFileUploads(boolean handleFileUploads) {
    this.handleFileUploads = handleFileUploads;
    return this;
  }

  public String getUploadsDirectory() {
    return uploadsDirectory;
  }

  /**
   * Set the uploads directory to use.
   *
   * @param uploadsDirectory the uploads directory
   * @return a reference to this, so the API can be used fluently
   */
  public BodyHandlerOptions setUploadsDirectory(String uploadsDirectory) {
    this.uploadsDirectory = uploadsDirectory;
    return this;
  }

  public boolean isMergeFormAttributes() {
    return mergeFormAttributes;
  }

  /**
   * Set whether form attributes will be added to the request parameters.
   *
   * @param mergeFormAttributes true if they should be merged
   * @return a reference to this, so the API can be used fluently
   */
  public BodyHandlerOptions setMergeFormAttributes(boolean mergeFormAttributes) {
    this.mergeFormAttributes = mergeFormAttributes;
    return this;
  }

  public boolean isDeleteUploadedFilesOnEnd() {
    return deleteUploadedFilesOnEnd;
  }

  /**
   * Set whether uploaded files should be removed after handling the request.
   *
   * @param deleteUploadedFilesOnEnd true if uploaded files should be removed after handling the request
   * @return a reference to this, so the API can be used fluently
   */
  public BodyHandlerOptions setDeleteUploadedFilesOnEnd(boolean deleteUploadedFilesOnEnd) {
    this.deleteUploadedFilesOnEnd = deleteUploadedFilesOnEnd;
    return this;
  }

  public boolean isPreallocateBodyBuffer() {
    return preallocateBodyBuffer;
  }

  /**
   * Pre-allocate the body buffer according to the value parsed from {@code content-length} header.
   * The buffer is capped at 64KB.
   * <p>
   * Otherwise, the body buffer is pre-allocated to 1KB, and resized dynamically as needed.
   *
   * @param preallocateBodyBuffer {@code true} to pre-allocate the body buffer, otherwise {code false}
   * @return a reference to this, so the API can be used fluently
   */
  public BodyHandlerOptions setPreallocateBodyBuffer(boolean preallocateBodyBuffer) {
    this.preallocateBodyBuffer = preallocateBodyBuffer;
    return this;
  }
}
