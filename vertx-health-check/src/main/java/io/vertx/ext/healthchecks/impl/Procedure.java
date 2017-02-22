package io.vertx.ext.healthchecks.impl;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface Procedure {

  void check(Handler<JsonObject> resultHandler);

}
