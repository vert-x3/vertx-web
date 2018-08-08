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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.sstore.AbstractSession;

import javax.crypto.Mac;
import java.util.Base64;

/**
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class CookieSession extends AbstractSession {

  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  private final Mac mac;
  // track the original version
  private int oldVersion = 0;
  // track the original crc
  private int oldCrc = 0;

  public CookieSession(Mac mac, PRNG prng, long timeout, int length) {
    super(prng, timeout, length);
    this.mac = mac;
  }

  public CookieSession(Mac mac, PRNG prng) {
    super(prng);
    this.mac = mac;
  }

  @Override
  public String value() {

    Buffer payload = new JsonObject()
      .put("id", id())
      .put("timeout", timeout())
      .put("lastAccessed", lastAccessed())
      .put("version", version())
      .put("data", data())
      .toBuffer();

    String b64 = ENCODER.encodeToString(payload.getBytes());
    String signature = ENCODER.encodeToString(mac.doFinal(b64.getBytes()));

    return b64 + "." + signature;
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

    String[] tokens = payload.split("\\.");
    if (tokens.length != 2) {
      throw new RuntimeException("Corrupted Session data");
    }

    String signature = ENCODER.encodeToString(mac.doFinal(tokens[0].getBytes()));

    if(!signature.equals(tokens[1])) {
      throw new RuntimeException("Session data was Tampered!");
    }

    // reconstruct the session
    JsonObject decoded = new JsonObject(Buffer.buffer(DECODER.decode(tokens[0])));

    setId(decoded.getString("id"));
    setTimeout(decoded.getLong("timeout"));
    setLastAccessed(decoded.getLong("lastAccessed"));
    setVersion(decoded.getInteger("version"));
    setData(decoded.getJsonObject("data"));

    // defaults
    oldVersion = version();
    oldCrc = crc();

    return this;
  }

  int oldVersion() {
    return oldVersion;
  }
}
