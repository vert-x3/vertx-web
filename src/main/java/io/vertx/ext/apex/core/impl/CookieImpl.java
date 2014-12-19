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

package io.vertx.ext.apex.core.impl;

import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.vertx.ext.apex.core.Cookie;

/**
 * ApexCookie
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookieImpl implements Cookie {

  private final io.netty.handler.codec.http.Cookie nettyCookie;

  public CookieImpl(String name, String value) {
    this.nettyCookie = new DefaultCookie(name, value);
  }

  public CookieImpl(io.netty.handler.codec.http.Cookie nettyCookie) {
    this.nettyCookie = nettyCookie;
  }

  @Override
  public String getValue() {
    return nettyCookie.getValue();
  }

  @Override
  public Cookie setValue(final String value) {
    nettyCookie.setValue(value);
    return this;
  }

  @Override
  public String getName() {
    return nettyCookie.getName();
  }

  @Override
  public String getDomain() {
    return nettyCookie.getDomain();
  }

  @Override
  public Cookie setDomain(final String domain) {
    nettyCookie.setDomain(domain);
    return this;
  }

  @Override
  public String getPath() {
    return nettyCookie.getPath();
  }

  @Override
  public Cookie setPath(final String path) {
    nettyCookie.setPath(path);
    return this;
  }


  @Override
  public long getMaxAge() {
    return nettyCookie.getMaxAge();
  }

  @Override
  public Cookie setMaxAge(final long maxAge) {
    nettyCookie.setMaxAge(maxAge);
    return this;
  }

  @Override
  public boolean isSecure() {
    return nettyCookie.isSecure();
  }

  @Override
  public Cookie setSecure(final boolean secure) {
    nettyCookie.setSecure(secure);
    return this;
  }

  @Override
  public boolean isHttpOnly() {
    return nettyCookie.isHttpOnly();
  }

  @Override
  public Cookie setHttpOnly(final boolean httpOnly) {
    nettyCookie.setHttpOnly(httpOnly);
    return this;
  }

  @Override
  public String encode() {
    return ServerCookieEncoder.encode(nettyCookie);
  }

}
