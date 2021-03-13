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
package io.vertx.ext.web.handler;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.RoutingContext;

/**
 * Implement to format the output of the {@link LoggerHandler}
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 * @author <a href="mailto:thced82@gmail.com">Thomas Cederholm</a>
 */
@VertxGen
@FunctionalInterface
public interface LoggerFormatter {

  /**
   * Formats and returns the log statement
   *
   * @param routingContext The routing context
   * @param ms The number of milliseconds since first receiving the request
   * @return The formatted string to log
   */
  String format(RoutingContext routingContext, long ms);
}
