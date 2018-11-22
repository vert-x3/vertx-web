/*
 * Copyright 2018 Red Hat, Inc.
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
package io.vertx.ext.web.client.impl;

public enum ClientPhase {

  /**
   * The {@link io.vertx.core.http.HttpClientRequest} has not yet been created, the {@link HttpContext#request()} can be fully modified.
   */
  PREPARE_REQUEST,

  /**
   * The {@link io.vertx.core.http.HttpClientRequest} has been created but not yet sent, the HTTP method, URI or request parameters
   * cannot be modified anymore.
   */
  SEND_REQUEST,

  /**
   * The {@link io.vertx.core.http.HttpClientResponse} has been received and the {@link HttpContext#response()} will be created.
   */
  RECEIVE_RESPONSE,

  /**
   * The {@link HttpContext#response()} has been created and will be dispatched.
   */
  DISPATCH_RESPONSE,

  /**
   * It failed.
   */
  FAILURE

}
