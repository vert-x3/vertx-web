package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.handler.graphql.impl.GraphQLSocketHandlerImpl;

public interface GraphQLSocketHandler extends Handler<ServerWebSocket> {

  GraphQLSocketHandler endHandler(Handler<ServerWebSocket> endHandler);

  static GraphQLSocketHandler create(GraphQL graphQL) {
    return new GraphQLSocketHandlerImpl(graphQL);
  }

}
