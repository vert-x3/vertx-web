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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.addons.Bodies;
import io.vertx.ext.apex.addons.FileUpload;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class BodiesHelper {

  public static String getBodyAsString(String encoding) {
    return body().toString(encoding);
  }

  public static String getBodyAsString() {
    return body().toString();
  }

  public static Buffer getBody() {
    return body();
  }

  public static JsonObject getBodyAsJson() {
    return new JsonObject(body().toString());
  }

  private static Buffer body() {
    return RoutingContext.getContext().get(Bodies.BODY_ENTRY_NAME);
  }

  public static Set<FileUpload> getFileUploads() {
    return RoutingContext.getContext().get(Bodies.FILE_UPLOADS_ENTRY_NAME);
  }
}
