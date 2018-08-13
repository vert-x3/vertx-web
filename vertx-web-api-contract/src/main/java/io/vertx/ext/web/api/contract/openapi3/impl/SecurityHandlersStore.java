package io.vertx.ext.web.api.contract.openapi3.impl;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  private List<Handler<RoutingContext>> mapWithFail(SecurityRequirementKey k) throws RouterFactoryException {
    if (k.hasScope())
      return Optional
        .ofNullable((this.securityHandlers.get(k) != null) ? this.securityHandlers.get(k) : this.securityHandlers.get(k.cloneWithoutScope()))
        .orElseThrow(() -> RouterFactoryException.createMissingSecurityHandler(k.getName(), k.getScope()));
    else
      return Optional.ofNullable(this.securityHandlers.get(k)).orElseThrow(() -> RouterFactoryException.createMissingSecurityHandler(k.getName()));
  }

  private List<Handler<RoutingContext>> mapWithoutFail(SecurityRequirementKey k) {
    if (k.hasScope())
      return Optional
        .ofNullable(this.securityHandlers.get(k))
        .orElseGet(() -> this.securityHandlers.get(k.cloneWithoutScope()));
    else
      return this.securityHandlers.get(k);
  }

  protected List<Handler<RoutingContext>> solveSecurityHandlers(List<SecurityRequirement> nonTranslatedKeys, boolean failOnNotFound) {
    List<SecurityRequirementKey> keys = this.translateRequirements(nonTranslatedKeys);
    if (keys != null) {
      if (failOnNotFound)
        return keys.stream().map(this::mapWithFail).flatMap(Collection::stream).collect(Collectors.toList());
      else
        return keys.stream().map(this::mapWithoutFail).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());
    } else
      return new ArrayList<>();
  }

  private List<SecurityRequirementKey> translateRequirements(List<SecurityRequirement> keys) {
    if (keys != null)
      return keys.stream()
        .flatMap(m -> m.entrySet().stream().flatMap(e -> {
          if (e.getValue() == null || e.getValue().size() == 0)
            return Stream.of(new SecurityRequirementKey(e.getKey()));
          else
            return e.getValue().stream().map(s -> new SecurityRequirementKey(e.getKey(), s));
        }))
        .collect(Collectors.toList());
    else
      return new ArrayList<>();
  }

}
