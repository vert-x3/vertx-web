package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.handler.impl.ChainAuthHandlerImpl;

public interface ChainAuthHandler extends AuthHandler {

  static ChainAuthHandler create() {
    return new ChainAuthHandlerImpl();
  }

  /**
   * Appends a auth provider to the chain.
   *
   * @param other auth handler
   * @return self
   *
   */
  @Fluent
  ChainAuthHandler append(AuthHandler other);

  /**
   * Removes a provider from the chain.
   *
   * @param other provider to remove
   * @return true if provider was removed, false if non existent in the chain.
   */
  boolean remove(AuthHandler other);

  /**
   * Clears the chain.
   */
  void clear();
}
