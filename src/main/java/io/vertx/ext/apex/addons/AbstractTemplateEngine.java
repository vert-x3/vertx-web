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

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.apex.core.RoutingContext;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class AbstractTemplateEngine implements TemplateEngine {

  @Override
  public void renderResponse(RoutingContext context, String templateFileName, String contentType) {
    render(context, templateFileName, res -> {
      if (res.succeeded()) {
        context.response().putHeader(HttpHeaders.CONTENT_TYPE, contentType).end(res.result());
      } else {
        context.fail(res.cause());
      }
    });
  }
}
