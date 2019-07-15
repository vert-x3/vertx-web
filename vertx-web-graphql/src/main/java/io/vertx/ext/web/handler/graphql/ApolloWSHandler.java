package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.handler.graphql.impl.GraphQLSocketHandlerImpl;

/**
 * A websocket {@link io.vertx.core.Handler} for GraphQL requests.
 *
 * @author Rogelio Orts
 */
@VertxGen
public interface GraphQLSocketHandler extends Handler<ServerWebSocket> {

  /**
   * Customize the end {@link Handler}.
   * This handler will be called at the end of each websocket connection.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  GraphQLSocketHandler endHandler(Handler<ServerWebSocket> endHandler);

  /**
   * Create a new {@link GraphQLSocketHandler} that will use the provided {@code graphQL} object to execute queries.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static GraphQLSocketHandler create(GraphQL graphQL) {
    return new GraphQLSocketHandlerImpl(graphQL);
  }

}
