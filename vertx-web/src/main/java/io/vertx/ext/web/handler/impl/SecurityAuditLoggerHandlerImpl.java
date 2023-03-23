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
package io.vertx.ext.web.handler.impl;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.auth.audit.Marker;
import io.vertx.ext.auth.audit.SecurityAudit;
import io.vertx.ext.auth.audit.impl.SecurityAuditLogger;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.SecurityAuditLoggerHandler;
import io.vertx.ext.web.impl.RoutingContextInternal;

public class SecurityAuditLoggerHandlerImpl implements SecurityAuditLoggerHandler {

  public SecurityAuditLoggerHandlerImpl() {
    if (!SecurityAuditLogger.isEnabled()) {
      throw new IllegalStateException("Security audit logger is not enabled. Please check your logging configuration.");
    }
  }

  @Override
  public void handle(RoutingContext ctx) {
    // the audit preserves state during the request, so it needs
    // a new instance per request
    final SecurityAudit audit = SecurityAudit.create();
    ((RoutingContextInternal) ctx).setSecurityAudit(audit);

    final HttpServerRequest req = ctx.request();
    final HttpServerResponse res = ctx.response();

    audit
      .source(req.remoteAddress())
      .destination(req.localAddress())
      .resource(req.version(), req.method(), ctx.normalizedPath());

    ctx.addEndHandler(end -> {
      final int status = res.getStatusCode();
      audit
        .status(status)
        .audit(Marker.REQUEST, status < 400);
    });

    ctx.next();
  }
}
