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
package io.vertx.ext.web.client;

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
   * It is implementation responsibility to return the appropriate cookies between the ones stored in this store.
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
   * @param name the cookie name
   * @param value the cookie value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CookieStore put(String name, String value);

  /**
   * Add a cookie to this {@code CookieStore}.
   * <p>
   * If a cookie with the same name is received from the server, it will overwrite this setting.
   * 
   * @param name the cookie name
   * @param value the cookie value
   * @param path the cookie path
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CookieStore put(String name, String value, String path);

  /**
   * Add a cookie to this {@code CookieStore}.
   * <p>
   * If a cookie with the same name is received from the server, it will overwrite this setting.
   * 
   * @param name the cookie name
   * @param value the cookie value
   * @param domain the coolie domain
   * @param path the cookie path
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CookieStore put(String name, String value, String domain, String path);

  /**
   * Add a cookie to this {@code CookieStore}.
   * <p>
   * If a cookie with the same name is received from the server, it will overwrite this setting.
   * <p>
   * Implementation notice: if you store cookies on disk and persist them between restarts,
   * you should honor the max-age attributes: max-ag<= 0 means that the cookie is a session cookie
   * to be removed when the user &quot;closes the browser&quot; (it is up to you to define this behavior)
   * 
   * @param name the cookie name
   * @param value the cookie value
   * @param domain the coolie domain
   * @param path the cookie path
   * @param maxAge the cookie max-age
   * @param isSecure true if the cookie must be used on https only
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CookieStore put(String name, String value, String domain, String path, Long maxAge, boolean isSecure);

  /**
   * Removes a previously added cookie.
   * 
   * @param name the cookie name
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CookieStore remove(String name);

  /**
   * Removes a previously added cookie.
   * 
   * @param name the cookie name
   * @param path the cookie path
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CookieStore remove(String name, String path);

  /**
   * Removes a previously added cookie.
   * 
   * @param name the cookie name
   * @param domain the cookie domain
   * @param path the cookie path
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  CookieStore remove(String name, String domain, String path);  
}
