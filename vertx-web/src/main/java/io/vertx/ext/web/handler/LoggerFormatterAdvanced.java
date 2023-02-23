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
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 * Implement to format the output of the {@link LoggerHandler}
 *
 * @author <a href="mailto:oded@geek.co.il">Oded Arbel</a>
 */
@VertxGen
@FunctionalInterface
public interface LoggerFormatterAdvanced {

  /**
   * Formats and returns the log statement
   * 
   * @param routingContext The routing context
   * @param timestamp The system time in milliseconds when the request was handled by the {@link LoggerHandler}
   * @param remoteClient The remote client's host address
   * @param versionFormatted The HTTP version as formatted for display (normally {@code HTTP/x.x})
   * @param method The request's HTTP method
   * @param uri The request's URI
   * @param status The response's HTTP status code
   * @param contentLength The amount of bytes that were written in the response
   * @param ms The number of milliseconds since first receiving the request
   * @return The formatted string to log
   */
  String format(RoutingContext routingContext, long timestamp, String remoteClient, String versionFormatted,
    HttpMethod method, String uri, int status, long contentLength, long ms);
}
