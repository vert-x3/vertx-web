package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Francesco Guardiani @slinkydeveloper
 */
class SecurityHandlersStore {

  private class SecurityRequirementKey {
    private String name;
    private String oauth2Scope;

    public SecurityRequirementKey(String name, String oauth2Scope) {
      this.name = name;
      this.oauth2Scope = oauth2Scope;
    }

    public SecurityRequirementKey(String name) {
      this(name, null);
    }

    public String getName() {
      return name;
    }

    public String getScope() {
      return oauth2Scope;
    }

    public boolean hasScope() {
      return oauth2Scope != null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SecurityRequirementKey that = (SecurityRequirementKey) o;

      if (!name.equals(that.name)) return false;
      return oauth2Scope != null ? oauth2Scope.equals(that.oauth2Scope) : that.oauth2Scope == null;
    }

    @Override
    public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + (oauth2Scope != null ? oauth2Scope.hashCode() : 0);
      return result;
    }

    private SecurityRequirementKey cloneWithoutScope() {
      return new SecurityRequirementKey(this.getName());
    }
  }

  private final Map<SecurityRequirementKey, List<Handler<RoutingContext>>> securityHandlers;

  SecurityHandlersStore() {
    this.securityHandlers = new HashMap<>();
  }

  protected void addSecurityRequirement(String name, String scope, Handler<RoutingContext> handler) {
    securityHandlers.computeIfAbsent(new SecurityRequirementKey(name, scope), k -> new ArrayList<>()).add(handler);
  }

  protected void addSecurityRequirement(String name, Handler<RoutingContext> handler) {
    securityHandlers.computeIfAbsent(new SecurityRequirementKey(name), k -> new ArrayList<>()).add(handler);
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

  protected List<Handler<RoutingContext>> solveSecurityHandlers(JsonArray nonTranslatedKeys, boolean failOnNotFound) {
    return new ArrayList<>();
  }

}
