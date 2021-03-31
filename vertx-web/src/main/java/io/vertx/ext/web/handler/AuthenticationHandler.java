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
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.RoutingContext;

/**
 * Base interface for auth handlers.
 * <p>
 * An auth handler allows your application to provide authentication support.
 * <p>
 * An Auth handler may require a {@link SessionHandler} to be on the routing chain before it.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
@VertxGen(concrete = false)
public interface AuthenticationHandler extends Handler<RoutingContext> {

  /**
   * Parses the credentials from the request into a JsonObject. The implementation should
   * be able to extract the required info for the auth provider in the format the provider
   * expects.
   *
   * @param context the routing context
   * @param handler the handler to be called once the information is available.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  void parseCredentials(RoutingContext context, Handler<AsyncResult<Credentials>> handler);

  /**
   * @see AuthenticationHandler#parseCredentials(RoutingContext, Handler)
   * @param context the routing context
   * @return Future json
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Future<Credentials> parseCredentials(RoutingContext context) {
    Promise<Credentials> promise = Promise.promise();
    parseCredentials(context, promise);
    return promise.future();
  }

  /**
   * Returns a {@code WWW-Authenticate} Response Header.
   *
   * If a server receives a request for an access-protected object, and an
   * acceptable Authorization header is not sent, the server responds with
   * a "401 Unauthorized" status code, and a WWW-Authenticate header.

   * @param context the routing context
   * @return the header or null if not applicable.
   */
  @Nullable
  default String authenticateHeader(RoutingContext context) {
    return null;
  }

  /**
   * This handler is called to perform any post authentication tasks, such as redirects or assertions.
   * Overrides must call {@link RoutingContext#next()} on success. Implementation must call this handler
   * at the end of the authentication process, or call {@link RoutingContext#next()} when no handler is
   * added.
   *
   * @param postAuthnHandler the routing context handler
   */
  @Fluent
  AuthenticationHandler postAuthenticationHandler(Handler<RoutingContext> postAuthnHandler);
}
