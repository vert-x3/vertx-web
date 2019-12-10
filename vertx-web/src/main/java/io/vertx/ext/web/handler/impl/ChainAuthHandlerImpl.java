package io.vertx.ext.web.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.ChainAuthHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChainAuthHandlerImpl extends AuthHandlerImpl implements ChainAuthHandler {

  private final List<AuthenticationHandler> handlers = new ArrayList<>();

  public ChainAuthHandlerImpl() {
    super(null);
  }

  @Override
  public ChainAuthHandler add(AuthenticationHandler other) {
    handlers.add(other);
    return this;
  }

  @Override
  public AuthHandler addAuthority(String authority) {
    return this;
  }

  @Override
  public AuthHandler addAuthorities(Set<String> authorities) {
    return this;
  }

  @Override
  public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
    // iterate all possible authN
    iterate(0, context, null, handler);
  }

  private void iterate(final int idx, final RoutingContext ctx, HttpStatusException lastException, Handler<AsyncResult<JsonObject>> handler) {
    // stop condition
    if (idx >= handlers.size()) {
      // no more providers, means that we failed to find a provider capable of performing this operation
      handler.handle(Future.failedFuture(lastException));
      return;
    }

    // parse the request in order to extract the credentials object
    final AuthenticationHandler authHandler = handlers.get(idx);

    authHandler.parseCredentials(ctx, res -> {
      if (res.failed()) {
        if (res.cause() instanceof HttpStatusException) {
          final HttpStatusException exception = (HttpStatusException) res.cause();
          switch (exception.getStatusCode()) {
            case 302:
            case 400:
            case 401:
            case 403:
              // try again with next provider since we know what kind of error it is
              iterate(idx + 1, ctx, exception, handler);
              return;
          }
        }
        handler.handle(Future.failedFuture(res.cause()));
        return;
      }

      // setup the desired auth provider if we can
      if (authHandler instanceof AuthHandlerImpl) {
        ctx.put(AuthHandlerImpl.AUTH_PROVIDER_CONTEXT_KEY, ((AuthHandlerImpl) authHandler).authProvider);
      }
      handler.handle(Future.succeededFuture(res.result()));
    });
  }

  @Override
  protected String authenticateHeader(RoutingContext ctx) {
    AuthenticationHandler authHandler = handlers.get(handlers.size()-1);
    if (authHandler instanceof AuthHandlerImpl) {
      return ((AuthHandlerImpl) authHandler).authenticateHeader(ctx);
    }
    return null;
  }
}


