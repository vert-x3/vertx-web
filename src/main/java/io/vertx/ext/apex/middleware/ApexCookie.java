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

package io.vertx.ext.apex.middleware;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.apex.middleware.impl.ApexCookieImpl;

import java.util.Set;

/**
 * Derived from io.netty.handler.codec.http.Cookie
 */
@VertxGen
public interface ApexCookie {

  static ApexCookie apexCookie(String name, String value) {
    return new ApexCookieImpl(name, value);
  }

  /**
   * Returns the name of this {@link ApexCookie}.
   *
   * @return The name of this {@link ApexCookie}
   */
  String getName();

  /**
   * Returns the value of this {@link ApexCookie}.
   *
   * @return The value of this {@link ApexCookie}
   */
  String getValue();

  String getUnsignedValue();

  /**
   * Sets the value of this {@link ApexCookie}.
   *
   * @param value The value to set
   */
  ApexCookie setValue(String value);

  /**
   * Returns the domain of this {@link ApexCookie}.
   *
   * @return The domain of this {@link ApexCookie}
   */
  String getDomain();

  /**
   * Sets the domain of this {@link ApexCookie}.
   *
   * @param domain The domain to use
   */
  ApexCookie setDomain(String domain);

  /**
   * Returns the path of this {@link ApexCookie}.
   *
   * @return The {@link ApexCookie}'s path
   */
  String getPath();

  /**
   * Sets the path of this {@link ApexCookie}.
   *
   * @param path The path to use for this {@link ApexCookie}
   */
  ApexCookie setPath(String path);

  /**
   * Returns the comment of this {@link ApexCookie}.
   *
   * @return The comment of this {@link ApexCookie}
   */
  String getComment();

  /**
   * Sets the comment of this {@link ApexCookie}.
   *
   * @param comment The comment to use
   */
  ApexCookie setComment(String comment);

  /**
   * Returns the maximum age of this {@link ApexCookie} in seconds or {@link Long#MIN_VALUE} if unspecified
   *
   * @return The maximum age of this {@link ApexCookie}
   */
  long getMaxAge();

  /**
   * Sets the maximum age of this {@link ApexCookie} in seconds.
   * If an age of {@code 0} is specified, this {@link ApexCookie} will be
   * automatically removed by browser because it will expire immediately.
   * If {@link Long#MIN_VALUE} is specified, this {@link ApexCookie} will be removed when the
   * browser is closed.
   *
   * @param maxAge The maximum age of this {@link ApexCookie} in seconds
   */
  ApexCookie setMaxAge(long maxAge);

  /**
   * Returns the version of this {@link ApexCookie}.
   *
   * @return The version of this {@link ApexCookie}
   */
  int getVersion();

  /**
   * Sets the version of this {@link ApexCookie}.
   *
   * @param version The new version to use
   */
  ApexCookie setVersion(int version);

  /**
   * Checks to see if this {@link ApexCookie} is secure
   *
   * @return True if this {@link ApexCookie} is secure, otherwise false
   */
  boolean isSecure();

  /**
   * Sets the security getStatus of this {@link ApexCookie}
   *
   * @param secure True if this {@link ApexCookie} is to be secure, otherwise false
   */
  ApexCookie setSecure(boolean secure);

  /**
   * Checks to see if this {@link ApexCookie} can only be accessed via HTTP.
   * If this returns true, the {@link ApexCookie} cannot be accessed through
   * client side script - But only if the browser supports it.
   * For more information, please look <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>
   *
   * @return True if this {@link ApexCookie} is HTTP-only or false if it isn't
   */
  boolean isHttpOnly();

  /**
   * Determines if this {@link ApexCookie} is HTTP only.
   * If set to true, this {@link ApexCookie} cannot be accessed by a client
   * side script. However, this works only if the browser supports it.
   * For for information, please look
   * <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>.
   *
   * @param httpOnly True if the {@link ApexCookie} is HTTP only, otherwise false.
   */
  ApexCookie setHttpOnly(boolean httpOnly);

  /**
   * Returns the comment URL of this {@link ApexCookie}.
   *
   * @return The comment URL of this {@link ApexCookie}
   */
  String getCommentUrl();

  /**
   * Sets the comment URL of this {@link ApexCookie}.
   *
   * @param commentUrl The comment URL to use
   */
  ApexCookie setCommentUrl(String commentUrl);

  /**
   * Checks to see if this {@link ApexCookie} is to be discarded by the browser
   * at the end of the current session.
   *
   * @return True if this {@link ApexCookie} is to be discarded, otherwise false
   */
  boolean isDiscard();

  /**
   * Sets the discard flag of this {@link ApexCookie}.
   * If set to true, this {@link ApexCookie} will be discarded by the browser
   * at the end of the current session
   *
   * @param discard True if the {@link ApexCookie} is to be discarded
   */
  ApexCookie setDiscard(boolean discard);

  /**
   * Returns the ports that this {@link ApexCookie} can be accessed on.
   *
   * @return The {@link java.util.Set} of ports that this {@link ApexCookie} can use
   */
  Set<Integer> getPorts();

  /**
   * Adds a port that this {@link ApexCookie} can be accessed on.
   *
   * @param port The ports that this {@link ApexCookie} can be accessed on
   */
  void addPort(int port);

  String encode();

}
