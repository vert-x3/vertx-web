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

package io.vertx.ext.web.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.impl.ServerCookie;
import io.vertx.ext.web.Cookie;


/**
 * Vert.x-Web cookie implementation
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookieImpl implements Cookie, ServerCookie {

  static Cookie wrapIfNecessary(ServerCookie cookie) {
    if (cookie == null) {
      return null;
    }
    return cookie instanceof Cookie ? (Cookie) cookie : new CookieImpl(cookie);
  }

  final io.vertx.core.http.impl.ServerCookie delegate;

  public CookieImpl(String name, String value) {
    this.delegate = (ServerCookie) io.vertx.core.http.Cookie.cookie(name, value);
  }

  public CookieImpl(io.vertx.core.http.impl.ServerCookie delegate) {
    this.delegate = delegate;
  }

  public CookieImpl(io.netty.handler.codec.http.cookie.Cookie nettyCookie) {
    this.delegate = new io.vertx.core.http.impl.CookieImpl(nettyCookie);
  }

  @Override
  public String getValue() {
    return delegate.getValue();
  }

  @Override
  public Cookie setValue(final String value) {
    delegate.setValue(value);
    return this;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public Cookie setDomain(final String domain) {
    delegate.setDomain(domain);
    return this;
  }

  @Override
  public String getDomain() {
    return delegate.getDomain();
  }

  @Override
  public Cookie setPath(final String path) {
    delegate.setPath(path);
    return this;
  }

  @Override
  public String getPath() {
    return delegate.getPath();
  }

  @Override
  public Cookie setMaxAge(final long maxAge) {
    delegate.setMaxAge(maxAge);
    return this;
  }

  @Override
  public Cookie setSecure(final boolean secure) {
    delegate.setSecure(secure);
    return this;
  }

  @Override
  public boolean isSecure() {
    return delegate.isSecure();
  }

  @Override
  public Cookie setHttpOnly(final boolean httpOnly) {
    delegate.setHttpOnly(httpOnly);
    return this;
  }

  @Override
  public boolean isHttpOnly() {
    return delegate.isHttpOnly();
  }

  @Override
  public Cookie setSameSite(final CookieSameSite policy) {
    delegate.setSameSite(policy);
    return this;
  }

  @Override
  public @Nullable CookieSameSite getSameSite() {
    return delegate.getSameSite();
  }

  @Override
  public String encode() {
    return delegate.encode();
  }

  @Override
  public boolean isChanged() {
    return delegate.isChanged();
  }

  @Override
  public void setChanged(boolean changed) {
    delegate.setChanged(changed);
  }

  @Override
  public boolean isFromUserAgent() {
    return delegate.isFromUserAgent();
  }
}
