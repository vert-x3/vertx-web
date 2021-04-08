package io.vertx.ext.web.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;

import java.util.function.Function;

public class SimpleAuthenticationHandlerImpl extends AuthenticationHandlerImpl<NOOPAuthenticationProvider> implements SimpleAuthenticationHandler {

  private Function<RoutingContext, Future<User>> authn;

  public SimpleAuthenticationHandlerImpl() {
    super(new NOOPAuthenticationProvider());
  }

  @Override
  public void parseCredentials(RoutingContext ctx, Handler<AsyncResult<Credentials>> handler) {
    if (authn != null) {
      authn.apply(ctx)
        .onFailure(err -> handler.handle(Future.failedFuture(err)))
        .onSuccess(user -> {
          ctx.setUser(user);
          handler.handle(Future.succeededFuture());
        });
    } else {
      handler.handle(Future.failedFuture("No Authenticate function"));
    }
  }

  @Override
  public SimpleAuthenticationHandlerImpl authenticate(Function<RoutingContext, Future<User>> authnFunction) {
    this.authn = authnFunction;
    return this;
  }
}
