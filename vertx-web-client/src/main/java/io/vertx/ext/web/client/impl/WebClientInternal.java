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
package io.vertx.ext.web.client.impl;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.Handler;
import io.vertx.ext.web.client.WebClient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface WebClientInternal extends WebClient {

  /**
   * Add interceptor.
   * <p>
   * This API is internal and is subject to change or to be replaced by another one.
   *
   * @param interceptor the interceptor to add
   */
  @GenIgnore
  void addInterceptor(Handler<HttpContext> interceptor);

}
