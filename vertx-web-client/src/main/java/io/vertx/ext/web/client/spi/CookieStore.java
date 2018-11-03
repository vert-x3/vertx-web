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
package io.vertx.ext.web.client.spi;

import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.ext.web.client.impl.CookieStoreImpl;

/**
 * A cookie store that manages cookies for a single user; received for different domains and valid for different paths.
 * 
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
public interface CookieStore {

  /**
   * Builds an in memory cookie store.
   * @return the new cookie store
   */
  static CookieStore build() {
    return new CookieStoreImpl();
  }
  
  /**
   * Returns and {@link Iterable} of cookies satisfying the filters passed as paraemters.
   * <p>
   * It is implementation responsibility to return the appropriate cookies between the ones stored in this store
   * and to clean up the path.
   * 
   * @param ssl true if is the connection secure
   * @param domain the domain we are calling
   * @param path the path we are calling
   * @return the matched cookies
   */
  Iterable<Cookie> get(boolean ssl, String domain, String path);
  
  /**
   * Add a cookie to this {@code CookieStore}.
   * <p>
   * If a cookie with the same name is received from the server, it will overwrite this setting.
   * 
   * @param cookie the {@link Cookie} to add
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CookieStore put(Cookie cookie);
  /**
   * Removes a previously added cookie.
   * 
   * @param cookie the {@link Cookie} to remove
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CookieStore remove(Cookie cookie);
}
