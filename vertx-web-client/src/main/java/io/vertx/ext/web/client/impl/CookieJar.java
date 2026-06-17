package io.vertx.ext.web.client.impl;

import io.netty.handler.codec.http.cookie.Cookie;
import io.vertx.core.internal.net.RFC3986;
import io.vertx.ext.web.client.spi.CookieStore;

import java.util.*;

public class CookieJar {

  private final CookieStore store;

  public CookieJar(CookieStore store) {
    this.store = store;
  }

  public List<Cookie> cookies(boolean requestSecure, String requestDomain, String requestUri) {
    long now = System.currentTimeMillis();
    String path = extractPath(requestUri);
    Iterable<Cookie> cookies = store.get((Boolean)null, requestDomain, path);
    List<Cookie> list = new ArrayList<>();
    List<Cookie> expired = null;
    for (Cookie cookie : cookies) {
      StoreCookie actual = (StoreCookie) cookie;
      if (actual.hostOnly && !cookie.domain().equals(requestDomain)) {
        // Check exact matching
        continue;
      }
      if (cookie.isSecure() && !requestSecure) {
        continue;
      }
      if (actual.expiryTime != null && actual.expiryTime <= now) {
        if (expired == null) {
          expired = new ArrayList<>();
        }
        expired.add(cookie);
        continue;
      }
      list.add(cookie);
    }
    if (expired != null) {
      for (Cookie cookie : expired) {
        store.remove(cookie);
      }
    }
    Collections.sort(list, new Comparator<Cookie>() {
      @Override
      public int compare(Cookie o1, Cookie o2) {
        return compare((StoreCookie)o1, (StoreCookie)o2);
      }
      private int compare(StoreCookie o1, StoreCookie o2) {
        int cmp = o1.path().compareTo(o2.path());
        if (cmp == 0) {
          cmp = Long.compare(o2.creationTime, o1.creationTime);
        }
        return -cmp;
      }
    });
    return list;
  }

  public void setCookie(long creationTime, String domain, String requestUri, Cookie cookie) {
    if (domain.isEmpty()) {
      throw new IllegalArgumentException("Domain must not be empty");
    }

    // Compute the default-path
    String path = extractPath(requestUri);
    String defaultPath;
    int lastSlashIdx;
    if (path.isEmpty() || path.charAt(0) != '/' || path.length() == 1 || ((lastSlashIdx = path.lastIndexOf('/')) < 1)) {
      defaultPath = "/";
    } else {
      defaultPath = path.substring(0, lastSlashIdx);
    }
    if (cookie.path() == null) {
      cookie.setPath(defaultPath);
    }
    boolean hostOnly;
    if (cookie.domain() == null) {
      // Set the domain if missing, because we need to send cookies
      // only to the domains we received them from.
      cookie.setDomain(domain);
      hostOnly = true;
    } else if (cookie.domain().isEmpty()) {
      throw new IllegalArgumentException("Cookie domain must not be empty");
    } else if (domain.equals(cookie.domain()) || domain.endsWith("." + cookie.domain())) {
      hostOnly = false;
    } else {
      return;
    }

    if (cookie.maxAge() == 0L) {
      store.remove(cookie);
    } else {

      Long expiryTime;
      if (cookie.maxAge() > 0L) {
        expiryTime = creationTime + cookie.maxAge() * 1000;
      } else {
        expiryTime = null;
      }

      // Preserve creation time if we reinsert
      StoreCookie prev = (StoreCookie) store.get(cookie.name(), cookie.domain(), cookie.path());
      if (prev != null) {
        creationTime = prev.creationTime;
      }
      store.put(new StoreCookie(cookie, hostOnly, creationTime, expiryTime));
    }
  }

  static class StoreCookie implements Cookie {

    private final Cookie actual;

    // Metadata
    private final boolean hostOnly;
    private final long creationTime;
    private final Long expiryTime;

    public StoreCookie(Cookie actual, boolean hostOnly, long creationTime, Long expiryTime) {
      this.actual = actual;
      this.hostOnly = hostOnly;
      this.creationTime = creationTime;
      this.expiryTime = expiryTime;
    }

    @Override
    public String name() {
      return actual.name();
    }

    @Override
    public String value() {
      return actual.value();
    }

    @Override
    public void setValue(String value) {
      actual.setValue(value);
    }

    @Override
    public boolean wrap() {
      return actual.wrap();
    }

    @Override
    public void setWrap(boolean wrap) {
      actual.setWrap(wrap);
    }

    @Override
    public String domain() {
      return actual.domain();
    }

    @Override
    public void setDomain(String domain) {
      actual.setDomain(domain);
    }

    @Override
    public String path() {
      return actual.path();
    }

    @Override
    public void setPath(String path) {
      actual.setPath(path);
    }

    @Override
    public long maxAge() {
      return actual.maxAge();
    }

    @Override
    public void setMaxAge(long maxAge) {
      actual.setMaxAge(maxAge);
    }

    @Override
    public boolean isSecure() {
      return actual.isSecure();
    }

    @Override
    public void setSecure(boolean secure) {
      actual.setSecure(secure);
    }

    @Override
    public boolean isHttpOnly() {
      return actual.isHttpOnly();
    }

    @Override
    public void setHttpOnly(boolean httpOnly) {
      actual.setHttpOnly(httpOnly);
    }

    @Override
    public int compareTo(Cookie o) {
      return actual.compareTo(o);
    }
  }

  private static String extractPath(String requestUri) {

    requestUri = RFC3986.removeDotSegments(requestUri);

    // Remove query params if present
    int pos = requestUri.indexOf('?');
    if (pos > -1) {
      requestUri = requestUri.substring(0, pos);
    }

    // Remove fragment identifier if present
    pos = requestUri.indexOf('#');
    if (pos > -1) {
      requestUri = requestUri.substring(0, pos);
    }

    return requestUri;
  }
}
