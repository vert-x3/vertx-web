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

import io.vertx.ext.apex.core.Cookie;

import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class LookupCookie implements Cookie {

  private final String name;

  public LookupCookie(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue() {
    return null;
  }

  @Override
  public String getUnsignedValue() {
    return null;
  }

  @Override
  public Cookie setValue(String value) {
    return null;
  }

  @Override
  public String getDomain() {
    return null;
  }

  @Override
  public Cookie setDomain(String domain) {
    return null;
  }

  @Override
  public String getPath() {
    return null;
  }

  @Override
  public Cookie setPath(String path) {
    return null;
  }

  @Override
  public String getComment() {
    return null;
  }

  @Override
  public Cookie setComment(String comment) {
    return null;
  }

  @Override
  public long getMaxAge() {
    return 0;
  }

  @Override
  public Cookie setMaxAge(long maxAge) {
    return null;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public Cookie setVersion(int version) {
    return null;
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public Cookie setSecure(boolean secure) {
    return null;
  }

  @Override
  public boolean isHttpOnly() {
    return false;
  }

  @Override
  public Cookie setHttpOnly(boolean httpOnly) {
    return null;
  }

  @Override
  public String getCommentUrl() {
    return null;
  }

  @Override
  public Cookie setCommentUrl(String commentUrl) {
    return null;
  }

  @Override
  public boolean isDiscard() {
    return false;
  }

  @Override
  public Cookie setDiscard(boolean discard) {
    return null;
  }

  @Override
  public Set<Integer> getPorts() {
    return null;
  }

  @Override
  public void addPort(int port) {

  }

  @Override
  public String encode() {
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Cookie) {
      return ((Cookie)o).getName().equals(name);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}

