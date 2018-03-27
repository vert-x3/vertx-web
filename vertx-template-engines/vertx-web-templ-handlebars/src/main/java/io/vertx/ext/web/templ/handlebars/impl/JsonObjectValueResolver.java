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

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

import com.github.jknack.handlebars.ValueResolver;

import io.vertx.core.json.JsonObject;

/**
 * @author <a href="https://github.com/Jotschi">Johannes Schüth</a>
 */
public class JsonObjectValueResolver implements ValueResolver {

  public static final ValueResolver INSTANCE = new JsonObjectValueResolver();

  @Override
  public Object resolve(final Object context, final String name) {
    Object value = null;
    if (context instanceof JsonObject) {
      value = ((JsonObject) context).getValue(name);
    }
    return value == null ? UNRESOLVED : value;
  }

  @Override
  public Object resolve(final Object context) {
    if (context instanceof JsonObject) {
      return context;
    }
    return UNRESOLVED;
  }

  @Override
  public Set<Entry<String, Object>> propertySet(final Object context) {
    if (context instanceof JsonObject) {
      return ((JsonObject) context).getMap().entrySet();
    }
    return Collections.emptySet();
  }
}
