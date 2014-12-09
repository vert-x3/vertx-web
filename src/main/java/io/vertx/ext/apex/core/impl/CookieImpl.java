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
import io.vertx.ext.apex.addons.impl.ApexSecurity;
import io.vertx.ext.apex.core.Cookie;

import javax.crypto.Mac;
import java.util.Set;

/**
 * ApexCookie
 *
 * I'm not entirely happy this uses Netty.
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookieImpl implements Cookie {

  private final io.netty.handler.codec.http.Cookie nettyCookie;
  private final Mac mac;
  private String value;
  private boolean signed;

  public CookieImpl(String name, String value) {
    this.nettyCookie = new DefaultCookie(name, value);
    this.mac = null;
  }

  public CookieImpl(io.netty.handler.codec.http.Cookie nettyCookie, Mac mac) {
    this.nettyCookie = nettyCookie;
    this.mac = mac;

    // get the original value
    value = nettyCookie.getValue();
    // if the prefix is there then it is signed
    if (value.startsWith("s:")) {
      signed = true;
      // if it is signed get the unsigned value
      if (mac == null) {
        // this is an error
        value = null;
      } else {
        value = ApexSecurity.unsign(value.substring(2), mac);
      }
    }
  }

  // extensions
  public boolean isSigned() {
    return signed;
  }

  public void sign() {
    if (mac != null) {
      nettyCookie.setValue("s:" + ApexSecurity.sign(value, mac));
      signed = true;
    } else {
      signed = false;
    }
  }

  public String getUnsignedValue() {
    return value;
  }

  @Override
  public String getValue() {
    return nettyCookie.getValue();
  }

  @Override
  public Cookie setValue(final String value) {
    this.value = value;
    this.signed = false;
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
  public String getComment() {
    return nettyCookie.getComment();
  }

  @Override
  public Cookie setComment(final String comment) {
    nettyCookie.setComment(comment);
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
  public int getVersion() {
    return nettyCookie.getVersion();
  }

  @Override
  public Cookie setVersion(final int version) {
    nettyCookie.setVersion(version);
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
  public String getCommentUrl() {
    return nettyCookie.getCommentUrl();
  }

  @Override
  public Cookie setCommentUrl(final String commentUrl) {
    nettyCookie.setCommentUrl(commentUrl);
    return this;
  }

  @Override
  public boolean isDiscard() {
    return nettyCookie.isDiscard();
  }

  @Override
  public Cookie setDiscard(final boolean discard) {
    nettyCookie.setDiscard(discard);
    return this;
  }

  @Override
  public Set<Integer> getPorts() {
    return nettyCookie.getPorts();
  }

  @Override
  public void addPort(final int port) {
    nettyCookie.setPorts();
  }

  @Override
  public String encode() {
    return ServerCookieEncoder.encode(nettyCookie);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Cookie) {
      return ((Cookie)o).getName().equals(getName());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }
}
