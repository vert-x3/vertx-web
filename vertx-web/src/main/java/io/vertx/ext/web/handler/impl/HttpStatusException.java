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

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpStatusException extends RuntimeException {

  private final int statusCode;
  private final String payload;

  public HttpStatusException(int statusCode) {
    this(statusCode, null, null);
  }

  public HttpStatusException(int statusCode, Throwable cause) {
      this(statusCode, null, cause);
    }

  public HttpStatusException(int statusCode, String payload) {
    this(statusCode, payload, null);
  }

  public HttpStatusException(int statusCode, String payload, Throwable cause) {
    super(HttpResponseStatus.valueOf(statusCode).reasonPhrase(), cause, false, false);
    this.statusCode = statusCode;
    this.payload = payload;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getPayload() {
    return payload;
  }
}
