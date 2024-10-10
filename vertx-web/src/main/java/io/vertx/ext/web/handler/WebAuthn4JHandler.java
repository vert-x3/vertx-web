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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.auth.webauthn4j.WebAuthn4J;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.handler.impl.WebAuthn4JHandlerImpl;

/**
 * An auth handler that provides FIDO2 WebAuthN Relay Party support.
 *
 * @author Paulo Lopes
 */
@VertxGen
public interface WebAuthn4JHandler extends AuthenticationHandler {

  /**
   * Create a WebAuthN auth handler. This handler expects at least the response callback to be installed.
   *
   * @return the auth handler
   */
  static WebAuthn4JHandler create(WebAuthn4J webAuthn) {
    return new WebAuthn4JHandlerImpl(webAuthn);
  }

  /**
   * The callback route to create registration attestations. Usually this route is <pre>/webauthn/register</pre>
   *
   * @param route the route where credential get options are generated.
   * @return fluent self.
   */
  @Fluent
  WebAuthn4JHandler setupCredentialsCreateCallback(Route route);

  /**
   * The callback route to create login attestations. Usually this route is <pre>/webauthn/login</pre>
   *
   * @param route the route where credential get options are generated.
   * @return fluent self.
   */
  @Fluent
  WebAuthn4JHandler setupCredentialsGetCallback(Route route);

  /**
   * The callback route to verify attestations and assertions. Usually this route is <pre>/webauthn/response</pre>
   *
   * @param route the route where assertions and attestations are verified.
   * @return fluent self.
   */
  @Fluent
  WebAuthn4JHandler setupCallback(Route route);

  /**
   * Set the Origin to be validated by the webauthn object.
   *
   * @param origin - an HTTP Origin
   * @return fluent self
   */
  @Fluent
  WebAuthn4JHandler setOrigin(String origin);
}
