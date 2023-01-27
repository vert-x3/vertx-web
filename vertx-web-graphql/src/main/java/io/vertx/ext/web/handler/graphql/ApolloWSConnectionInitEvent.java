package io.vertx.ext.web.handler.graphql;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Promise;

/**
 * @deprecated the subscriptions-transport-ws protocol is no longer maintained
 */
@VertxGen
@Deprecated
public interface ApolloWSConnectionInitEvent extends Promise<Object> {
  /**
   * Provides {@link ApolloWSMessageType#CONNECTION_INIT} message content.
   *
   * @return message
   */
  @CacheReturn
  ApolloWSMessage message();
}
