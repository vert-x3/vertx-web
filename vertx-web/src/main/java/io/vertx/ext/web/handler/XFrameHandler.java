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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * The X-Frame-Options HTTP response header can be used to indicate whether or not a browser should be allowed to render
 * a page in a {@code <frame>}, {@code <iframe>}, {@code <embed>} or {@code <object>}. Sites can use this to avoid
 * click-jacking attacks, by ensuring that their content is not embedded into other sites.
 *
 * The added security is provided only if the user accessing the document is using a browser that supports
 * {@code X-Frame-Options}.
 *
 * @author Paulo Lopes
 */
@VertxGen
public interface XFrameHandler extends Handler<RoutingContext> {

  /**
   * The page cannot be displayed in a frame, regardless of the site attempting to do so.
   */
  String DENY = "DENY";

  /**
   * The page can only be displayed in a frame on the same origin as the page itself. The spec leaves it up to browser
   * vendors to decide whether this option applies to the top level, the parent, or the whole chain, although it is
   * argued that the option is not very useful unless all ancestors are also in the same origin.
   */
  String SAMEORIGIN = "SAMEORIGIN";

  /**
   * Creates a new handler that will add the {@code X-FRAME-OPTIONS} header to the current response.
   * @param action a string value either {@code DENY} or {@code SAMEORIGIN}.
   * @return the handler
   */
  static XFrameHandler create(String action) {
    if (action == null) {
      throw new IllegalArgumentException("action cannot be null");
    }

    final String value = action.toUpperCase();

    if (value.equals("ALLOW-FROM")) {
      throw new IllegalArgumentException("action ALLOW_FROM is deprecated and should not be used");
    }

    if (!value.equals(DENY) && !value.equals(SAMEORIGIN)) {
      throw new IllegalArgumentException("action should be either DENY or SAMEORIGIN");
    }

    return ctx -> {
      ctx.response().putHeader("X-FRAME-OPTIONS", value);
      ctx.next();
    };
  }
}
