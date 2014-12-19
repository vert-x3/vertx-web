/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.apex.core;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
/**
 * Derived from io.netty.handler.codec.http.Cookie
 */
@CompileStatic
public class Cookie {
  final def io.vertx.ext.apex.core.Cookie delegate;
  public Cookie(io.vertx.ext.apex.core.Cookie delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static Cookie cookie(String name, String value) {
    def ret= Cookie.FACTORY.apply(io.vertx.ext.apex.core.Cookie.cookie(name, value));
    return ret;
  }
  /**
   * Returns the name of this {@link Cookie}.
   *
   * @return The name of this {@link Cookie}
   */
  public String getName() {
    def ret = this.delegate.getName();
    return ret;
  }
  /**
   * Returns the value of this {@link Cookie}.
   *
   * @return The value of this {@link Cookie}
   */
  public String getValue() {
    def ret = this.delegate.getValue();
    return ret;
  }
  /**
   * Sets the value of this {@link Cookie}.
   *
   * @param value The value to set
   */
  public Cookie setValue(String value) {
    def ret= Cookie.FACTORY.apply(this.delegate.setValue(value));
    return ret;
  }
  /**
   * Returns the domain of this {@link Cookie}.
   *
   * @return The domain of this {@link Cookie}
   */
  public String getDomain() {
    def ret = this.delegate.getDomain();
    return ret;
  }
  /**
   * Sets the domain of this {@link Cookie}.
   *
   * @param domain The domain to use
   */
  public Cookie setDomain(String domain) {
    def ret= Cookie.FACTORY.apply(this.delegate.setDomain(domain));
    return ret;
  }
  /**
   * Returns the path of this {@link Cookie}.
   *
   * @return The {@link Cookie}'s path
   */
  public String getPath() {
    def ret = this.delegate.getPath();
    return ret;
  }
  /**
   * Sets the path of this {@link Cookie}.
   *
   * @param path The path to use for this {@link Cookie}
   */
  public Cookie setPath(String path) {
    def ret= Cookie.FACTORY.apply(this.delegate.setPath(path));
    return ret;
  }
  /**
   * Returns the maximum age of this {@link Cookie} in seconds or {@link Long#MIN_VALUE} if unspecified
   *
   * @return The maximum age of this {@link Cookie}
   */
  public long getMaxAge() {
    def ret = this.delegate.getMaxAge();
    return ret;
  }
  /**
   * Sets the maximum age of this {@link Cookie} in seconds.
   * If an age of {@code 0} is specified, this {@link Cookie} will be
   * automatically removed by browser because it will expire immediately.
   * If {@link Long#MIN_VALUE} is specified, this {@link Cookie} will be removed when the
   * browser is closed.
   *
   * @param maxAge The maximum age of this {@link Cookie} in seconds
   */
  public Cookie setMaxAge(long maxAge) {
    def ret= Cookie.FACTORY.apply(this.delegate.setMaxAge(maxAge));
    return ret;
  }
  /**
   * Checks to see if this {@link Cookie} is secure
   *
   * @return True if this {@link Cookie} is secure, otherwise false
   */
  public boolean isSecure() {
    def ret = this.delegate.isSecure();
    return ret;
  }
  /**
   * Sets the security getStatus of this {@link Cookie}
   *
   * @param secure True if this {@link Cookie} is to be secure, otherwise false
   */
  public Cookie setSecure(boolean secure) {
    def ret= Cookie.FACTORY.apply(this.delegate.setSecure(secure));
    return ret;
  }
  /**
   * Checks to see if this {@link Cookie} can only be accessed via HTTP.
   * If this returns true, the {@link Cookie} cannot be accessed through
   * client side script - But only if the browser supports it.
   * For more information, please look <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>
   *
   * @return True if this {@link Cookie} is HTTP-only or false if it isn't
   */
  public boolean isHttpOnly() {
    def ret = this.delegate.isHttpOnly();
    return ret;
  }
  /**
   * Determines if this {@link Cookie} is HTTP only.
   * If set to true, this {@link Cookie} cannot be accessed by a client
   * side script. However, this works only if the browser supports it.
   * For for information, please look
   * <a href="http://www.owasp.org/index.php/HTTPOnly">here</a>.
   *
   * @param httpOnly True if the {@link Cookie} is HTTP only, otherwise false.
   */
  public Cookie setHttpOnly(boolean httpOnly) {
    def ret= Cookie.FACTORY.apply(this.delegate.setHttpOnly(httpOnly));
    return ret;
  }
  public String encode() {
    def ret = this.delegate.encode();
    return ret;
  }

  static final java.util.function.Function<io.vertx.ext.apex.core.Cookie, Cookie> FACTORY = io.vertx.lang.groovy.Factories.createFactory() {
    io.vertx.ext.apex.core.Cookie arg -> new Cookie(arg);
  };
}
