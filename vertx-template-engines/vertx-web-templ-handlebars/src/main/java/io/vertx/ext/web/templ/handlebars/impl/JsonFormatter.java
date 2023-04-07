/*
 * Copyright 2017 Red Hat, Inc.
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
package io.vertx.ext.web.templ.handlebars.impl;

import com.github.jknack.handlebars.Formatter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonFormatter implements Formatter {
  @Override
  public Object format(Object value, Chain next) {
    if (value instanceof JsonObject) {
      return ((JsonObject) value).encode();
    }
    if (value instanceof JsonArray) {
      return ((JsonArray) value).encode();
    }
    return next.format(value);
  }
}
