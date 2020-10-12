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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface WebClientInternal extends WebClient {

  <T> HttpContext<T> createContext(Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * Add interceptor in the chain.
   * <p/>
   * The interceptor can maintain per request state with {@link HttpContext#get(String)}/{@link HttpContext#set(String, Object)}.
   * <p/>
   * A request/response can be processed several times (in case of retry) and thus they should use the per request state
   * to ensure an operation is not done twice.
   * <p/>
   * This API is internal.
   *
   * @param interceptor the interceptor to add, must not be null
   * @return a reference to this, so the API can be used fluently
   */
  WebClientInternal addInterceptor(Handler<HttpContext<?>> interceptor);

}
