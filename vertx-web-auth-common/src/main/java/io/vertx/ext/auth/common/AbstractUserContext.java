package io.vertx.ext.auth.common;

import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.common.HttpException;

public abstract class AbstractUserContext implements UserContextInternal {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractUserContext.class);

  protected AuthenticationContext ctx;
  protected User user;

  @Override
  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public User get() {
    return user;
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

    // remove user from the context
    this.user = null;

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
  public Future<Void> logout() {
    return logout("/");
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

    // remove the current user from the context to avoid any further access
    this.user = null;

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
  public Future<Void> restore() {
    if (!ctx.request().method().equals(HttpMethod.GET)) {
      // we can't automate a redirect to a non-GET request
      return Future.failedFuture(new HttpException(405, "Method not allowed"));
    }
    return restore(ctx.request().absoluteURI());
  }

  @Override
  public Future<Void> restore(String redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri cannot be null");

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
  public Future<Void> logout(String redirectUri) {
    Objects.requireNonNull(redirectUri, "redirectUri cannot be null");

    // clear the user
    user = null;

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
  public void clear() {
    user = null;
  }

}
