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

package io.vertx.ext.apex.handler.impl;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.apex.impl.FileUploadImpl;
import io.vertx.ext.apex.handler.BodyHandler;
import io.vertx.ext.apex.FileUpload;
import io.vertx.ext.apex.RoutingContext;

import java.io.File;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BodyHandlerImpl implements BodyHandler {

  private long bodyLimit = DEFAULT_BODY_LIMIT;
  private File uploadsDir;
  private boolean mergeFormAttributes = DEFAULT_MERGE_FORM_ATTRIBUTES;

  public BodyHandlerImpl() {
    setUploadsDirectory(DEFAULT_UPLOADS_DIRECTORY);
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (request.method() == HttpMethod.GET || request.method() == HttpMethod.HEAD) {
      // Don't have bodies
      context.next();
    } else {
      BHandler handler = new BHandler(context);
      request.handler(handler);
      request.endHandler(v -> handler.end());
    }
  }

  @Override
  public BodyHandler setBodyLimit(long bodyLimit) {
    this.bodyLimit = bodyLimit;
    return this;
  }

  @Override
  public BodyHandler setUploadsDirectory(String uploadsDirectory) {
    this.uploadsDir = new File(uploadsDirectory);
    if (!uploadsDir.exists()) {
      uploadsDir.mkdirs();
    }
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

    private BHandler(RoutingContext context) {
      this.context = context;
      Set<FileUpload> fileUploads = context.fileUploads();
      context.request().setExpectMultipart(true);
      context.request().exceptionHandler(context::fail);
      context.request().uploadHandler(upload -> {
        // We actually upload to a file with a generated filename
        String uploadedFileName = new File(uploadsDir, UUID.randomUUID().toString()).getPath();
        upload.streamToFileSystem(uploadedFileName);
        FileUploadImpl fileUpload = new FileUploadImpl(uploadedFileName, upload);
        fileUploads.add(fileUpload);
        upload.exceptionHandler(context::fail);
      });
    }

    @Override
    public void handle(Buffer buff) {
      if (failed) {
        return;
      }
      if (bodyLimit != -1 && body.length() > bodyLimit) {
        failed = true;
        context.fail(413);
      } else {
        body.appendBuffer(buff);
      }
    }

    void end() {
      if (failed) {
        return;
      }
      HttpServerRequest req = context.request();
      if (mergeFormAttributes && req.isExpectMultipart()) {
        req.params().addAll(req.formAttributes());
      }
      context.setBody(body);
      context.next();
    }
  }

}
