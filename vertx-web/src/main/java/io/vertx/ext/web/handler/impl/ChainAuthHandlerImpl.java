package io.vertx.ext.web.handler.impl;

import io.vertx.core.*;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.ChainAuthHandler;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.impl.RoutingContextDecorator;
import io.vertx.ext.web.impl.RoutingContextInternal;
import io.vertx.ext.web.impl.UserContextInternal;

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
  public Future<User> authenticate(RoutingContext context) {
    if (handlers.isEmpty()) {
      return Future.failedFuture("No providers in the auth chain.");
    } else {
      // iterate all possible authN
      Promise<User> promise = Promise.promise();
      iterate(0, context, null, null, promise);
      return promise.future();
    }
  }

  private void iterate(final int idx, final RoutingContext ctx, User result, Throwable exception, Completable<User> handler) {
    // stop condition
    if (idx >= handlers.size()) {
      if (all) {
        // no more providers, if the call is signaling an error we fail as the last handler failed
        handler.complete(result, exception);
      } else {
        // no more providers, means that we failed to find a provider capable of performing this operation
        handler.complete(null, exception);
      }
      return;
    }

    // parse the request in order to extract the credentials object
    final AuthenticationHandlerInternal authHandler = handlers.get(idx);

    authHandler
      .authenticate(ctx)
      .onFailure(err -> {
        if (all) {
          // all handlers need to be valid, a single failure is enough to
          // abort the execution of the chain
          handler.complete(null, err);
        } else {
          // any handler can be valid, if the response is within a validation error
          // the chain is allowed to proceed, otherwise we must abort.
          if (isRecoverable(err)) {
            // try again with next provider since we know what kind of error it is
            iterate(idx + 1, ctx, null, err, handler);
            return;
          }
          // the error is not a validation exception, so we abort regardless
          handler.complete(null, err);
        }
      })
      .onSuccess(user -> {
        if (all) {
          // this handler is succeeded, but as we need all, we must continue with
          // the iteration of the remaining handlers.
          iterate(idx + 1, ctx, user, null, handler);
          return;
        }
        // `any` mode: a successful authenticate is not enough on its own. The handler's
        // postAuthentication step (e.g. JWT scope check) may still reject the request.
        // Run it now against a context wrapper that captures next()/fail(...) so we can
        // continue iterating to the next handler instead of stopping at the first one
        // that authenticates. See issue #2691.
        runPostAuthentication(idx, ctx, authHandler, user, handler);
      });
  }

  private void runPostAuthentication(final int idx, final RoutingContext ctx,
                                     final AuthenticationHandlerInternal authHandler,
                                     final User user, final Completable<User> handler) {
    // postAuthentication implementations rely on ctx.user() to return the authenticated
    // user. The framework normally sets it after authenticate() succeeds; do it here so
    // the inner handler observes the same state.
    ((UserContextInternal) ctx.userContext()).setUser(user);

    final PostAuthenticationCapture wrapper = new PostAuthenticationCapture(ctx);
    try {
      authHandler.postAuthentication(wrapper);
    } catch (RuntimeException e) {
      wrapper.completion.tryFail(e);
    }
    wrapper.completion.future().onComplete(o -> {
      if (o.succeeded() && Boolean.TRUE.equals(o.result())) {
        // postAuthentication called next() -> this handler fully accepts the request
        handler.complete(user, null);
        return;
      }
      // postAuthentication called fail(...) (or threw): treat it like an authenticate
      // failure and try the next handler in the chain when the status is recoverable.
      final int sc = wrapper.statusCode;
      final Throwable f = o.failed() ? o.cause() : wrapper.failure;
      final Throwable err = f != null ? f : new HttpException(sc != 0 ? sc : 403);
      if (isRecoverable(err) || isRecoverableStatus(sc)) {
        iterate(idx + 1, ctx, null, err, handler);
      } else {
        handler.complete(null, err);
      }
    });
  }

  private static boolean isRecoverable(Throwable err) {
    if (err instanceof HttpException) {
      return isRecoverableStatus(((HttpException) err).getStatusCode());
    }
    return false;
  }

  private static boolean isRecoverableStatus(int sc) {
    switch (sc) {
      case 302:
      case 400:
      case 401:
      case 403:
        return true;
      default:
        return false;
    }
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

  @Override
  public void postAuthentication(RoutingContext ctx) {
    // In `any` mode the matched handler's postAuthentication already ran (and succeeded)
    // during authenticate(). In `all` mode there is no per-handler postAuthentication
    // chained here either. So we just continue with the router pipeline.
    ctx.next();
  }

  /**
   * Decorator that intercepts {@link RoutingContext#next()} and {@link RoutingContext#fail}
   * calls performed by an inner {@code postAuthentication} so the chain can decide whether
   * to advance to the next handler instead of immediately failing the request.
   */
  private static final class PostAuthenticationCapture extends RoutingContextDecorator {

    final Promise<Boolean> completion = Promise.promise();
    int statusCode = 0;
    Throwable failure;

    PostAuthenticationCapture(RoutingContext ctx) {
      super(ctx.currentRoute(), (RoutingContextInternal) ctx);
    }

    @Override
    public void next() {
      completion.tryComplete(Boolean.TRUE);
    }

    @Override
    public void fail(int statusCode) {
      this.statusCode = statusCode;
      completion.tryComplete(Boolean.FALSE);
    }

    @Override
    public void fail(Throwable throwable) {
      this.failure = throwable;
      completion.tryComplete(Boolean.FALSE);
    }

    @Override
    public void fail(int statusCode, Throwable throwable) {
      this.statusCode = statusCode;
      this.failure = throwable;
      completion.tryComplete(Boolean.FALSE);
    }
  }
}
