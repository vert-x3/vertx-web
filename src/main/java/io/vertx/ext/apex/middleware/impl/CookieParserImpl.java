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

package io.vertx.ext.apex.middleware.impl;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.middleware.ApexCookie;
import io.vertx.ext.apex.middleware.CookieParser;

import javax.crypto.Mac;
import java.util.Set;
import java.util.TreeSet;

/**
 * # CookieParser
 * <p>
 * Parse request cookies both signed or plain.
 * <p>
 * If a cooke value starts with *s:* it means that it is a signed cookie. In this case the value is expected to be
 * *s:&lt;cookie&gt;.&lt;signature&gt;*. The signature is *HMAC + SHA256*.
 * <p>
 * When the Cookie parser is initialized with a secret then that value is used to verify if a cookie is valid.
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookieParserImpl implements CookieParser {

  /**
   * Message Signer
   */
  private final Mac mac;

  /**
   * Instantiates a CookieParser with a given Mac.
   * <p>
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new CookieParser(YokeSecurity.newHmacSHA256("s3cr3t")));
   * </pre>
   *
   * @param mac Mac
   */
  public CookieParserImpl(final Mac mac) {
    this.mac = mac;
  }

  /**
   * Instantiates a CookieParser without a Mac. In this case no cookies will be signed.
   * <p>
   * <pre>
   * Yoke yoke = new Yoke(...);
   * yoke.use(new CookieParser());
   * </pre>
   */
  public CookieParserImpl() {
    this(null);
  }

  @Override
  public void handle(RoutingContext context) {
    String cookieHeader = context.request().headers().get("cookie");

    if (cookieHeader != null) {
      Set<Cookie> nettyCookies = CookieDecoder.decode(cookieHeader);
      Set<ApexCookie> cookies = new TreeSet<>();

      for (Cookie cookie : nettyCookies) {
        ApexCookie yokeCookie = new ApexCookieImpl(cookie, mac);
        String value = yokeCookie.getUnsignedValue();
        // value cannot be null in a cookie if the signature is mismatch then this value will be null
        // in that case the cookie has been tampered
        if (value == null) {
          // FIXME - how to handle this??
          //next.handle(400);
          return;
        }
        cookies.add(yokeCookie);
      }

      context.setCookies(cookies);
    }

    context.next();
  }
}
