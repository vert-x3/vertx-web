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
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.impl.HttpRequestImpl;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A key for a {@link io.vertx.ext.web.client.cache.CacheAdapter} based on a {@link HttpRequest}.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CacheKey {

  private final HttpRequestImpl<?> request;

  public CacheKey(HttpRequest<?> request) {
    this.request = (HttpRequestImpl<?>) request;
  }

  public String digest() {
    List<String> keyParts = new ArrayList<>(3);

    keyParts.add(request.host());
    keyParts.add(Integer.toString(request.port()));
    keyParts.add(queryString(request.queryParams()));

    String rawKey = String.join("|", keyParts);

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA3-256");
      byte[] hashed = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(hashed);
    } catch (NoSuchAlgorithmException e) {
      return rawKey;
    }
  }

  private String queryString(MultiMap queryParams) {
    return queryParams.entries()
      .stream()
      .map(e -> e.getKey() + "=" + e.getValue())
      .collect(Collectors.joining("&"));
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);

    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if(hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }

    return hexString.toString();
  }
}
