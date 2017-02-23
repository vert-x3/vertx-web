package io.vertx.ext.healthchecks.impl;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
class DefaultProcedure implements Procedure {

  private final Handler<Future<Status>> handler;
  private final String name;

  private final Vertx vertx;
  private final long timeout;

  DefaultProcedure(Vertx vertx, String name, long timeout,
                   Handler<Future<Status>> handler) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(name);
    Objects.requireNonNull(handler);
    this.timeout = timeout;
    this.name = name;
    this.handler = handler;
    this.vertx = vertx;
  }

  @Override
  public void check(Handler<JsonObject> resultHandler) {
    Future<Status> future = Future.<Status>future()
      .setHandler(ar -> {
        if (ar.cause() instanceof ProcedureException) {
          resultHandler.handle(StatusHelper.onError(name, (ProcedureException) ar.cause()));
        } else {
          resultHandler.handle(StatusHelper.from(name, ar));
        }
      });

    if (timeout >= 0) {
      vertx.setTimer(timeout, l -> {
        // attempt to mark the future as failed, NNOP if already completed.
        future.fail(new ProcedureException("Timeout"));
      });
    }

    try {
      handler.handle(future);
    } catch (Exception e) {
      future.fail(new ProcedureException(e));
    }
  }
}
