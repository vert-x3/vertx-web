package io.vertx.ext.web.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;

import java.util.function.Function;

public class SimpleAuthenticationHandlerImpl extends AuthenticationHandlerImpl<NOOPAuthenticationProvider> implements SimpleAuthenticationHandler {

  private Function<RoutingContext, Future<User>> authn;

  public SimpleAuthenticationHandlerImpl() {
    super(new NOOPAuthenticationProvider());
  }

  @Override
  public void authenticate(RoutingContext ctx, Handler<AsyncResult<User>> handler) {
    if (authn != null) {
      authn.apply(ctx)
        .onFailure(err -> {
          if (err instanceof HttpException) {
            handler.handle(Future.failedFuture(err));
          } else {
            handler.handle(Future.failedFuture(new HttpException(401, err)));
          }
        })
        .onSuccess(user -> handler.handle(Future.succeededFuture(user)));
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
