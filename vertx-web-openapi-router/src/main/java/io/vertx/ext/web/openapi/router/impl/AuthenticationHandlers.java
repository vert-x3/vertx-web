package io.vertx.ext.web.openapi.router.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
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
    final JsonArray globalSecurity = contract.getRawContract()
      .getJsonArray("security");
    final JsonArray operationSecurity = operation.getOpenAPIModel()
      .getJsonArray("security");

    final JsonArray securityRequirements = operationSecurity != null ? operationSecurity : globalSecurity;

    AuthenticationHandler authn = or(route, securityRequirements, failOnNotFound);
    if (authn != null) {
      route.handler(authn);
    }
  }

  private List<AuthenticationHandler> resolveHandlers(Route route, Map.Entry<String, Object> e, boolean failOnNotFound) {
    List<AuthenticationHandler> authenticationHandlers;
    if (failOnNotFound) {
      authenticationHandlers = Optional
        .ofNullable(this.securityHandlers.get(e.getKey()))
        .orElseThrow(() -> new IllegalStateException("Missing security handler for: '" + e.getKey() + "'"));
    } else {
      authenticationHandlers = Optional
        .ofNullable(this.securityHandlers.get(e.getKey()))
        .orElse(Collections.emptyList());
    }

    // Some scopes are defines, we need to configure them in OAuth2Handlers
    if (e.getValue() instanceof JsonArray && ((JsonArray) e.getValue()).size() != 0) {
      List<String> scopes = ((JsonArray) e.getValue())
        .stream()
        .map(v -> (String) v)
        .collect(Collectors.toList());

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

  private AuthenticationHandler and(Route route, JsonObject securityRequirements, boolean failOnNotFound) {
    List<AuthenticationHandler> handlers = securityRequirements
      .stream()
      .flatMap(e -> resolveHandlers(route, e, failOnNotFound).stream())
      .collect(Collectors.toList());

    if (handlers.size() == 0) {
      return null;
    }

    if (handlers.size() == 1) {
      return handlers.get(0);
    }

    ChainAuthHandler authHandler = ChainAuthHandler.all();
    handlers.forEach(authHandler::add);

    return authHandler;
  }

  private AuthenticationHandler or(Route route, JsonArray securityRequirements, boolean failOnNotFound) {
    if (securityRequirements == null || securityRequirements.size() == 0) {
      return null;
    }

    boolean emptyAuth = false;
    for (int i = 0; i < securityRequirements.size(); i++) {
      if (EMPTY_JSON.equals(securityRequirements.getValue(i))) {
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
          return and(route, securityRequirements.getJsonObject(0), failOnNotFound);
        }
      default:
        authHandler = ChainAuthHandler.any();
        securityRequirements
          .stream()
          .map(json -> and(route, (JsonObject) json, failOnNotFound))
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