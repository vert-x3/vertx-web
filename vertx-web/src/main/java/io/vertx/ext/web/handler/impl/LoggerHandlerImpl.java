/*
 * Copyright 2014 Red Hat, Inc.
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

import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.logger.AccessLogFormatter;
import java.util.TimeZone;

/**
 * An access logger.
 *
 * @author <a href="http://pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="mailto:marcin.czeczko@gmail.com">Marcin Czeczko</a>
 */
public class LoggerHandlerImpl implements LoggerHandler {

  private final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * log before request or after
   */
  private final boolean immediate;

  /**
   * the access log formatter
   */
  private final AccessLogFormatter logFormatter;

  public LoggerHandlerImpl(boolean immediate, String pattern) {
    this.immediate = immediate;
    this.logFormatter = new AccessLogFormatter(pattern);
  }

  public LoggerHandlerImpl(String pattern) {
    this(false, pattern);
  }

  private void log(RoutingContext context, long timestamp) {

    context.put("logger-requestStart", timestamp);
    int status = context.response().getStatusCode();
    String message = logFormatter.format(context, immediate);

    if (status >= 500) {
      logger.error(message);
    } else if (status >= 400) {
      logger.warn(message);
    } else {
      logger.info(message);
    }
  }


  @Override
  public void handle(RoutingContext context) {
    long start = System.currentTimeMillis();
    context.addHeadersEndHandler(v -> context
      .put("logger-requestDuration",
        System.currentTimeMillis() - start));

    if (immediate) {
      log(context, start);
    } else {
      context.addBodyEndHandler(v -> log(context, start));
    }

    context.next();

  }

  @Override
  public LoggerHandler setTimeZone(TimeZone tz) {
    logFormatter.setTimeZone(tz);
    return this;
  }
}
