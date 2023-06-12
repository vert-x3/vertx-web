/*
 * Copyright 2023 Red Hat, Inc.
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

package io.vertx.ext.web.impl;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static io.vertx.ext.auth.impl.Codec.base64UrlEncode;

public class Signature {
  private static final Logger LOG = LoggerFactory.getLogger(Signature.class);
  private static final String algorithm = "HmacSHA256";

  private final Mac mac;

  public Signature(final String secret) {
    try {
      if (secret.length() <= 8) {
        LOG.warn("Signing secret is very short (<= 8 bytes)");
      }
      mac = Mac.getInstance(algorithm);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException(e);
    }
  }

  public String sign(String data) {
    final String signature;
    synchronized (mac) {
      signature = base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.US_ASCII)));
    }
    return data + "." + signature;
  }

  public boolean verify(String signedData) {
    String[] parts = signedData.split("\\.");
    if (parts.length < 2) {
      // There is no signature on this data
      return false;
    }

    final String signature = parts[parts.length - 1];
    final String[] dataParts = Arrays.copyOfRange(parts,0, parts.length - 1);
    final String data = String.join(".", dataParts);
    final String calculatedSignature;

    synchronized (mac) {
      calculatedSignature = base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.US_ASCII)));
    }

    return MessageDigest.isEqual(
      calculatedSignature.getBytes(StandardCharsets.US_ASCII),
      signature.getBytes(StandardCharsets.US_ASCII)
    );
  }

  public String parse(String signedData) {
    if (!verify(signedData)) {
      return null;
    }

    String[] parts = signedData.split("\\.");
    final String[] dataParts = Arrays.copyOfRange(parts,0, parts.length - 1);
    return String.join(".", dataParts);
  }
}
