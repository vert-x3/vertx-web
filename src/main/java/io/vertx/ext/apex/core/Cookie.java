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

package io.vertx.ext.apex.core;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.apex.core.impl.CookieImpl;

/**
 * Derived from io.netty.handler.codec.http.Cookie
 */
@VertxGen
public interface Cookie {

  static Cookie cookie(String name, String value) {
    return new CookieImpl(name, value);
  }

  @GenIgnore
  static Cookie cookie(io.netty.handler.codec.http.Cookie nettyCookie) {
    return new CookieImpl(nettyCookie);
  }

  /**
   * Returns the name of this {@link Cookie}.
   *
   * @return The name of this {@link Cookie}
   */
  String getName();

  /**
   * Returns the value of this {@link Cookie}.
   *
   * @return The value of this {@link Cookie}
   */
  String getValue();

  /**
   * Sets the value of this {@link Cookie}.
   *
   * @param value The value to set
   */
  Cookie setValue(String value);

  /**
   * Returns the domain of this {@link Cookie}.
   *
   * @return The domain of this {@link Cookie}
   */
  String getDomain();

  /**
   * Sets the domain of this {@link Cookie}.
   *
   * @param domain The domain to use
   */
  Cookie setDomain(String domain);

  /**
   * Returns the path of this {@link Cookie}.
   *
   * @return The {@link Cookie}'s path
   */
  String getPath();

  /**
   * Sets the path of this {@link Cookie}.
   *
   * @param path The path to use for this {@link Cookie}
   */
  Cookie setPath(String path);

  /**
   * Returns the maximum age of this {@link Cookie} in seconds or {@link Long#MIN_VALUE} if unspecified
   *
   * @return The maximum age of this {@link Cookie}
   */
  long getMaxAge();

  /**
   * Sets the maximum age of this {@link Cookie} in seconds.
   * If an age of {@code 0} is specified, this {@link Cookie} will be
   * automatically removed by browser because it will expire immediately.
   * If {@link Long#MIN_VALUE} is specified, this {@link Cookie} will be removed when the
   * browser is closed.
   *
   * @param maxAge The maximum age of this {@link Cookie} in seconds
   */
  Cookie setMaxAge(long maxAge);

  /**
   * Checks to see if this {@link Cookie} is secure
   *
   * @return True if this {@link Cookie} is secure, otherwise false
   */
  boolean isSecure();

  /**
   * Sets the security getStatus of this {@link Cookie}
   *
   * @param secure True if this {@link Cookie} is to be secure, otherwise false
   */
  Cookie setSecure(boolean secure);

  /**
   * Checks to see if this {@link Cookie} can only be accessed via HTTP.
   * If this returns true, the {@link Cookie} cannot be accessed through
   * client side script - But only if the browser supports it.
   * For more information, please look <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>
   *
   * @return True if this {@link Cookie} is HTTP-only or false if it isn't
   */
  boolean isHttpOnly();

  /**
   * Determines if this {@link Cookie} is HTTP only.
   * If set to true, this {@link Cookie} cannot be accessed by a client
   * side script. However, this works only if the browser supports it.
   * For for information, please look
   * <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>.
   *
   * @param httpOnly True if the {@link Cookie} is HTTP only, otherwise false.
   */
  Cookie setHttpOnly(boolean httpOnly);


  String encode();

}
