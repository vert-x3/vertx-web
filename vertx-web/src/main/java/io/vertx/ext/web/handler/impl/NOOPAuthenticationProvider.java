package io.vertx.ext.web.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;

final class NOOPAuthenticationProvider implements AuthenticationProvider {
  @Override
  public void authenticate(JsonObject credentials, Handler<AsyncResult<User>> handler) {
    handler.handle(Future.failedFuture("NOOP Provider does not authenticate"));
  }
}
