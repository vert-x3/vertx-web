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

package io.vertx.ext.web;

import io.vertx.core.http.HttpServerRequest;

/**
 * Extends to access the routing context associated with the request.
 *
 * @author Florian Bütler
 */
public interface WebServerRequest extends HttpServerRequest {

  /**
   * @return the Vert.x context associated with this server request
   */
  public abstract RoutingContext routingContext();

}
