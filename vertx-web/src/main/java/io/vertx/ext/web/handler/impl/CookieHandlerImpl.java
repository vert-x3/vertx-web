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

package io.vertx.ext.web.handler.impl;

import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.impl.CookieImpl;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;

import static io.vertx.core.http.HttpHeaders.*;

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
public class CookieHandlerImpl implements CookieHandler {

  @Override
  public void handle(RoutingContext context) {
    String cookieHeader = context.request().headers().get(COOKIE);

    if (cookieHeader != null) {
      Set<io.netty.handler.codec.http.cookie.Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
      for (io.netty.handler.codec.http.cookie.Cookie cookie : nettyCookies) {
        Cookie ourCookie = new CookieImpl(cookie);
        context.addCookie(ourCookie);
      }
    }

    context.addHeadersEndHandler(v -> {
      // save the cookies
      Set<Cookie> cookies = context.cookies();
      for (Cookie cookie: cookies) {
        if (cookie.isChanged()) {
          context.response().headers().add(SET_COOKIE, cookie.encode());
        }
      }
    });

    context.next();
  }



}
