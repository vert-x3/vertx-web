package io.vertx.ext.web.openapi.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.ChainAuthHandler;
import io.vertx.ext.web.handler.impl.AuthenticationHandlerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
class AuthenticationHandlersStore {

  private static final AuthenticationHandler SUCCESS_HANDLER = new AuthenticationHandlerImpl((authInfo, resultHandler) -> resultHandler.handle(Future.succeededFuture(User.create(new JsonObject())))) {
    @Override
    public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
      handler.handle(Future.succeededFuture(new JsonObject()));
    }
  };

  private final Map<String, List<AuthenticationHandler>> securityHandlers;

  AuthenticationHandlersStore() {
    this.securityHandlers = new HashMap<>();
  }

  protected void addAuthnRequirement(String name, AuthenticationHandler handler) {
    securityHandlers.computeIfAbsent(name, k -> new ArrayList<>()).add(handler);
  }

//  private List<Handler<RoutingContext>> mapWithFail(List<JsonObject> k) throws RouterFactoryException {
//    if (k.hasScope())
//      return Optional
//        .ofNullable((this.securityHandlers.get(k) != null) ? this.securityHandlers.get(k) : this.securityHandlers.get(k.cloneWithoutScope()))
//        .orElseThrow(() -> RouterFactoryException.createMissingSecurityHandler(k.getName(), k.getScope()));
//    else
//      return Optional.ofNullable(this.securityHandlers.get(k)).orElseThrow(() -> RouterFactoryException.createMissingSecurityHandler(k.getName()));
//  }
//
//  private List<Handler<RoutingContext>> mapWithoutFail(SecurityRequirementKey k) {
//    if (k.hasScope())
//      return Optional
//        .ofNullable(this.securityHandlers.get(k))
//        .orElseGet(() -> this.securityHandlers.get(k.cloneWithoutScope()));
//    else
//      return this.securityHandlers.get(k);
//  }
//
//  protected List<Handler<RoutingContext>> solveSecurityHandlers(JsonArray nonTranslatedKeys, boolean failOnNotFound) {
//    //TODO how do we manage and/or auths problem?
//    List<List<JsonObject>> securityRequirements = this.translateRequirements(nonTranslatedKeys);
//    if (failOnNotFound)
//      return securityRequirements.stream().map(this::mapWithFail).flatMap(Collection::stream).collect(Collectors.toList());
//    else
//      return securityRequirements.stream().map(this::mapWithoutFail).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
//  }
//
//  private List<List<JsonObject>> translateRequirements(JsonArray keys) {
//    if (keys != null)
//      return keys.stream()
//        .flatMap(m -> m.entrySet().stream().flatMap(e -> {
//          if (e.getValue() == null || e.getValue().size() == 0)
//            return Stream.of(new SecurityRequirementKey(e.getKey()));
//          else
//            return e.getValue().stream().map(s -> new SecurityRequirementKey(e.getKey(), s));
//        }))
//        .collect(Collectors.toList());
//    else
//      return new ArrayList<>();
//  }

  private AuthenticationHandler andAuths(JsonObject securityRequirements, boolean failOnNotFound) {
    return null;
  }

  private AuthenticationHandler orAuths(JsonArray securityRequirements, boolean failOnNotFound) {
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
      .map(jo -> (JsonObject)jo)
      .forEach(jo -> authHandler.add(andAuths(jo, failOnNotFound)));

    if (hasEmptyAuth != -1) {
      authHandler.add(SUCCESS_HANDLER);
    }

    return authHandler;
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

}
