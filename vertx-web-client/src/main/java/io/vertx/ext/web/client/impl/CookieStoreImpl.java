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

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.vertx.ext.web.client.CookieStore;

/**
 * @author <a href="mailto:tommaso.nolli@gmail.com">Tommaso Nolli</a>
 */
public class CookieStoreImpl implements InternalCookieStore {

  private ConcurrentHashMap<Key, Cookie> noDomainCookies;
  private ConcurrentSkipListMap<Key, Cookie> domainCookies;
  
  public CookieStoreImpl() {
    noDomainCookies = new ConcurrentHashMap<>();
    domainCookies = new ConcurrentSkipListMap<>();
  }
  
  @Override
  public Iterable<Cookie> get(boolean ssl, String domain, String path) {
    /* TODO: Shall we cleanup path (remove dots)? Or HttpRequestImpl.uri is always normalized?
     *       Ideally this should call io.vertx.ext.web.impl.Utils.removeDots
     *       but vertx-web-client does not have a dependency on vertx-web so, probably, the Utils class (or parts of it)
     *       should be moved to vertx-web-commons.
     */

    assert domain != null && domain.length() > 0;
    
    TreeMap<String, Cookie> matches = new TreeMap<>();
    
    Consumer<Cookie> adder = c -> {
      if (!ssl && c.isSecure()) {
        return;
      }
      if (c.path() != null && !path.equals(c.path())) {
        String cookiePath = c.path();
        if (!cookiePath.endsWith("/")) {
          cookiePath += '/';
        }
        if (!path.startsWith(cookiePath)) {
          return;
        }
      }
      matches.put(c.name(), c);      
    };
    
    for (Cookie c : noDomainCookies.values()) {
      adder.accept(c);
    }
    
    Key key = new Key(domain, "", "");
    String prefix = key.domain.substring(0, 1);
    for (Entry<Key, Cookie> entry : domainCookies.tailMap(new Key(prefix, "", ""), true).entrySet()) {
      if (entry.getKey().domain.compareTo(key.domain) > 0) {
        break;
      }
      if (!key.domain.startsWith(entry.getKey().domain)) {
        continue;
      }
      adder.accept(entry.getValue());
    }
        
    return matches.values();
  }

  @Override
  public CookieStore put(String name, String value) {
    return put(name, value, null, null, null, false);
  }

  @Override
  public CookieStore put(String name, String value, String path) {
    return put(name, value, null, path, null, false);
  }

  @Override
  public CookieStore put(String name, String value, String domain, String path) {
    return put(name, value, domain, path, null, false);
  }

  @Override
  public CookieStore put(String name, String value, String domain, String path, Long maxAge, boolean isSecure) {
    DefaultCookie cookie = new DefaultCookie(name, value);
    cookie.setSecure(isSecure);
    if (domain != null)
      cookie.setDomain(domain);
    if (path != null)
      cookie.setPath(path);
    if (maxAge != null)
      cookie.setMaxAge(maxAge);
    return put(cookie);
  }
  @Override
  public CookieStore put(Cookie cookie) {
    Key key = new Key(cookie.domain(), cookie.path(), cookie.name());
    if (key.domain.equals(Key.NO_DOMAIN)) {
      noDomainCookies.put(key, cookie);
      return this;
    }
    domainCookies.put(key, cookie);
    return this;
  }

  @Override
  public CookieStore remove(String name) {
    return remove(name, null, null);
  }

  @Override
  public CookieStore remove(String name, String path) {
    return remove(name, null, path);
  }

  @Override
  public CookieStore remove(String name, String domain, String path) {
    Key key = new Key(domain, path, name);
    if (key.domain.equals(Key.NO_DOMAIN)) {
      noDomainCookies.remove(key);
    } else {
      domainCookies.remove(key);
    }
    return null;
  }

  private static class Key implements Comparable<Key> {
    private static final String NO_DOMAIN = "";
    
    private final String domain;
    private final String path;
    private final String name;

    public Key(String domain, String path, String name) {
      if (domain == null || domain.length() == 0) {
        this.domain = NO_DOMAIN;
      } else {
        while (domain.charAt(0) == '.') {
          domain = domain.substring(1);
        }
        while (domain.charAt(domain.length() - 1) == '.') {
          domain = domain.substring(0, domain.length() - 1);
        }
        if (domain.length() == 0) {
          this.domain = NO_DOMAIN;
        } else {
          String[] tokens = domain.split("\\.");
          String tmp;
          for (int i = 0, j = tokens.length - 1; i < tokens.length / 2; ++i, --j) {
            tmp = tokens[j];
            tokens[j] = tokens[i];
            tokens[i] = tmp;
          }
          this.domain = String.join(".", tokens);
        }
      }      
      this.path = path == null ? "" : path;
      this.name = name;
    }

    @Override
    public int compareTo(Key o) {
      int ret = domain.compareTo(o.domain);
      if (ret == 0)
        ret = path.compareTo(o.path);
      if (ret == 0)
        ret = name.compareTo(o.name);
      return ret;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((domain == null) ? 0 : domain.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((path == null) ? 0 : path.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Key other = (Key) obj;
      if (domain == null) {
        if (other.domain != null)
          return false;
      } else if (!domain.equals(other.domain))
        return false;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      if (path == null) {
        if (other.path != null)
          return false;
      } else if (!path.equals(other.path))
        return false;
      return true;
    }
  }  
}
