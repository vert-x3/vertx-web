package io.vertx.ext.web.handler.graphql;

import graphql.GraphQL;
import io.vertx.core.Handler;
import io.vertx.ext.web.handler.graphql.impl.GraphQLSocketHandlerImpl;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

public interface GraphQLSocketHandler extends Handler<SockJSSocket> {

  GraphQLSocketHandler exceptionHandler(Handler<Throwable> exceptionHandler);

  GraphQLSocketHandler completeHandler(Handler<SockJSSocket> completeHandler);

  GraphQLSocketHandler endHandler(Handler<SockJSSocket> endHandler);

  static GraphQLSocketHandler create(GraphQL graphQL) {
    return new GraphQLSocketHandlerImpl(graphQL);
  }

}
