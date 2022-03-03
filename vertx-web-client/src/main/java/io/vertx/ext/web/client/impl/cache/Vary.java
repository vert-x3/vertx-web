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

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.HttpRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 * @author <a href="mailto:jllachf@gmail.com">Jordi Llach</a>
 */
public class Vary {

  private final MultiMap requestHeaders;
  private final MultiMap responseHeaders;
  private final Set<CharSequence> variations;

  public Vary(MultiMap requestHeaders, MultiMap responseHeaders) {
    this.requestHeaders = requestHeaders;
    this.responseHeaders = responseHeaders;
    this.variations = parseHeaders(responseHeaders);
  }

  public boolean matchesRequest(HttpRequest<?> request) {
    return variations.stream().allMatch(variation -> variationMatches(variation, request));
  }

  @Override
  public String toString() {
    List<String> parts = new ArrayList<>(variations.size());

    for (CharSequence variation : variations) {
      parts.addAll(normalizeValues(requestHeaders.getAll(variation)));
    }

    return parts.stream().sorted().collect(Collectors.joining(","));
  }

  private boolean variationMatches(CharSequence variation, HttpRequest<?> request) {
    if (HttpHeaders.USER_AGENT.equals(variation)) {
      return isUserAgentMatch(request);
    } else if (HttpHeaders.ACCEPT_ENCODING.equals(variation)) {
      return true;
    } else {
      return isExactMatch(variation, request);
    }
  }

  private boolean isUserAgentMatch(HttpRequest<?> request) {
    UserAgent original = UserAgent.parse(requestHeaders);
    UserAgent current = UserAgent.parse(request.headers());

    return original.equals(current);
  }

  private boolean isExactMatch(CharSequence variation, HttpRequest<?> request) {
    Set<String> a = normalizeValues(request.headers().getAll(variation));
    Set<String> b = normalizeValues(requestHeaders.getAll(variation));

    return a.equals(b);
  }

  private Set<String> normalizeValues(List<String> values) {
    return values
      .stream()
      .flatMap(v -> Arrays.stream(v.split(",")))
      .map(v -> v.trim().toLowerCase())
      .collect(Collectors.toSet());
  }

  private Set<CharSequence> parseHeaders(MultiMap headers) {
    List<String> varyHeaders = headers.getAll(HttpHeaders.VARY);
    Set<CharSequence> parsed = new HashSet<>(varyHeaders.size());

    varyHeaders.forEach(names -> {
      for (String name : names.split(",")) {
        parsed.add(HttpHeaders.createOptimized(name.trim().toLowerCase()));
      }
    });

    return parsed;
  }
}
