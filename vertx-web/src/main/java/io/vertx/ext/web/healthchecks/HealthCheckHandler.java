/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.ext.web.healthchecks;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.healthchecks.CheckResult;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.healthchecks.impl.HealthCheckHandlerImpl;

import java.util.function.Function;

/**
 * A Vert.x Web handler on which you register health check procedure. It computes the outcome status (`UP` or `DOWN`)
 * . When the handler process an HTTP request, it computes the global outcome and build a HTTP response as follows:
 * <p>
 * <ul>
 * <li>204 - status is `UP` but no procedures installed (no payload)</li>
 * <li>200 - status is `UP`, the payload contains the result of the installed procedures</li>
 * <li>503 - status is `DOWN`, the payload contains the result of the installed procedures</li>
 * <li>500 - status is `DOWN`, the payload contains the result of the installed procedures, one of the
 * procedure has failed</li>
 * </ul>
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface HealthCheckHandler extends Handler<RoutingContext> {

  /**
   * Creates an instance of the default implementation of the {@link HealthCheckHandler}.
   * This function creates a new instance of {@link HealthChecks}.
   *
   * @param vertx    the Vert.x instance, must not be {@code null}
   * @param provider the Authentication provider used to authenticate the HTTP request
   * @return the created instance
   */
  static HealthCheckHandler create(Vertx vertx, AuthenticationProvider provider) {
    return new HealthCheckHandlerImpl(vertx, provider);
  }

  /**
   * Creates an instance of the default implementation of the {@link HealthCheckHandler}.
   * This function creates a new instance of {@link HealthChecks}.
   *
   * @param vertx the Vert.x instance, must not be {@code null}
   * @return the created instance
   */
  static HealthCheckHandler create(Vertx vertx) {
    return create(vertx, null);
  }


  /**
   * Creates an instance of the default implementation of the {@link HealthCheckHandler}.
   *
   * @param hc the health checks object to use, must not be {@code null}
   * @return the created instance
   */
  static HealthCheckHandler createWithHealthChecks(HealthChecks hc, AuthenticationProvider provider) {
    return new HealthCheckHandlerImpl(hc, provider);
  }

  /**
   * Creates an instance of the default implementation of the {@link HealthCheckHandler}.
   *
   * @param hc the health checks object to use
   * @return the created instance
   */
  static HealthCheckHandler createWithHealthChecks(HealthChecks hc) {
    return createWithHealthChecks(hc, null);
  }

  /**
   * Registers a health check procedure.
   * <p>
   * The procedure is a {@link Handler} taking a {@link Promise} of {@link Status} as parameter. Procedures are
   * asynchronous, and <strong>must</strong> complete or fail the given {@link Promise}. If the future object is
   * failed, the procedure outcome is considered as `DOWN`. If the future is completed without any object, the
   * procedure outcome is considered as `UP`. If the future is completed with a (not-null) {@link Status}, the
   * procedure outcome is the received status.
   * <p>
   * This method uses a 1s timeout. To configure the timeout use {@link #register(String, long, Handler)}.
   *
   * @param name      the name of the procedure, must not be {@code null} or empty
   * @param procedure the procedure, must not be {@code null}
   * @return the current {@link HealthCheckHandler}
   */
  @Fluent
  HealthCheckHandler register(String name, Handler<Promise<Status>> procedure);

  /**
   * Registers a health check procedure.
   * <p>
   * The procedure is a {@link Handler} taking a {@link Promise} of {@link Status} as parameter. Procedures are
   * asynchronous, and <strong>must</strong> complete or fail the given {@link Promise}. If the future object is
   * failed, the procedure outcome is considered as `DOWN`. If the future is completed without any object, the
   * procedure outcome is considered as `UP`. If the future is completed with a (not-null) {@link Status}, the
   * procedure outcome is the received status.
   *
   * @param name      the name of the procedure, must not be {@code null} or empty
   * @param timeout   the procedure timeout
   * @param procedure the procedure, must not be {@code null}
   * @return the current {@link HealthCheckHandler}
   */
  @Fluent
  HealthCheckHandler register(String name, long timeout, Handler<Promise<Status>> procedure);

  /**
   * Unregisters a procedure.
   *
   * @param name the name of the procedure
   * @return the current {@link HealthCheckHandler}
   */
  @Fluent
  HealthCheckHandler unregister(String name);

  /**
   * Sets a function which will be invoked before the {@link CheckResult} gets written to clients.
   *
   * @param resultMapper the {@link Function} used to manipulate the {@link CheckResult}, {@code null} means no result mapper function enabled.
   * @return the current {@link HealthCheckHandler}
   */
  @Fluent
  HealthCheckHandler resultMapper(Function<CheckResult, Future<CheckResult>> resultMapper);

}
