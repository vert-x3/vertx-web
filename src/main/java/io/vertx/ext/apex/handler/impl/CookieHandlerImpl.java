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

package io.vertx.ext.apex.handler.impl;

import static io.vertx.core.http.HttpHeaders.COOKIE;
import static io.vertx.core.http.HttpHeaders.SET_COOKIE;
import io.netty.handler.codec.http.CookieDecoder;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Cookie;
import io.vertx.ext.apex.Crypto;
import io.vertx.ext.apex.RoutingContext;
import io.vertx.ext.apex.handler.CookieHandler;
import io.vertx.ext.apex.impl.CookieImpl;

import java.util.Objects;
import java.util.Set;

/**
 * # CookieParser
 * <p>
 * Parse request cookies both encrypted or plain.
 * <p>
 * If a codec is set then cookie values are encrypted/decrypted using the codec.
 * <p>
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class CookieHandlerImpl implements CookieHandler {

  private static final Logger logger = LoggerFactory.getLogger(CookieHandlerImpl.class);

  private Crypto codec;

  public CookieHandlerImpl() {
  }
  
  public CookieHandlerImpl(Crypto codec) {
    Objects.requireNonNull(codec);
    
    this.codec = codec;
  }
  
  @Override
  public void handle(RoutingContext context) {
    String cookieHeader = context.request().headers().get(COOKIE);

    if (cookieHeader != null) {
      Set<io.netty.handler.codec.http.Cookie> nettyCookies = CookieDecoder.decode(cookieHeader);
      for (io.netty.handler.codec.http.Cookie cookie : nettyCookies) {
        Cookie apexCookie = getDecryptedCookie(cookie);
        // a cookie can be null in case something went wrong when decrypting its value
        if (apexCookie!=null) {
          context.addCookie(apexCookie);
        }
      }
    }

    context.addHeadersEndHandler(v -> {
      // save cookies
      Set<Cookie> cookies = context.cookies();
      for (Cookie cookie: cookies) {
        if (cookie.isChanged()) {
          String value = encryptValue(cookie.encode());
          if (value!=null) {
            context.response().headers().add(SET_COOKIE, value);
          }
        }
      }

    });

    context.next();
  }

  private Cookie getDecryptedCookie(io.netty.handler.codec.http.Cookie nettyCookie) {
    if (codec!=null) {
      try {
        // swap the value of the netty cookie with the decrypted value
        nettyCookie.setValue(codec.decrypt(nettyCookie.getValue()));
      }
      catch (Exception e) {
       // something went wrong when decrypting the cookie. It could be that someone modified the data.
       // in any cases, we should ignore the cookie
        logger.error("Could not decrypt cookie", e);
        return null;
      }
    }
    return new CookieImpl(nettyCookie) ;
  }

  private String encryptValue(String value) {
    if (codec!=null) {
      try {
        // swap the value with the encoded value
        value = codec.encrypt(value);
      }
      catch (Exception e) {
       // something went wrong when encrypting the value
        logger.error("Could not encrypt cookie", e);
        return null;
      }
    }
    return value;
  }
  
}
