package io.vertx.ext.web.handler.impl;

import io.vertx.ext.web.handler.AuthenticationHandler;

import java.util.List;

/**
 * Internal interface for scope aware Authentication handlers.
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 * @param <SELF>
 */
public interface AuthenticationScopes<SELF extends AuthenticationHandler> {

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
}
