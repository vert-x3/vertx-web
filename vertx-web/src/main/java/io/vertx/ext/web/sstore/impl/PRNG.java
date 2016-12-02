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
package io.vertx.ext.web.sstore.impl;

import io.vertx.core.Vertx;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Wrapper around secure random that periodically seeds the PRNG with new entropy.
 *
 * @author Paulo Lopes
 */
public class PRNG {

  private static final int DEFAULT_SEED_INTERVAL_MILLIS = 300000;
  private static final int DEFAULT_SEED_BITS = 64;

  private final SecureRandom random;
  private final long seedID;

  private final Vertx vertx;

  public PRNG(Vertx vertx) {
    this.vertx = vertx;

    final String algorithm = System.getProperty("io.vertx.ext.web.session.algorithm");
    final int seedInterval = Integer.getInteger("io.vertx.ext.web.session.seed.interval", DEFAULT_SEED_INTERVAL_MILLIS);
    final int seedBits = Integer.getInteger("io.vertx.ext.web.session.seed.bits", DEFAULT_SEED_BITS);

    if (algorithm != null) {
      // the user has made a conscious decision to not use the JVM defaults
      try {
        random = SecureRandom.getInstance(algorithm);
      } catch (NoSuchAlgorithmException e) {
        // the algorithm is not available
        throw new RuntimeException(e);
      }
    } else {
      // initialize a secure random (note that on unices JDK8 will default to a mixed mode nativeprng
      // (non-blocking for getBytes() blocking for generateSeed()). A similar behavior is expected with SHA1PRNG which
      // will be the fallback on Windows
      random = new SecureRandom();
    }

    // Make sure default seeding happens now to avoid calling setSeed() too early
    random.nextBytes(new byte[1]);

    // seed internal and bits must be enabled
    if (seedInterval > 0 && seedBits > 0) {
      // Add a 64bit entropy every five minutes
      // see: https://www.owasp.org/index.php/Session_Management_Cheat_Sheet#Session_ID_Entropy
      seedID = vertx.setPeriodic(
        seedInterval,
        id -> vertx.<byte[]>executeBlocking(
          future -> future.complete(random.generateSeed(seedBits / 8)),
          false,
          asyncResult -> random.setSeed(asyncResult.result())));
    } else {
      seedID = -1;
    }
  }

  void close() {
    // stop seeding the PRNG
    if (seedID != -1) {
      vertx.cancelTimer(seedID);
    }
  }

  void nextBytes(byte[] bytes) {
    random.nextBytes(bytes);
  }
}
