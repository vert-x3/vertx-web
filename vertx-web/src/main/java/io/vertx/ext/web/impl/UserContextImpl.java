package io.vertx.ext.web.impl;

import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.common.AbstractUserContext;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.auth.common.UserContext;
import io.vertx.ext.web.common.HttpException;

public class UserContextImpl extends AbstractUserContext {

  private static final String USER_SWITCH_KEY = "__vertx.user-switch-ref";
  private static final Logger LOG = LoggerFactory.getLogger(UserContext.class);

  private final RoutingContext ctx;

  public UserContextImpl(RoutingContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public UserContext loginHint(String loginHint) {
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
  public Future<Void> refresh(String redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri cannot be null");

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

    return super.refresh(redirectUri);
  }

  @Override
  public Future<Void> impersonate(String redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri cannot be null");

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

    return super.impersonate(redirectUri);
  }

  @Override
  public Future<Void> restore(String redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri cannot be null");

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
    this.user = previousUser;

    return super.restore(redirectUri);
  }

  @Override
  public Future<Void> logout(String redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri cannot be null");

    final Session session = ctx.session();
    // clear the session
    if (session != null) {
      session.destroy();
    }

    return super.logout(redirectUri);
  }

  @Override
  public void clear() {
    final Session session = ctx.session();
    // clear the session
    if (session != null) {
      session.destroy();
    }

    super.clear();
  }
}
