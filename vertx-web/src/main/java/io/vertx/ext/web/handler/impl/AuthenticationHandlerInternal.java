package io.vertx.ext.web.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;

public interface AuthenticationHandlerInternal extends AuthenticationHandler {

  /**
   * Parses the credentials from the request into a JsonObject. The implementation should
   * be able to extract the required info for the auth provider in the format the provider
   * expects.
   *
   * @param context the routing context
   * @param handler the handler to be called once the information is available.
   */
  void parseCredentials(RoutingContext context, Handler<AsyncResult<Credentials>> handler);

  /**
   * Returns a {@code WWW-Authenticate} Response Header.
   *
   * If a server receives a request for an access-protected object, and an
   * acceptable Authorization header is not sent, the server responds with
   * a "401 Unauthorized" status code, and a WWW-Authenticate header.

   * @param context the routing context
   * @return the header or null if not applicable.
   */
  default String authenticateHeader(RoutingContext context) {
    return null;
  }
}
