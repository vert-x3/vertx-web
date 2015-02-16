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

package io.vertx.ext.apex.impl;

import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.vertx.ext.apex.Cookie;

/**
 * ApexCookie
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookieImpl implements Cookie {

  private final io.netty.handler.codec.http.Cookie nettyCookie;
  private boolean changed;

  public CookieImpl(String name, String value) {
    this.nettyCookie = new DefaultCookie(name, value);
    this.changed = true;
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
    this.changed = true;
    return this;
  }

  @Override
  public String getName() {
    return nettyCookie.getName();
  }

  @Override
  public Cookie setDomain(final String domain) {
    nettyCookie.setDomain(domain);
    this.changed = true;
    return this;
  }

  @Override
  public String getDomain() {
    return nettyCookie.getDomain();
  }

  @Override
  public Cookie setPath(final String path) {
    nettyCookie.setPath(path);
    this.changed = true;
    return this;
  }

  @Override
  public String getPath() {
    return nettyCookie.getPath();
  }

  @Override
  public Cookie setMaxAge(final long maxAge) {
    nettyCookie.setMaxAge(maxAge);
    this.changed = true;
    return this;
  }

  @Override
  public Cookie setSecure(final boolean secure) {
    nettyCookie.setSecure(secure);
    this.changed = true;
    return this;
  }

  @Override
  public Cookie setHttpOnly(final boolean httpOnly) {
    nettyCookie.setHttpOnly(httpOnly);
    this.changed = true;
    return this;
  }

  @Override
  public Cookie setVersion(int version) {
    nettyCookie.setVersion(version);
    this.changed = true;
    return this;
  }

  @Override
  public String encode() {
    return ServerCookieEncoder.encode(nettyCookie);
  }

  @Override
  public boolean isChanged() {
    return changed;
  }

  @Override
  public void setChanged(boolean changed) {
    this.changed = changed;
  }
}
