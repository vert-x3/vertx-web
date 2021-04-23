/*
 * Copyright 2021 Red Hat, Inc.
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
package io.vertx.ext.web.client.impl.cache;

import io.netty.handler.codec.DateFormatter;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parse HTTP headers to determine if and how to cache a response.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CacheControl {

  private final Set<String> directives;
  private final Map<String, Long> timeDirectives;
  private final Instant expires;
  private final Instant date;
  private final String etag;
  private final String vary;
  private final long maxAge;

  static CacheControl parse(MultiMap headers) {
    return new CacheControl(headers);
  }

  private CacheControl(MultiMap headers) {
    this.directives = new HashSet<>();
    this.timeDirectives = new HashMap<>();
    this.etag = headers.get(HttpHeaders.ETAG);
    this.vary = headers.get(HttpHeaders.VARY);

    if (headers.contains(HttpHeaders.DATE)) {
      this.date = DateFormatter.parseHttpDate(headers.get(HttpHeaders.DATE)).toInstant();
    } else {
      this.date = Instant.now();
    }

    if (headers.contains(HttpHeaders.EXPIRES)) {
      this.expires = DateFormatter.parseHttpDate(headers.get(HttpHeaders.EXPIRES)).toInstant();
    } else {
      this.expires = null;
    }

    // Order of operations matters here. We do plain assignment above, but then we must parse
    // the Cache-Control header before we can compute max age.
    parseAllCacheControl(headers);
    this.maxAge = computeMaxAge();
  }

  public Set<String> getDirectives() {
    return directives;
  }

  public Map<String, Long> getTimeDirectives() {
    return timeDirectives;
  }

  public String getEtag() {
    return etag;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public Set<CharSequence> variations() {
    if (vary == null) {
      return Collections.emptySet();
    } else {
      Set<CharSequence> variations = new HashSet<>();
      for (String variation : vary.split(",")) {
        variations.add(HttpHeaders.createOptimized(variation.trim().toLowerCase()));
      }
      return variations;
    }
  }

  public boolean isCacheable() {
    if (directives.contains(CacheControlDirectives.NO_STORE)) {
      return false;
    }
    if ("*".equals(vary)) {
      return false;
    }

    return maxAge > 0;
  }

  public boolean isPublic() {
    // Technically, you cannot say `Cache-Control: public, private` but on the chance that we do,
    // default to private which is considered safer and more strict.
    return directives.contains(CacheControlDirectives.PUBLIC) && !isPrivate();
  }

  public boolean isPrivate() {
    return directives.contains(CacheControlDirectives.PRIVATE);
  }

  public boolean isVarying() {
    return !variations().isEmpty();
  }

  public boolean noStore() {
    return directives.contains(CacheControlDirectives.NO_STORE);
  }

  public boolean noCache() {
    return !noStore() && directives.contains(CacheControlDirectives.NO_CACHE);
  }

  private long computeMaxAge() {
    if (!isPrivate() && timeDirectives.containsKey(CacheControlDirectives.SHARED_MAX_AGE)) {
      return timeDirectives.get(CacheControlDirectives.SHARED_MAX_AGE);
    } else if (timeDirectives.containsKey(CacheControlDirectives.MAX_AGE)) {
      return timeDirectives.get(CacheControlDirectives.MAX_AGE);
    } else if (expires != null) {
      return Duration.between(date, expires).getSeconds();
    } else {
      return Long.MAX_VALUE;
    }
  }

  private void parseAllCacheControl(MultiMap headers) {
    headers.getAll(HttpHeaders.CACHE_CONTROL).forEach(value -> {
      for (String headerDirectives : value.split(",")) {
        String[] directive = headerDirectives.split("=", 2);

        if (directive.length == 1) {
          directives.add(directive[0].trim().toLowerCase());
        } else {
          try {
            timeDirectives.put(
              directive[0].trim().toLowerCase(),
              Long.parseLong(directive[1].replaceAll("\"", "").trim().toLowerCase())
            );
          } catch (NumberFormatException e) {
            // The header contains unexpected data, ignore it
          }
        }
      }
    });
  }
}
