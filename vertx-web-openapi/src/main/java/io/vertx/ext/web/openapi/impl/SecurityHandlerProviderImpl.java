package io.vertx.ext.web.openapi.impl;

import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.openapi.SecurityHandlerProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SecurityHandlerProviderImpl implements SecurityHandlerProvider {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityHandlerProviderImpl.class);

  private final Map<String, Function<JsonObject, Future<AuthenticationHandler>>> factories = new ConcurrentHashMap<>();

  @Override
  public SecurityHandlerProvider add(String id, Function<JsonObject, Future<AuthenticationHandler>> factory) {
    Function<JsonObject, Future<AuthenticationHandler>> previous = factories.put(id, factory);
    if (previous != null) {
      LOG.warn("factory for id [" + id + "] got replaced");
    }
    return this;
  }

  public boolean containsSecuritySchemaId(String id) {
    return factories.containsKey(id);
  }

  @Override
  public Future<AuthenticationHandler> build(String id, JsonObject config) {
    if (factories.containsKey(id)) {
      return factories.get(id).apply(config);
    } else {
      return Future.failedFuture("SecuritySchemaId not found: " + id);
    }
  }
}
