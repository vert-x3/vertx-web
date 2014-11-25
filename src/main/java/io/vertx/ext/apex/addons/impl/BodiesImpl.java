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

package io.vertx.ext.apex.addons.impl;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.apex.addons.Bodies;
import io.vertx.ext.apex.addons.FileUpload;
import io.vertx.ext.apex.core.RoutingContext;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BodiesImpl implements Bodies {

  private final long bodyLimit;
  private final File uploadsDir;

  public BodiesImpl() {
    this(DEFAULT_BODY_LIMIT, DEFAULT_UPLOADS_DIRECTORY);
  }

  public BodiesImpl(long bodyLimit) {
    this(bodyLimit, DEFAULT_UPLOADS_DIRECTORY);
  }

  public BodiesImpl(String uploadsDirectory) {
    this(DEFAULT_BODY_LIMIT, uploadsDirectory);
  }

  public BodiesImpl(long bodyLimit, String uploadsDirectory) {
    this.bodyLimit = bodyLimit;
    uploadsDir = new File(uploadsDirectory);
    if (!uploadsDir.exists()) {
      uploadsDir.mkdirs();
    }
  }

  @Override
  public void handle(RoutingContext context) {
    HttpServerRequest request = context.request();
    if (request.method() == HttpMethod.GET || request.method() == HttpMethod.HEAD) {
      // Don't have bodies
      context.next();
    } else {
      BodyHandler handler = new BodyHandler(context);
      request.handler(handler);
      request.endHandler(v -> handler.end());
    }
  }

  private class BodyHandler implements Handler<Buffer> {

    final RoutingContext context;
    final Buffer body = Buffer.buffer();
    Set<FileUpload> fileUploads;
    boolean failed;

    private BodyHandler(RoutingContext context) {
      this.context = context;
      context.request().setExpectMultipart(true);
      context.request().exceptionHandler(context::fail);
      context.request().uploadHandler(upload -> {
        // We actually upload to a file with a generated filename
        String uploadedFileName = new File(uploadsDir, UUID.randomUUID().toString()).getPath();
        upload.streamToFileSystem(uploadedFileName);
        if (fileUploads == null) {
          fileUploads = new HashSet<>();
        }
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
      context.put(Bodies.FILE_UPLOADS_ENTRY_NAME, fileUploads);
      context.put(Bodies.BODY_ENTRY_NAME, body);
      context.next();
    }
  }

}
