package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.ChainAuthHandler;
import io.vertx.ext.web.handler.SimpleAuthenticationHandler;
import io.vertx.ext.web.handler.impl.AuthenticationScopes;
import io.vertx.ext.web.openapi.RouterBuilderException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
class AuthenticationHandlersStore {

  private static final AuthenticationHandler SUCCESS_HANDLER =
    SimpleAuthenticationHandler.create()
    .authenticate(ctx -> Future.succeededFuture(User.create(new JsonObject())));

  private final Map<String, List<AuthenticationHandler>> securityHandlers;

  AuthenticationHandlersStore() {
    this.securityHandlers = new HashMap<>();
  }

  protected void addAuthnRequirement(String name, AuthenticationHandler handler) {
    securityHandlers.computeIfAbsent(name, k -> new ArrayList<>()).add(handler);
  }

  /**
   * The input array is an OR of different AND security requirements
   *
   * @param securityRequirements
   * @param failOnNotFound
   * @return
   */
  protected AuthenticationHandler solveAuthenticationHandler(JsonArray securityRequirements, boolean failOnNotFound) {
    return orAuths(securityRequirements, failOnNotFound);
  }

  private List<AuthenticationHandler> resolveHandlers(Map.Entry<String, Object> e, boolean failOnNotFound) {
    List<AuthenticationHandler> authenticationHandlers;
    if (failOnNotFound) {
      authenticationHandlers = Optional
        .ofNullable(this.securityHandlers.get(e.getKey()))
        .orElseThrow(() -> RouterBuilderException.createMissingSecurityHandler(e.getKey()));
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

      for (int i = 0; i < authenticationHandlers.size(); i++) {
        if (authenticationHandlers.get(i) instanceof AuthenticationScopes<?>) {
          AuthenticationScopes<?> scopedHandler = (AuthenticationScopes<?>) authenticationHandlers.get(i);
          // this mutates the state, so we replace the list with an updated handler
          AuthenticationHandler updatedHandler = scopedHandler.withScopes(scopes);
          authenticationHandlers.set(i, updatedHandler);
        }
      }
    }

    return authenticationHandlers;
  }

  private AuthenticationHandler andAuths(JsonObject securityRequirements, boolean failOnNotFound) {
    List<AuthenticationHandler> handlers = securityRequirements
      .stream()
      .flatMap(e -> resolveHandlers(e, failOnNotFound).stream())
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

  private AuthenticationHandler orAuths(JsonArray securityRequirements, boolean failOnNotFound) {
    if (securityRequirements == null) {
      return null;
    }

    // Put empty auth at the end
    int hasEmptyAuth = securityRequirements.getList().indexOf(new JsonObject());
    if (hasEmptyAuth != -1) {
      securityRequirements.remove(hasEmptyAuth);
    }

    // Either the list was originally null or was made only by that single empty auth requirement
    if (securityRequirements.size() == 0) {
      return null;
    }

    // If one security requirements, we don't need a ChainAuthHandler
    if (securityRequirements.size() == 1) {
      return andAuths(securityRequirements.getJsonObject(0), failOnNotFound);
    }

    ChainAuthHandler authHandler = ChainAuthHandler.any();
    securityRequirements
      .stream()
      .map(jo -> (JsonObject) jo)
      .map(jo -> andAuths(jo, failOnNotFound))
      .filter(Objects::nonNull)
      .forEach(authHandler::add);

    if (hasEmptyAuth != -1) {
      authHandler.add(SUCCESS_HANDLER);
    }

    return authHandler;
  }

}
