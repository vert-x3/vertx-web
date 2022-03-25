package io.vertx.ext.web.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;

import java.util.ArrayList;
import java.util.List;

public class ChainAuthHandlerImpl extends AuthenticationHandlerImpl<AuthenticationProvider> implements ChainAuthHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ChainAuthHandler.class);

  private final List<AuthenticationHandlerInternal> handlers = new ArrayList<>();
  private final boolean all;

  private int willRedirect = -1;

  public ChainAuthHandlerImpl(boolean all) {
    super(null);
    this.all = all;
  }

  @Override
  public boolean performsRedirect() {
    return willRedirect != -1;
  }

  @Override
  public synchronized ChainAuthHandler add(AuthenticationHandler other) {
    if (performsRedirect()) {
      throw new IllegalStateException("Cannot add a handler after a handler known to perform a HTTP redirect: " + handlers.get(willRedirect));
    }
    final AuthenticationHandlerInternal otherInternal = (AuthenticationHandlerInternal) other;
    // control if we should not allow more handlers due to the possibility of a redirect to happen
    if (otherInternal.performsRedirect()) {
      willRedirect = handlers.size();
    }
    handlers.add(otherInternal);
    return this;
  }

  @Override
  public void authenticate(RoutingContext context, Handler<AsyncResult<User>> handler) {
    if (handlers.size() == 0) {
      handler.handle(Future.failedFuture("No providers in the auth chain."));
    } else {
      // iterate all possible authN
      iterate(0, context, null, null, handler);
    }
  }

  private void iterate(final int idx, final RoutingContext ctx, User result, Throwable exception, Handler<AsyncResult<User>> handler) {
    // stop condition
    if (idx >= handlers.size()) {
      if (all) {
        // no more providers, if the call is signaling an error we fail as the last handler failed
        if (exception == null) {
          handler.handle(Future.succeededFuture(result));
        } else {
          handler.handle(Future.failedFuture(exception));
        }
      } else {
        // no more providers, means that we failed to find a provider capable of performing this operation
        handler.handle(Future.failedFuture(exception));
      }
      return;
    }

    // parse the request in order to extract the credentials object
    final AuthenticationHandlerInternal authHandler = handlers.get(idx);

    authHandler.authenticate(ctx, res -> {
      if (res.failed()) {
        if (all) {
          // all handlers need to be valid, a single failure is enough to
          // abort the execution of the chain
        } else {
          // any handler can be valid, if the response is within a validation error
          // the chain is allowed to proceed, otherwise we must abort.
          if (res.cause() instanceof HttpException) {
            final HttpException ex = (HttpException) res.cause();
            switch (ex.getStatusCode()) {
              case 302:
              case 400:
              case 401:
              case 403:
                // try again with next provider since we know what kind of error it is
                iterate(idx + 1, ctx, null, ex, handler);
                return;
            }
          }
          // the error is not a validation exception, so we abort regardless
        }
        handler.handle(Future.failedFuture(res.cause()));
        return;
      }

      if (all) {
        // this handler is succeeded, but as we need all, we must continue with
        // the iteration of the remaining handlers.
        iterate(idx + 1, ctx, res.result(), null, handler);
      } else {
        // a single success is enough to signal the end of the validation
        handler.handle(Future.succeededFuture(res.result()));
      }
    });
  }

  @Override
  public boolean setAuthenticateHeader(RoutingContext ctx) {
    boolean added = false;
    for (AuthenticationHandlerInternal authHandler : handlers) {
      if (all && added) {
        // we can only allow 1 header in this case,
        // otherwise we tell the user agent to pick the strongest,
        // yet we want them all
        LOG.warn("Multiple WWW-Authenticate headers will be suppressed on a ALL chain");
        break;
      }
      added |= authHandler.setAuthenticateHeader(ctx);
    }
    return added;
  }
}
