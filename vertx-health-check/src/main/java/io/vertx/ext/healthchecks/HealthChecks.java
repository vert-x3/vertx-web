package io.vertx.ext.healthchecks;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.impl.HealthChecksImpl;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface HealthChecks {

  /**
   * Creates a new instance of the default implementation of {@link HealthChecks}.
   *
   * @param vertx the instance of Vert.x, must not be {@code null}
   * @return the created instance
   */
  static HealthChecks create(Vertx vertx) {
    return new HealthChecksImpl(vertx);
  }

  /**
   * Registers a health check procedure.
   * <p>
   * The procedure is a {@link Handler} taking a {@link Future} of {@link Status} as parameter.
   * Procedures are asynchronous, and <strong>must</strong> complete or fail the given {@link Future}.
   * If the future object is failed, the procedure outcome is considered as `DOWN`. If the future is
   * completed without any object, the procedure outcome is considered as `UP`. If the future is completed
   * with a (not-null) {@link Status}, the procedure outcome is the received status.
   *
   * @param name      the name of the procedure, must not be {@code null} or empty
   * @param procedure the procedure, must not be {@code null}
   * @return the current {@link HealthChecks}
   */
  @Fluent
  HealthChecks register(String name, Handler<Future<Status>> procedure);

  /**
   * Unregisters a procedure.
   *
   * @param name the name of the procedure
   * @return the current {@link HealthChecks}
   */
  @Fluent
  HealthChecks unregister(String name);


  /**
   * Invokes the registered procedures and computes the outcome.
   *
   * @param resultHandler the result handler, must not be {@code null}. The handler received the computed
   *                      {@link JsonObject}.
   * @return the current {@link HealthChecks}
   */
  @Fluent
  HealthChecks invoke(Handler<JsonObject> resultHandler);


  /**
   * Invokes the registered procedure with the given name and sub-procedures. It computes the overall
   * outcome.
   *
   * @param resultHandler the result handler, must not be {@code null}. The handler received an
   *                      {@link AsyncResult} marked as failed if the procedure with the given name cannot
   *                      be found or invoked.
   * @return the current {@link HealthChecks}
   */
  @Fluent
  HealthChecks invoke(String name, Handler<AsyncResult<JsonObject>> resultHandler);

}
