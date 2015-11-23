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

package io.vertx.ext.web.handler.impl;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.impl.FileUploadImpl;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BodyHandlerImpl implements BodyHandler {

  private static final String BODY_HANDLED = "__body-handled";

  private long bodyLimit = DEFAULT_BODY_LIMIT;
  private String uploadsDir;
  private boolean mergeFormAttributes = DEFAULT_MERGE_FORM_ATTRIBUTES;

  public BodyHandlerImpl() {
    setUploadsDirectory(DEFAULT_UPLOADS_DIRECTORY);
  }

  public BodyHandlerImpl(String uploadDirectory) {
    setUploadsDirectory(uploadDirectory);
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    // we need to keep state since we can be called again on reroute
    Boolean handled = context.get(BODY_HANDLED);
    if (handled == null || !handled) {
      BHandler handler = new BHandler(context);
      request.handler(handler);
      request.endHandler(v -> handler.end());
      context.put(BODY_HANDLED, true);
    } else {
      context.next();
    }
  }

  @Override
  public BodyHandler setBodyLimit(long bodyLimit) {
    this.bodyLimit = bodyLimit;
    return this;
  }

  @Override
  public BodyHandler setUploadsDirectory(String uploadsDirectory) {
    this.uploadsDir = uploadsDirectory;
    return this;
  }

  @Override
  public BodyHandler setMergeFormAttributes(boolean mergeFormAttributes) {
    this.mergeFormAttributes = mergeFormAttributes;
    return this;
  }

  private class BHandler implements Handler<Buffer> {

    RoutingContext context;
    Buffer body = Buffer.buffer();
    boolean failed;
    AtomicInteger uploadCount = new AtomicInteger();
    boolean ended;

    public BHandler(RoutingContext context) {
      this.context = context;
      Set<FileUpload> fileUploads = context.fileUploads();
      makeUploadDir(context.vertx().fileSystem());

      context.request().setExpectMultipart(true);
      context.request().exceptionHandler(context::fail);
      context.request().uploadHandler(upload -> {
        // We actually upload to a file with a generated filename
        uploadCount.incrementAndGet();
        String uploadedFileName = new File(uploadsDir, UUID.randomUUID().toString()).getPath();
        upload.streamToFileSystem(uploadedFileName);
        FileUploadImpl fileUpload = new FileUploadImpl(uploadedFileName, upload);
        fileUploads.add(fileUpload);
        upload.exceptionHandler(context::fail);
        upload.endHandler(v -> uploadEnded());
      });
    }

    private void makeUploadDir(FileSystem fileSystem) {
      if (!fileSystem.existsBlocking(uploadsDir)) {
        fileSystem.mkdirsBlocking(uploadsDir);
      }
    }

    @Override
    public void handle(Buffer buff) {
      if (failed) {
        return;
      }
      if (bodyLimit != -1 && (body.length() + buff.length()) > bodyLimit) {
        failed = true;
        context.fail(413);
      } else {
        body.appendBuffer(buff);
      }
    }

    void uploadEnded() {
      int count = uploadCount.decrementAndGet();
      if (count == 0) {
        doEnd();
      }
    }

    void end() {
      if (uploadCount.get() == 0) {
        doEnd();
      }
    }

    void doEnd() {
      if (failed || ended) {
        return;
      }
      ended = true;
      HttpServerRequest req = context.request();
      if (mergeFormAttributes && req.isExpectMultipart()) {
        req.params().addAll(req.formAttributes());
      }
      context.setBody(body);
      context.next();
    }
  }

}
