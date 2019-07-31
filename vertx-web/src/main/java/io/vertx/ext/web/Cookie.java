/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.web;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.impl.CookieImpl;

/**
 * Represents an HTTP Cookie.
 * <p>
 * All cookies must have a name and a value and can optionally have other fields set such as path, domain, etc.
 * <p>
 * (Derived from io.netty.handler.codec.http.Cookie)
 *
 * @deprecated use instead {@link io.vertx.core.http.Cookie}, this class will be removed in Vert.x 4
 */
@Deprecated
@VertxGen
public interface Cookie extends io.vertx.core.http.Cookie {

  /**
   * Create a new cookie
   * @param name  the name of the cookie
   * @param value  the cookie value
   * @return the cookie
   */
  static Cookie cookie(String name, String value) {
    return new CookieImpl(name, value);
  }

  /**
   * Create a new cookie from a Netty cookie
   * @param nettyCookie  the Netty cookie
   * @return the cookie
   */
  @GenIgnore
  static Cookie cookie(io.netty.handler.codec.http.cookie.Cookie nettyCookie) {
    return new CookieImpl(nettyCookie);
  }

  @Override
  @Fluent
  Cookie setValue(String value);

  @Override
  @Fluent
  Cookie setDomain(@Nullable String domain);

  @Override
  @Fluent
  Cookie setPath(@Nullable String path);

  @Override
  @Fluent
  Cookie setMaxAge(long maxAge);

  @Override
  @Fluent
  Cookie setSecure(boolean secure);

  @Override
  @Fluent
  Cookie setHttpOnly(boolean httpOnly);

  /**
   * Has the cookie been changed? Changed cookieMap will be saved out in the response and sent to the browser.
   *
   * @return true  if changed
   */
  boolean isChanged();

  /**
   * Set the cookie as being changed. Changed will be true for a cookie just created, false by default if just
   * read from the request
   *
   * @param changed  true if changed
   */
  void setChanged(boolean changed);

  /**
   * Has this Cookie been sent from the User Agent (the browser)? or was created during the executing on the request.
   *
   * @return true if the cookie comes from the User Agent.
   */
  boolean isFromUserAgent();

}
