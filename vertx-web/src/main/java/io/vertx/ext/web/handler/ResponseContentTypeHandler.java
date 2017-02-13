/*
 * Copyright 2017 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.ResponseContentTypeHandlerImpl;

/**
 * A handler which sets the response content type automatically according to the best {@code Accept} header match.
 *
 * The header is set only if:
 * <ul>
 * <li>no object is stored in the routing context under the name {@link #DEFAULT_DISABLE_FLAG}</li>
 * <li>a match is found</li>
 * <li>the header is not present already</li>
 * <li>content length header is absent or set to something different than zero</li>
 * </ul>
 *
 * @author Thomas Segismont
 * @see RoutingContext#getAcceptableContentType()
 */
@VertxGen
public interface ResponseContentTypeHandler extends Handler<RoutingContext> {

  String DEFAULT_DISABLE_FLAG = "__vertx.autoContenType.disable";

  /**
   * Create a response content type handler.
   *
   * @return the response content type handler
   */
  static ResponseContentTypeHandler create() {
    return new ResponseContentTypeHandlerImpl(DEFAULT_DISABLE_FLAG);
  }

  /**
   * Create a response content type handler with a custom disable flag.
   *
   * @return the response content type handler
   */
  static ResponseContentTypeHandler create(String disableFlag) {
    return new ResponseContentTypeHandlerImpl(disableFlag);
  }
}
