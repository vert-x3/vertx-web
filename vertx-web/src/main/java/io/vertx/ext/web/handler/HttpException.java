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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * An utility exception class to signal HTTP failures.
 * <p>
 * The class that convey an HTTP status code, by default it is {@code 500}. The exception may contain a cause throwable
 * and for special cases a simple payload string may be added for context. The payload can be used for example perform
 * a redirect.
 * <p>
 * The message for the exception is inferred from the standard http error code using {@link HttpResponseStatus}.
 *
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public final class HttpException extends RuntimeException {

  private final int statusCode;
  private Object payload;
  private Class<?> callee;

  public HttpException() {
    this(500, null, null);
  }

  public HttpException(int statusCode) {
    this(statusCode, HttpResponseStatus.valueOf(statusCode).reasonPhrase(), null);
  }

  public HttpException(int statusCode, Throwable cause) {
    this(statusCode, HttpResponseStatus.valueOf(statusCode).reasonPhrase(), cause);
  }

  public HttpException(int statusCode, String message) {
    this(statusCode, message, null);
  }

  public HttpException(int statusCode, String message, Throwable cause) {
    super("[" + statusCode + "]: " + message, cause, false, cause == null);
    if (cause == null) {
      StackTraceElement[] currentTrace = Thread.currentThread().getStackTrace();
      StackTraceElement[] newTrace = new StackTraceElement[currentTrace.length - 3];
      System.arraycopy(currentTrace, 3, newTrace, 0, newTrace.length);
      setStackTrace(newTrace);
    }
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  @SuppressWarnings("unchecked")
  public <T> T getPayload() {
    return (T) payload;
  }

  public <T> HttpException setPayload(T value) {
    this.payload = value;
    return this;
  }

  public HttpException setCallee(Handler<RoutingContext> handler) {
    Objects.requireNonNull(handler, "'handler' cannot be null");
    this.callee = handler.getClass();
    return this;
  }

  public static int httpStatusCodeOf(Throwable throwable) {
    Objects.requireNonNull(throwable, "'throwable' must not be null");
    if (throwable instanceof HttpException) {
      return ((HttpException) throwable).getStatusCode();
    } else {
      return 500;
    }
  }

  public HttpException catchFrom(Class<?> clazz, Consumer<HttpException> consumer) {
    Objects.requireNonNull(clazz, "'clazz' must not be null");
    if (callee != null) {
      if (clazz.isAssignableFrom(callee)) {
        consumer.accept(this);
      }
    }
    return this;
  }
}
