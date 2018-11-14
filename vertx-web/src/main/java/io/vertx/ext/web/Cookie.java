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
 */
@VertxGen
public interface Cookie {

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

  /**
   * @return the name of this cookie
   */
  String getName();

  /**
   * @return the value of this cookie
   */
  String getValue();

  /**
   * Sets the value of this cookie
   *
   * @param value The value to set
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Cookie setValue(String value);

  /**
   * Sets the domain of this cookie
   *
   * @param domain The domain to use
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Cookie setDomain(@Nullable String domain);

  /**
   * @return  the domain for the cookie
   */
  @Nullable
  String getDomain();

  /**
   * Sets the path of this cookie.
   *
   * @param path The path to use for this cookie
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Cookie setPath(@Nullable String path);

  /**
   *
   * @return the path for this cookie
   */
  @Nullable
  String getPath();

  /**
   * Sets the maximum age of this cookie in seconds.
   * If an age of {@code 0} is specified, this cookie will be
   * automatically removed by browser because it will expire immediately.
   * If {@link Long#MIN_VALUE} is specified, this cookie will be removed when the
   * browser is closed.
   * If you don't set this the cookie will be a session cookie and be removed when the browser is closed.
   *
   * @param maxAge The maximum age of this cookie in seconds
   */
  @Fluent
  Cookie setMaxAge(long maxAge);

  /**
   * Sets the security getStatus of this cookie
   *
   * @param secure True if this cookie is to be secure, otherwise false
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Cookie setSecure(boolean secure);

  /**
   * Determines if this cookie is HTTP only.
   * If set to true, this cookie cannot be accessed by a client
   * side script. However, this works only if the browser supports it.
   * For for information, please look
   * <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>.
   *
   * @param httpOnly True if the cookie is HTTP only, otherwise false.
   */
  @Fluent
  Cookie setHttpOnly(boolean httpOnly);

  /**
   * Encode the cookie to a string. This is what is used in the Set-Cookie header
   *
   * @return  the encoded cookie
   */
  String encode();

  /**
   * Has the cookie been changed? Changed cookies will be saved out in the response and sent to the browser.
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
