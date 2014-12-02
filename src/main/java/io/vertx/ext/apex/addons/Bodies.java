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

package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.addons.impl.BodiesHelper;
import io.vertx.ext.apex.addons.impl.BodiesImpl;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Bodies extends Handler<RoutingContext> {

  static final long DEFAULT_BODY_LIMIT = -1;
  static final String DEFAULT_UPLOADS_DIRECTORY = "file-uploads";

  static final String BODY_ENTRY_NAME = "__apex_body";
  static final String FILE_UPLOADS_ENTRY_NAME = "__apex_file_uploads";

  static Bodies bodies() {
    return new BodiesImpl();
  }

  static Bodies bodies(long bodyLimit) {
    return new BodiesImpl(bodyLimit);
  }

  static Bodies bodies(String uploadsDirectory) {
    return new BodiesImpl(uploadsDirectory);
  }

  static Bodies bodies(long bodyLimit, String uploadsDirectory) {
    return new BodiesImpl(bodyLimit, uploadsDirectory);
  }

  static String getBodyAsString() {
    return BodiesHelper.getBodyAsString();
  }

  static String getBodyAsString(String encoding) {
    return BodiesHelper.getBodyAsString(encoding);
  }

  static JsonObject getBodyAsJson() {
    return BodiesHelper.getBodyAsJson();
  }

  static Buffer getBody() {
    return BodiesHelper.getBody();
  }

  static Set<FileUpload> fileUploads() {
    return BodiesHelper.getFileUploads();
  }

  @Override
  void handle(RoutingContext event);

}
