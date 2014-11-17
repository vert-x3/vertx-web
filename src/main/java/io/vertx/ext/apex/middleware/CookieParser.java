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
package io.vertx.ext.apex.middleware;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.core.RoutingContext;
import io.vertx.ext.apex.middleware.impl.CookieParserImpl;

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
 */
@VertxGen
public interface CookieParser extends Handler<RoutingContext> {

  static CookieParser cookierParser() {
    return new CookieParserImpl();
  }

  @Override
  void handle(RoutingContext event);
}
