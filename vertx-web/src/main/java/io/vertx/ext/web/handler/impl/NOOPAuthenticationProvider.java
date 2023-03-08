package io.vertx.ext.web.handler.impl;

import io.vertx.core.Future;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;

final class NOOPAuthenticationProvider implements AuthenticationProvider {
  @Override
  public Future<User> authenticate(Credentials credentials) {
    return Future.failedFuture("NOOP Provider does not authenticate");
  }
}
