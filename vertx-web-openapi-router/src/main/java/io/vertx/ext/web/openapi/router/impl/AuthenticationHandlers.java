package io.vertx.ext.web.openapi.router.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.ChainAuthHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;
import io.vertx.ext.web.handler.impl.ScopedAuthentication;
import io.vertx.openapi.contract.OpenAPIContract;
import io.vertx.openapi.contract.Operation;
import io.vertx.openapi.contract.SecurityRequirement;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
class AuthenticationHandlers {

  private static final JsonObject EMPTY_JSON = new JsonObject(Collections.emptyMap());

  private static final AuthenticationHandler SUCCESS_HANDLER =
    SimpleAuthenticationHandler
      .create()
      .authenticate(ctx -> Future.succeededFuture(User.create(EMPTY_JSON)));

  private final Map<String, List<AuthenticationHandler>> securityHandlers;
  private final Map<String, OAuth2AuthHandler> callbackHandlers;

  AuthenticationHandlers() {
    this.securityHandlers = new HashMap<>();
    this.callbackHandlers = new HashMap<>();
  }

  protected void addRequirement(String name, AuthenticationHandler handler, String callback) {
    securityHandlers
      .computeIfAbsent(name, k -> new ArrayList<>())
      .add(handler);

    if (callback != null) {
      // TODO: check if callback is already present
      callbackHandlers.put(callback, (OAuth2AuthHandler) handler);
    }
  }

  /**
   * The input array is an OR of different AND security requirements
   */
  protected void solve(OpenAPIContract contract, Operation operation, Route route, boolean failOnNotFound) {
    final List<SecurityRequirement> securityRequirements =
      operation.getSecurityRequirements() != null ?
        operation.getSecurityRequirements() :
        contract.getSecurityRequirements();

    AuthenticationHandler authn = or(
      route,
      // we may modify the list, so we need to copy it
      securityRequirements == null ? Collections.emptyList() : new ArrayList<>(securityRequirements),
      failOnNotFound);

    if (authn != null) {
      route.handler(authn);
    }
  }

  private List<AuthenticationHandler> resolveHandlers(Route route, String name, List<String> scopes, boolean failOnNotFound) {
    List<AuthenticationHandler> authenticationHandlers;
    if (failOnNotFound) {
      authenticationHandlers = Optional
        .ofNullable(this.securityHandlers.get(name))
        .orElseThrow(() -> new IllegalStateException("Missing security handler for: '" + name + "'"));
    } else {
      authenticationHandlers = Optional
        .ofNullable(this.securityHandlers.get(name))
        .orElse(Collections.emptyList());
    }

    // Some scopes are defines, we need to configure them in OAuth2Handlers
    if (!scopes.isEmpty()) {
      route.putMetadata("scopes", scopes);

      // Update the returned list to have handlers with the required scopes
      authenticationHandlers = authenticationHandlers
        .stream()
        .map(authHandler -> {
          if (authHandler instanceof ScopedAuthentication<?>) {
            return ((ScopedAuthentication<?>) authHandler).withScopes(scopes);
          } else {
            return authHandler;
          }
        })
        .collect(Collectors.toList());
    }

    return authenticationHandlers;
  }

  private AuthenticationHandler and(Route route, SecurityRequirement securityRequirement, boolean failOnNotFound) {
    List<AuthenticationHandler> handlers = securityRequirement.getNames()
      .stream()
      .flatMap(name -> resolveHandlers(route, name, securityRequirement.getScopes(name), failOnNotFound).stream())
      .collect(Collectors.toList());

    if (handlers.isEmpty()) {
      return null;
    }

    if (handlers.size() == 1) {
      return handlers.get(0);
    }

    ChainAuthHandler authHandler = ChainAuthHandler.all();
    handlers.forEach(authHandler::add);

    return authHandler;
  }

  private AuthenticationHandler or(Route route, List<SecurityRequirement> securityRequirements, boolean failOnNotFound) {
    if (securityRequirements == null || securityRequirements.isEmpty()) {
      return null;
    }

    boolean emptyAuth = false;
    for (int i = 0; i < securityRequirements.size(); i++) {
      if (securityRequirements.get(i).isEmpty()) {
        emptyAuth = true;
        securityRequirements.remove(i);
        break;
      }
    }

    ChainAuthHandler authHandler;

    switch (securityRequirements.size()) {
      case 0:
        return SUCCESS_HANDLER;
      case 1:
        if (!emptyAuth) {
          // If one security requirements, we don't need a ChainAuthHandler
          return and(route, securityRequirements.get(0), failOnNotFound);
        }
      default:
        authHandler = ChainAuthHandler.any();
        securityRequirements
          .stream()
          .map(securityRequirement -> and(route, securityRequirement, failOnNotFound))
          .filter(Objects::nonNull)
          .forEach(authHandler::add);
    }

    if (emptyAuth) {
      authHandler.add(SUCCESS_HANDLER);
    }

    return authHandler;
  }

  public void applyCallbackHandlers(Router router) {
    callbackHandlers.forEach((path, handler) -> {
      handler.setupCallback(router.get(path));
    });
  }
}
