/*
 * Copyright 2016 Red Hat, Inc.
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

package io.vertx.ext.web.templ;

import io.vertx.core.Vertx;

/**
 * A template engine that uses the FreeMarker library.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 * @deprecated please use {@link io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine} instead.
 */
@Deprecated
public interface FreeMarkerTemplateEngine extends io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine {
  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine create(Vertx vertx) {
    return io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine.create(vertx);
  }
}
