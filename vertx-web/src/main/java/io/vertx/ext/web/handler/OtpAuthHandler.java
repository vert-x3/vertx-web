/*
 * Copyright 2021 Red Hat, Inc.
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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.auth.otp.OtpKeyGenerator;
import io.vertx.ext.auth.otp.hotp.HotpAuth;
import io.vertx.ext.auth.otp.totp.TotpAuth;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.HotpAuthHandlerImpl;
import io.vertx.ext.web.handler.impl.TotpAuthHandlerImpl;

/**
 * An auth handler that provides One Time Password (Multi-Factor) Authentication support.
 *
 * @author Paulo Lopes
 */
@VertxGen
public interface OtpAuthHandler extends Handler<RoutingContext> {

  static OtpAuthHandler create(TotpAuth totpAuth) {
    return new TotpAuthHandlerImpl(totpAuth, OtpKeyGenerator.create());
  }

  static OtpAuthHandler create(HotpAuth hotpAuth) {
    return new HotpAuthHandlerImpl(hotpAuth, OtpKeyGenerator.create());
  }

  @Fluent
  OtpAuthHandler verifyUrl(String url);

  @Fluent
  OtpAuthHandler setupRegisterCallback(Route route);

  @Fluent
  OtpAuthHandler setupCallback(Route route);

  @Fluent
  OtpAuthHandler period(long period);

  @Fluent
  OtpAuthHandler issuer(String issuer);

  @Fluent
  OtpAuthHandler label(String label);
}
