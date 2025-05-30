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
package io.vertx.ext.web.sstore.cookie.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.auth.prng.VertxContextPRNG;
import io.vertx.ext.web.sstore.AbstractSession;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Base64;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class CookieSession extends AbstractSession {

  private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

  private static final String AES_ALGORITHM_GCM = "AES/GCM/NoPadding";

  private static final int IV_LENGTH = 12;
  private static final int TAG_LENGTH = 16;

  public static String base64UrlEncode(byte[] bytes) {
    return BASE64_URL_ENCODER.encodeToString(bytes);
  }

  public static byte[] base64UrlDecode(String base64) {
    return BASE64_URL_DECODER.decode(base64);
  }

  private static final Charset UTF8 = StandardCharsets.UTF_8;

  private final SecretKeySpec aesKey;
  private final VertxContextPRNG prng;
  // track the original version
  private int oldVersion = 0;
  // track the original crc
  private int oldCrc = 0;

  public CookieSession(SecretKeySpec aesKey, VertxContextPRNG prng, long timeout, int length) {
    super(prng, timeout, length);
    this.prng = prng;
    this.aesKey = aesKey;
  }

  public CookieSession(SecretKeySpec aesKey, VertxContextPRNG prng) {
    super(prng);
    this.prng = prng;
    this.aesKey = aesKey;
  }

  @Override
  public String value() {

    Buffer buff = Buffer.buffer();

    byte[] bytes = id().getBytes(UTF8);
    buff.appendInt(bytes.length).appendBytes(bytes);
    buff.appendLong(timeout());
    buff.appendLong(lastAccessed());
    buff.appendInt(version());
    writeDataToBuffer(buff);

    try {
      return encrypt(buff);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isRegenerated() {
    if (!super.isRegenerated()) {
      // force a new checksum calculation
      return oldCrc != checksum();
    }

    return true;
  }


  protected CookieSession setValue(String payload) {

    if (payload == null) {
      throw new NullPointerException();
    }

    try {
      final Buffer buffer = decrypt(payload);

      // reconstruct the session
      int pos = 0;
      int len = buffer.getInt(0);
      pos += 4;
      byte[] bytes = buffer.getBytes(pos, pos + len);
      pos += len;
      setId(new String(bytes, UTF8));
      setTimeout(buffer.getLong(pos));
      pos += 8;
      setLastAccessed(buffer.getLong(pos));
      pos += 8;
      setVersion(buffer.getInt(pos));
      pos += 4;
      readDataFromBuffer(pos, buffer);

      // defaults
      oldVersion = version();
      oldCrc = crc();
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
      // this is a bad session, force a regeneration
      return null;
    }

    return this;
  }

  int oldVersion() {
    return oldVersion;
  }

  private String encrypt(Buffer data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    // Initialization Vector
    byte[] iv = new byte[IV_LENGTH];
    prng.nextBytes(iv);

    // get a cipher
    Cipher cipher = Cipher.getInstance(AES_ALGORITHM_GCM);
    GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH * 8, iv);
    cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

    byte[] encryptedBytes = cipher.doFinal(data.getBytes());

    // combine IV and cipher data
    byte[] combinedIvAndCipherText = new byte[iv.length + encryptedBytes.length];
    System.arraycopy(iv, 0, combinedIvAndCipherText, 0, iv.length);
    System.arraycopy(encryptedBytes, 0, combinedIvAndCipherText, iv.length, encryptedBytes.length);

    return base64UrlEncode(combinedIvAndCipherText);
  }

  private Buffer decrypt(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    byte[] decodedCipherText = base64UrlDecode(data);

    // extract the IV
    byte[] iv = new byte[IV_LENGTH];
    System.arraycopy(decodedCipherText, 0, iv, 0, iv.length);
    byte[] encryptedText = new byte[decodedCipherText.length - IV_LENGTH];
    System.arraycopy(decodedCipherText, IV_LENGTH, encryptedText, 0, encryptedText.length);

    // get a cipher
    Cipher cipher = Cipher.getInstance(AES_ALGORITHM_GCM);
    GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH * 8, iv);
    cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

    byte[] decryptedBytes = cipher.doFinal(encryptedText);

    return Buffer.buffer(decryptedBytes);
  }
}
