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

package io.vertx.ext.web;


import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.impl.SignatureImpl;

@VertxGen
public interface Signature {

  /**
   * Instantiate a new Signature instance with a secret
   * <p>
   * <pre>
   * Signature.create("s3cr37")
   * </pre>
   *
   * @param secret server secret to sign data.
   */
  static Signature create(String secret) {
    return new SignatureImpl(secret);
  }

  /**
   * Sign some data with a signature, the signature is appended after a . to the data
   * @param data the data to sign
   * @return a signed copy of the data
   */
  String sign(String data);

  /**
   * Verify that the provided signed data contains a signature which matches the data
   * @param signedData the signed data to verify
   * @return true if the signature matches the data
   */
  boolean verify(String signedData);

  /**
   * Verify that the provided signed data contains a signature which matches the data and returns the data
   * @param signedData the signed data to verify
   * @return the supplied data without the signature if the signature matches, null if it does not
   */
  @Nullable String parse(String signedData);
}
