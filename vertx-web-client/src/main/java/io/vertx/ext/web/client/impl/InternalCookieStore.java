/*
 * Copyright (c) 2011-2018 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.web.client.impl;

import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.ext.web.client.CookieStore;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
public interface InternalCookieStore extends CookieStore {

  CookieStore put(Cookie cookie);
  
}
