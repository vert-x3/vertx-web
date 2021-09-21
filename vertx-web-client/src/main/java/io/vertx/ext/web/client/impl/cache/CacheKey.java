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

import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.spi.CacheStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * A key for a {@link CacheStore} based on a {@link HttpRequest}.
 *
 * @author <a href="mailto:craigday3@gmail.com">Craig Day</a>
 */
public class CacheKey extends CacheVariationsKey {

  private final String variations;

  public CacheKey(HttpRequest<?> request, Vary vary) {
    super(request);
    this.variations = vary.toString();
  }

  @Override
  public String toString() {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(super.toString().getBytes(StandardCharsets.UTF_8));
      digest.update(variations.getBytes(StandardCharsets.UTF_8));
      byte[] hashed = digest.digest();
      return bytesToHex(hashed);
    } catch (Exception e) {
      return super.toString() + "|" + variations;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CacheKey cacheKey = (CacheKey) o;
    return port == cacheKey.port
      && host.equals(cacheKey.host)
      && path.equals(cacheKey.path)
      && queryString.equals(cacheKey.queryString)
      && variations.equals(cacheKey.variations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port, path, queryString, variations);
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
