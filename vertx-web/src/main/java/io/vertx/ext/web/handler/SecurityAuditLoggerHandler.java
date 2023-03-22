/*
 * Copyright 2023 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.handler.impl.SecurityAuditLoggerHandlerImpl;

/**
 * A handler that logs security audit events. This handler is to be used with the underlying logger
 * {@code io.vertx.ext.auth.audit}.
 *
 * This handler will ensure that logs are structured and that sensitive data is masked. This information can be used to
 * feed SIEM or EDR/XDR tools to monitor and detect security incidents.
 *
 * @author Paulo Lopes
 */
@VertxGen
public interface SecurityAuditLoggerHandler extends PlatformHandler {

  /**
   * Create a new instance of the handler. For each request the audit data is collected and will be logged explicitly
   * by {@link AuthorizationHandler} and {@link AuthorizationHandler} instances. The handler will also log the final
   * status of the current request. The marker kinds are defined in the enum {@link io.vertx.ext.auth.audit.Marker}.
   */
  static SecurityAuditLoggerHandler create() {
    return new SecurityAuditLoggerHandlerImpl();
  }
}
