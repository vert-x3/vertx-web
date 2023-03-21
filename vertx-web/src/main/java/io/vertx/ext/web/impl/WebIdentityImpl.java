package io.vertx.ext.web.impl;

import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.WebIdentity;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.HttpException;

import java.util.Objects;

public class WebIdentityImpl implements WebIdentity {

  private static final String USER_SWITCH_KEY = "__vertx.user-switch-ref";
  private static final Logger LOG = LoggerFactory.getLogger(WebIdentity.class);

  private final RoutingContext ctx;

  public WebIdentityImpl(RoutingContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public WebIdentity loginHint(String loginHint) {
    final Session session = ctx.session();

    if (session == null) {
      if (loginHint == null) {
        // Fine, we don't need a session
        return this;
      }
      // we always need a session, otherwise we can't track the state of the previous user
      throw new IllegalStateException("SessionHandler not seen in the route. Sessions are required to keep the state");
    }

    if (loginHint == null) {
      // we're removing the hint if present
      session.remove("login_hint");
    } else {
      session
        .put("login_hint", loginHint);
    }

    return this;
  }

  @Override
  public Future<Void> refresh() {
    if (!ctx.request().method().equals(HttpMethod.GET)) {
      // we can't automate a redirect to a non-GET request
      return Future.failedFuture(new HttpException(405, "Method not allowed"));
    }
    return refresh(ctx.request().absoluteURI());
  }

  @Override
  public Future<Void> refresh(String redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri cannot be null");

    final User user = ctx.user();

    if (user == null) {
      // we need to ensure that we already had a user, otherwise we can't switch
      LOG.debug("Impersonation can only occur after a complete authn flow.");
      return Future.failedFuture(new HttpException(401));
    }

    final Session session = ctx.session();

    if (session != null) {
      // From now on, we're changing the state
      session
        // force a session id regeneration to protect against replay attacks
        .regenerateId();
    }

    // remove user from the context
    ctx
      .setUser(null);

    // we should redirect the UA so this link becomes invalid
    return ctx.response()
      // disable all caching
      .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
      .putHeader("Pragma", "no-cache")
      .putHeader(HttpHeaders.EXPIRES, "0")
      // redirect (when there is no state, redirect to home
      .putHeader(HttpHeaders.LOCATION, redirectUri)
      .setStatusCode(302)
      .end("Redirecting to " + redirectUri + ".");
  }

  @Override
  public Future<Void> impersonate() {
    if (!ctx.request().method().equals(HttpMethod.GET)) {
      // we can't automate a redirect to a non-GET request
      return Future.failedFuture(new HttpException(405, "Method not allowed"));
    }
    return impersonate(ctx.request().absoluteURI());
  }

  @Override
  public Future<Void> impersonate(String redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri cannot be null");

    final User user = ctx.user();

    if (user == null) {
      // we need to ensure that we already had a user, otherwise we can't switch
      LOG.debug("Impersonation can only occur after a complete authn flow.");
      return Future.failedFuture(new HttpException(401));
    }

    final Session session = ctx.session();

    if (session == null) {
      // we always need a session, otherwise we can't track the state of the previous user
      LOG.debug("SessionHandler not seen in the route. Sessions are required to keep the state");
      return Future.failedFuture(new HttpException(500));
    }

    if (session.get(USER_SWITCH_KEY) != null) {
      // we always need a session, otherwise we can't track the state of the previous user
      LOG.debug("Impersonation already in place");
      return Future.failedFuture(new HttpException(400));
    }

    // From now on, we're changing the state
    session
      // move the user out of the context (yet keep it in the session, so we can roll back
      .put(USER_SWITCH_KEY, user)
      // force a session id regeneration to protect against replay attacks
      .regenerateId();

    // remove the current user from the context to avoid any further access
    ctx
      .setUser(null);

    // we should redirect the UA so this link becomes invalid
    return ctx.response()
      // disable all caching
      .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
      .putHeader("Pragma", "no-cache")
      .putHeader(HttpHeaders.EXPIRES, "0")
      // redirect (when there is no state, redirect to home
      .putHeader(HttpHeaders.LOCATION, redirectUri)
      .setStatusCode(302)
      .end("Redirecting to " + redirectUri + ".");
  }

  @Override
  public Future<Void> undo() {
    if (!ctx.request().method().equals(HttpMethod.GET)) {
      // we can't automate a redirect to a non-GET request
      return Future.failedFuture(new HttpException(405, "Method not allowed"));
    }
    return undo(ctx.request().absoluteURI());
  }

  @Override
  public Future<Void> undo(String redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri cannot be null");

    final User user = ctx.user();

    if (user == null) {
      // we need to ensure that we already had a user, otherwise we can't switch
      LOG.debug("Impersonation can only occur after a complete authn flow.");
      return Future.failedFuture(new HttpException(401));
    }

    final Session session = ctx.session();

    if (session == null) {
      // we always need a session, otherwise we can't track the state of the previous user
      LOG.debug("SessionHandler not seen in the route. Sessions are required to keep the state");
      return Future.failedFuture(new HttpException(500));
    }

    if (session.get(USER_SWITCH_KEY) == null) {
      // we always need a session, otherwise we can't track the state of the previous user
      LOG.debug("No previous impersonation in place");
      return Future.failedFuture(new HttpException(400));
    }

    // From now on, we're changing the state
    User previousUser = session.get(USER_SWITCH_KEY);

    session
      // move the user out of the context (yet keep it in the session, so we can rollback
      .remove(USER_SWITCH_KEY);
    // remove the previous hint
    session
      .remove("login_hint");

    session
      // force a session id regeneration to protect against replay attacks
      .regenerateId();

    // restore it to the context
    ctx
      .setUser(previousUser);

    // we should redirect the UA so this link becomes invalid
    return ctx.response()
      // disable all caching
      .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
      .putHeader("Pragma", "no-cache")
      .putHeader(HttpHeaders.EXPIRES, "0")
      // redirect (when there is no state, redirect to home
      .putHeader(HttpHeaders.LOCATION, redirectUri)
      .setStatusCode(302)
      .end("Redirecting to " + redirectUri + ".");
  }
}
