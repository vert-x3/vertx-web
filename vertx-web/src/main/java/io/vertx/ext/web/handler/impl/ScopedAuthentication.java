package io.vertx.ext.web.handler.impl;

import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Internal interface for scope aware Authentication handlers.
 *
 * @param <SELF>
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public interface ScopedAuthentication<SELF extends AuthenticationHandler> {

  /**
   * Return a new instance with the internal state copied from the caller but the scopes to be requested during a token
   * request are unique to the instance.
   *
   * @param scope scope.
   * @return new instance of this interface.
   */
  SELF withScope(String scope);

  /**
   * Return a new instance with the internal state copied from the caller but the scopes to be requested during a token
   * request are unique to the instance.
   *
   * @param scopes scopes.
   * @return new instance of this interface.
   */
  SELF withScopes(List<String> scopes);

  /**
   * Return the list of scopes provided as the 1st argument, unless the list is empty. In this case, the list of scopes
   * is obtained from the routing context metadata if possible. In case the metadata is not available, the list of
   * scopes is always an empty list.
   */
  default List<String> getScopesOrSearchMetadata(List<String> scopes, RoutingContext ctx) {
    if (!scopes.isEmpty()) {
      return scopes;
    }

    final Route currentRoute = ctx.currentRoute();

    if (currentRoute == null) {
      return Collections.emptyList();
    }

    final Object value = currentRoute
      .metadata()
      .get("scopes");

    if (value == null) {
      return Collections.emptyList();
    }

    if (value instanceof List) {
      return (List<String>) value;
    }

    if (value instanceof String) {
      return Collections.singletonList((String) value);
    }

    throw new IllegalStateException("Invalid type for scopes metadata: " + value.getClass().getName());
  }
}
