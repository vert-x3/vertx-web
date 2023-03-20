/*
 * Copyright 2018 Red Hat, Inc.
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
package io.vertx.ext.web.sstore.cookie;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.cookie.impl.CookieSessionStoreImpl;

/**
 * A SessionStore that uses a Cookie to store the session data. All data is stored in
 * encrypted form using {@code AES-256 with AES/CBC/PKCS5Padding}.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen
public interface CookieSessionStore extends SessionStore {

  /**
   * Creates a CookieSessionStore.
   *
   * Cookie data will be encrypted using the given secret and salt. The secret as the name
   * reflects, should never leave the server, otherwise user agents could tamper
   * with the payload. The salt adds an extra later of security and should be a random.
   *
   * @param vertx a vert.x instance
   * @param secret a secret to derive a secure private key
   * @param salt a binary salt used in the key derivation
   * @return the store
   */
  static CookieSessionStore create(Vertx vertx, String secret, Buffer salt) {
    return new CookieSessionStoreImpl(vertx, secret, salt);
  }
}
