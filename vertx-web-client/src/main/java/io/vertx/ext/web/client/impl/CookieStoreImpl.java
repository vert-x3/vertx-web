/*
 * Copyright (c) 2011-2018 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.web.client.impl;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.core.internal.net.RFC3986;
import io.vertx.ext.web.client.spi.CookieStore;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
public class CookieStoreImpl implements CookieStore {

  private final ConcurrentSkipListMap<Key, Cookie> cookies;

  public CookieStoreImpl() {
    cookies = new ConcurrentSkipListMap<>();
  }

  @Override
  public Iterable<Cookie> get(Boolean ignored, String domain, String path) {
    if (domain == null) {
      throw new NullPointerException();
    }
    if (domain.isEmpty()) {
      throw new IllegalArgumentException();
    }

    Predicate<Cookie> adder = c -> {
      if (c.path() != null && !path.equals(c.path())) {
        String cookiePath = c.path();
        if (!cookiePath.endsWith("/")) {
          cookiePath += '/';
        }
        return path.startsWith(cookiePath);
      } else {
        return true;
      }
    };

    Key key = new Key(domain, "", "");
    String prefix = key.domain.substring(0, 1);
    List<Cookie> ret = Collections.emptyList();
    Set<Entry<Key, Cookie>> entries = cookies.tailMap(new Key(prefix, "", ""), true).entrySet();
    for (Entry<Key, Cookie> entry : entries) {
      if (entry.getKey().domain.compareTo(key.domain) > 0) {
        break;
      } else if ((key.domain).startsWith(entry.getKey().domain) && adder.test(entry.getValue())) {
        Cookie abc = entry.getValue();
        if (ret.isEmpty()) {
          ret = new ArrayList<>();
        }
        ret.add(abc);
      }
    }
    return ret;
  }

  @Override
  public CookieStore put(Cookie cookie) {
    Key key = new Key(cookie.domain(), cookie.path(), cookie.name());
    cookies.put(key, cookie);
    return this;
  }

  @Override
  public CookieStore remove(Cookie cookie) {
    Key key = new Key(cookie.domain(), cookie.path(), cookie.name());
    cookies.remove(key);
    return this;
  }

  private final static class Key implements Comparable<Key> {

    private final String domain;
    private final String path;
    private final String name;

    public Key(String domain, String path, String name) {
      if (domain == null) {
        throw new NullPointerException();
      }
      if (domain.isEmpty()) {
        throw new IllegalArgumentException();
      }
      while (domain.charAt(0) == '.') {
        domain = domain.substring(1);
      }
      while (domain.charAt(domain.length() - 1) == '.') {
        domain = domain.substring(0, domain.length() - 1);
      }
      String[] tokens = domain.split("\\.");
      String tmp;
      for (int i = 0, j = tokens.length - 1; i < tokens.length / 2; ++i, --j) {
        tmp = tokens[j];
        tokens[j] = tokens[i];
        tokens[i] = tmp;
      }
      this.domain = String.join(".", tokens) + ".";
      this.path = path == null ? "" : path;
      this.name = name;
    }

    @Override
    public int compareTo(Key o) {
      int ret = domain.compareTo(o.domain);
      if (ret == 0) {
        ret = path.compareTo(o.path);
      }
      if (ret == 0) {
        ret = name.compareTo(o.name);
      }
      return ret;
    }

    @Override
    public int hashCode() {
      return Objects.hash(domain, path, name);
    }

    @Override
    public boolean equals(Object obj) {
      throw new UnsupportedOperationException("Should not be called since used in TreeMap");
    }
  }
}
