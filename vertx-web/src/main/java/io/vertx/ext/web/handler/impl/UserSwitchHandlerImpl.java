package io.vertx.ext.web.handler.impl;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.UserSwitchHandler;

public class UserSwitchHandlerImpl implements UserSwitchHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UserSwitchHandlerImpl.class);

  private final boolean impersonate;

  public UserSwitchHandlerImpl(boolean impersonate) {
    this.impersonate = impersonate;
  }

  @Override
  public void handle(RoutingContext ctx) {

    final User user = ctx.user();

    if (user == null) {
      // we need to ensure that we already had a user, otherwise we can't switch
      LOG.debug("Impersonation can only occur after a complete authn flow.");
      ctx.fail(401);
      return;
    }

    final Session session = ctx.session();

    if (session == null) {
      // we always need a session, otherwise we can't track the state of the previous user
      LOG.debug("SessionHandler not seen in the route. Sessions are required to keep the state");
      ctx.fail(500);
      return;
    }

    if (impersonate) {
      if (session.get(USER_SWITCH_KEY) != null) {
        // we always need a session, otherwise we can't track the state of the previous user
        LOG.debug("Impersonation already in place");
        ctx.fail(400);
        return;
      }

      // extract options from query string
      String redirectUri = ctx.request().params().get("redirect_uri");

      if (redirectUri == null) {
        LOG.info("Invalid or missing redirect_uri");
        ctx.fail(400);
        return;
      }

      // From now on, we're changing the state

      session
        // move the user out of the context (yet keep it in the session, so we can rollback
        .put(USER_SWITCH_KEY, user)
        // force a session id regeneration to protect against replay attacks
        .regenerateId();

      String loginHint = ctx.request().params().get("login_hint");

      if (loginHint != null) {
        session
          .put("login_hint", loginHint);
      }

      // remove it from the context
      ctx
        .setUser(null);

      // we should redirect the UA so this link becomes invalid
      ctx.response()
        // disable all caching
        .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
        .putHeader("Pragma", "no-cache")
        .putHeader(HttpHeaders.EXPIRES, "0")
        // redirect (when there is no state, redirect to home
        .putHeader(HttpHeaders.LOCATION, redirectUri)
        .setStatusCode(302)
        .end("Redirecting to " + redirectUri + ".");

    } else {
      // we're undo'ing the impersonation

      if (session.get(USER_SWITCH_KEY) == null) {
        // we always need a session, otherwise we can't track the state of the previous user
        LOG.debug("No previous impersonation in place");
        ctx.fail(400);
        return;
      }

      // extract options from query string
      String redirectUri = ctx.request().params().get("redirect_uri");

      if (redirectUri == null) {
        LOG.info("Invalid or missing redirect_uri");
        ctx.fail(400);
        return;
      }

      // From now on, we're changing the state
      User previousUser = session.get(USER_SWITCH_KEY);

      session
        // move the user out of the context (yet keep it in the session, so we can rollback
        .remove(USER_SWITCH_KEY);
      session
        // force a session id regeneration to protect against replay attacks
        .regenerateId();

      // restore it to the context
      ctx
        .setUser(previousUser);

      // we should redirect the UA so this link becomes invalid
      ctx.response()
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
}
