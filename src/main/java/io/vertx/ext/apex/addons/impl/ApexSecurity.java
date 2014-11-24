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

package io.vertx.ext.apex.addons.impl;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Base64;

/**
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 */
public abstract class ApexSecurity {

  protected ApexSecurity() {
  }

  /**
   * Creates a new Message Authentication Code
   *
   * @param alias algorithm to use e.g.: HmacSHA256
   * @return Mac implementation
   */
  public abstract Mac getMac(final String alias);

  public abstract Signature getSignature(final String alias);

  /**
   * Creates a new Crypto KEY
   *
   * @return Key implementation
   */
  public abstract Key getKey(final String alias);

  /**
   * Creates a new Cipher
   *
   * @return Cipher implementation
   */
  public static Cipher getCipher(final Key key, int mode) {
    try {
      Cipher cipher = Cipher.getInstance(key.getAlgorithm());
      cipher.init(mode, key);
      return cipher;
    } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Signs a String value with a given MAC
   */
  public static String sign(String val, Mac mac) {
    return val + "." + Base64.getEncoder().encodeToString(mac.doFinal(val.getBytes()));
  }

  /**
   * Returns the original value is the signature is correct. Null otherwise.
   */
  public static String unsign(String val, Mac mac) {
    int idx = val.lastIndexOf('.');

    if (idx == -1) {
      return null;
    }

    String str = val.substring(0, idx);
    if (val.equals(sign(str, mac))) {
      return str;
    }
    return null;
  }

  public static String encrypt(String val, Cipher cipher) {
    try {
      byte[] encVal = cipher.doFinal(val.getBytes());
      return Base64.getEncoder().encodeToString(encVal);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String decrypt(String val, Cipher cipher) {
    try {
      byte[] decordedValue = DatatypeConverter.parseBase64Binary(val);
      byte[] decValue = cipher.doFinal(decordedValue);
      return new String(decValue);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }
}
