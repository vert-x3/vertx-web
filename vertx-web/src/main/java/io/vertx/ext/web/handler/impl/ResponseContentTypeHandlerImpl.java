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

package io.vertx.ext.web.handler.impl;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * @author Thomas Segismont
 */
public class ResponseContentTypeHandlerImpl implements ResponseContentTypeHandler {

  private final String disableFlag;

  public ResponseContentTypeHandlerImpl(String disableFlag) {
    this.disableFlag = disableFlag;
  }

  @Override
  public void handle(RoutingContext rc) {
    rc.addHeadersEndHandler(v -> {
      if (rc.get(disableFlag) != null) {
        return;
      }
      String acceptableContentType = rc.getAcceptableContentType();
      if (acceptableContentType == null) {
        return;
      }
      MultiMap headers = rc.response().headers();
      if (headers.contains(CONTENT_TYPE)) {
        return;
      }
      if (!"0".equals(headers.get(CONTENT_LENGTH))) {
        headers.add(CONTENT_TYPE, acceptableContentType);
      }
    });
    rc.next();
  }
}
