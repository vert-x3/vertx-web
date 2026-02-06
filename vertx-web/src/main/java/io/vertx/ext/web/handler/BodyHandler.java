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

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.BodyHandlerImpl;

import java.util.List;

/**
 * A handler which gathers the entire request body and sets it on the {@link RoutingContext}.
 * <p>
 * It also handles HTTP file uploads and can be used to limit body sizes.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface BodyHandler extends Handler<RoutingContext> {

  /**
   * Default max size for a request body in bytes = {@code 10485760}, i.e. 10 megabytes
   */
  long DEFAULT_BODY_LIMIT = 10 * 1024 * 1024;

  /**
   * Default uploads directory on server for file uploads
   */
  String DEFAULT_UPLOADS_DIRECTORY = "file-uploads";

  /**
   * Default value of whether form attributes should be merged into request params
   */
  boolean DEFAULT_MERGE_FORM_ATTRIBUTES = true;

  /**
   * Default value of whether uploaded files should be removed after handling the request
   */
  boolean DEFAULT_DELETE_UPLOADED_FILES_ON_END = false;

  /**
   * Default value of whether to pre-allocate the body buffer size according to the content-length HTTP request header
   */
  boolean DEFAULT_PREALLOCATE_BODY_BUFFER = false;

  /**
   * Default value of whether to stream large request bodies to temporary files
   */
  boolean DEFAULT_STREAM_TO_FILE = false;

  /**
   * Default size threshold in bytes for streaming to file = {@code 1048576}, i.e. 1 megabyte
   */
  long DEFAULT_STREAM_THRESHOLD = 1024 * 1024;

  /**
   * Default value of whether to stream all binary (non-text, non-form) content types to file
   */
  boolean DEFAULT_STREAM_ALL_BINARY = false;

  /**
   * Create a body handler with defaults.
   *
   * @return the body handler
   */
  static BodyHandler create() {
    return new BodyHandlerImpl();
  }

  /**
   * Create a body handler setting if it should handle file uploads.
   *
   * @param handleFileUploads true if files upload should be handled
   * @return the body handler
   */
  static BodyHandler create(boolean handleFileUploads) {
    return new BodyHandlerImpl(handleFileUploads);
  }

  /**
   * Create a body handler and use the given upload directory.
   *
   * @param uploadDirectory  the uploads directory
   * @return the body handler
   */
  static BodyHandler create(String uploadDirectory) {
    return new BodyHandlerImpl(uploadDirectory);
  }

  /**
   * Set whether file uploads will be handled.
   *
   * @param handleFileUploads  true if they should be handled
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setHandleFileUploads(boolean handleFileUploads);

  /**
   * Set the maximum body size in bytes, {@code -1} means no limit.
   *
   * @param bodyLimit  the max size in bytes
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setBodyLimit(long bodyLimit);

  /**
   * Set the uploads directory to use.
   *
   * @param uploadsDirectory  the uploads directory
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setUploadsDirectory(String uploadsDirectory);

  /**
   * Set whether form attributes will be added to the request parameters.
   *
   * @param mergeFormAttributes  true if they should be merged
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setMergeFormAttributes(boolean mergeFormAttributes);

  /**
   * Set whether uploaded files should be removed after handling the request.
   *
   * @param deleteUploadedFilesOnEnd  true if uploaded files should be removed after handling the request
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setDeleteUploadedFilesOnEnd(boolean deleteUploadedFilesOnEnd);

  /**
   * Pre-allocate the body buffer according to the value parsed from content-length header.
   * The buffer is capped at 64KB
   * @param isPreallocateBodyBuffer {@code true} if body buffer is pre-allocated according to the size
   *                               read from content-length Header.
   *                               {code false} if body buffer is pre-allocated to 1KB, and is resized dynamically
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setPreallocateBodyBuffer(boolean isPreallocateBodyBuffer);

  /**
   * Enable streaming large request bodies to temporary files instead of holding them in memory.
   * When enabled, non-multipart request bodies that match the configured content types and exceed the
   * stream threshold will be written to a temporary file in the uploads directory.
   *
   * @param streamToFile true to enable file streaming (default: false)
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setStreamToFile(boolean streamToFile);

  /**
   * Set the size threshold for streaming to file. Bodies larger than this value will be
   * streamed to disk when file streaming is enabled and the content type matches.
   *
   * @param bytes threshold in bytes (default: 1MB)
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setStreamThreshold(long bytes);

  /**
   * Set the content types that should be streamed to file. Supports wildcard patterns
   * such as {@code "image/*"} or {@code "video/*"}.
   *
   * @param contentTypes list of MIME type patterns
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setStreamContentTypes(List<String> contentTypes);

  /**
   * Enable streaming for all non-text, non-form content types. When enabled, any content type
   * that is not {@code text/*}, {@code multipart/form-data}, or
   * {@code application/x-www-form-urlencoded} will be streamed to file if it exceeds the threshold.
   *
   * @param streamAllBinary true to stream all binary content types (default: false)
   * @return reference to this for fluency
   */
  @Fluent
  BodyHandler setStreamAllBinary(boolean streamAllBinary);

}
