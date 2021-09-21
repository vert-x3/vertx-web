/*
 * Copyright 2021 Red Hat, Inc.
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
package io.vertx.ext.web.client.impl.cache;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class UserAgent {

  enum NormalizedType {
    MOBILE,
    DESKTOP
  }

  private final NormalizedType type;

  static UserAgent parse(MultiMap headers) {
    final String agent = headers.get(HttpHeaders.USER_AGENT);
    return new UserAgent(parseHeader(agent));
  }

  private UserAgent(NormalizedType type) {
    this.type = type;
  }

  public String normalize() {
    return type.name();
  }

  public NormalizedType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserAgent userAgent = (UserAgent) o;
    return type.equals(userAgent.type);
  }

  private static NormalizedType parseHeader(String string) {
    if (string == null) {
      return NormalizedType.DESKTOP;
    } else {
      return string.contains("Mobile") ? NormalizedType.MOBILE : NormalizedType.DESKTOP;
    }
  }
}
