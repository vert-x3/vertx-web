package io.vertx.ext.web.handler.impl;

import io.vertx.core.Future;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;

import java.util.List;

/**
 * Internal interface for scope aware Authentication handlers.
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 * @param <SELF>
 */
public interface ScopedAuthentication<SELF extends AuthenticationHandler> {

  /**
   * Return a new instance with the internal state copied from the caller but the scopes to be requested during a token
   * request are unique to the instance.
   *
   * @param scope scope.
   * @return new instance of this interface.
   */
  SELF withScope(String scope);

  /**
   * Return a new instance with the internal state copied from the caller but the scopes to be requested during a token
   * request are unique to the instance.
   *
   * @param scopes scopes.
   * @return new instance of this interface.
   */
  SELF withScopes(List<String> scopes);

  /**
   * Verifies that the given user satisfies the scopes required by this handler.
   * The default implementation succeeds immediately (no scope requirements).
   *
   * @param ctx  the routing context
   * @param user the authenticated user
   * @return a succeeded future if the user's scopes are valid, a failed future otherwise
   */
  default Future<Void> verifyScopes(RoutingContext ctx, User user) {
    return Future.succeededFuture();
  }
}
