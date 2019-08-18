package io.vertx.ext.web.handler.graphql;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.impl.ApolloWSMessageType;

public class ApolloWSContext {

  private final RoutingContext routingContext;

  private final ServerWebSocket serverWebSocket;

  private final ApolloWSMessageType type;

  private final JsonObject message;

  public ApolloWSContext(
      RoutingContext routingContext, ServerWebSocket serverWebSocket, ApolloWSMessageType type,
      JsonObject message) {
    this.routingContext = routingContext;
    this.serverWebSocket = serverWebSocket;
    this.type = type;
    this.message = message;
  }

  public RoutingContext getRoutingContext() {
    return routingContext;
  }

  public ServerWebSocket getServerWebSocket() {
    return serverWebSocket;
  }

  public ApolloWSMessageType getType() {
    return type;
  }

  public JsonObject getMessage() {
    return message;
  }

}
