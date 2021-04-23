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
import java.util.Collections;
import java.util.Date;
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

  private static final String SHARED_MAX_AGE = "s-maxage";
  private static final String MAX_AGE = "max-age";

  private final Set<String> directives;
  private final Map<String, Long> timeDirectives;
  private final Date expires;
  private final Date date;
  private final String etag;
  private final String vary;

  static CacheControl parse(MultiMap headers) {
    return new CacheControl(headers);
  }

  private CacheControl(MultiMap headers) {
    this.directives = new HashSet<>();
    this.timeDirectives = new HashMap<>();
    this.etag = headers.get(HttpHeaders.ETAG);
    this.vary = headers.get(HttpHeaders.VARY);

    if (headers.contains(HttpHeaders.DATE)) {
      this.date = DateFormatter.parseHttpDate(headers.get(HttpHeaders.DATE));
    } else {
      this.date = new Date();
    }

    if (headers.contains(HttpHeaders.EXPIRES)) {
      this.expires = DateFormatter.parseHttpDate(headers.get(HttpHeaders.EXPIRES));
    } else {
      this.expires = null;
    }

    parseAllCacheControl(headers);
  }

  public Set<String> directives() {
    return directives;
  }

  public Map<String, Long> timeDirectives() {
    return timeDirectives;
  }

  public String etag() {
    return etag;
  }

  public long maxAge() {
    if (!isPrivate() && timeDirectives.containsKey(SHARED_MAX_AGE)) {
      return timeDirectives.get(SHARED_MAX_AGE);
    } else if (timeDirectives.containsKey(MAX_AGE)) {
      return timeDirectives.get(MAX_AGE);
    } else if (expires != null) {
      return Duration.between(date.toInstant(), expires.toInstant()).getSeconds();
    } else {
      return 0L;
    }
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
    if (directives.contains("no-store")) {
      return false;
    }
    if (directives.contains("no-cache")) {
      return false;
    }
    if ("*".equals(vary)) {
      return false;
    }
    if (timeDirectives.getOrDefault(SHARED_MAX_AGE, 0L) <= 0) {
      return false;
    }
    if (timeDirectives.getOrDefault(MAX_AGE, 0L) <= 0) {
      return false;
    }
    if (expires != null && !expires.after(new Date())) {
      return false;
    }

    return true;
  }

  public boolean isPrivate() {
    return directives.contains("private");
  }

  public boolean isVarying() {
    return !variations().isEmpty();
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
