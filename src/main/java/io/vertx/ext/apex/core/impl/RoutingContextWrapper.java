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

package io.vertx.ext.apex.core.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Iterator;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RoutingContextWrapper extends RoutingContextImplBase {

  protected final RoutingContext inner;

  public RoutingContextWrapper(HttpServerRequest request, Iterator<RouteImpl> iter,
                               RoutingContext inner) {
    super(request, iter);
    this.inner = inner;
  }

  @Override
  public HttpServerRequest request() {
    return inner.request();
  }

  @Override
  public HttpServerResponse response() {
    return inner.response();
  }

  @Override
  public void fail(int statusCode) {
    inner.fail(statusCode);
  }

  @Override
  public void fail(Throwable throwable) {
    inner.fail(throwable);
  }

  @Override
  public void put(String key, Object obj) {
    inner.put(key, obj);
  }

  @Override
  public <T> T get(String key) {
    return inner.get(key);
  }

  @Override
  public Vertx vertx() {
    return inner.vertx();
  }

  @Override
  public void addHeadersEndHandler(Handler<Void> handler) {
    inner.addHeadersEndHandler(handler);
  }

  @Override
  public boolean removeHeadersEndHandler(Handler<Void> handler) {
    return inner.removeHeadersEndHandler(handler);
  }

  @Override
  public void addBodyEndHandler(Handler<Void> handler) {
    inner.addBodyEndHandler(handler);
  }

  @Override
  public boolean removeBodyEndHandler(Handler<Void> handler) {
    return inner.removeBodyEndHandler(handler);
  }

  @Override
  public void next() {
    if (!super.iterateNext()) {
      // The router itself counts as handling so we cancel this if we didn't handle anything in the router
      unhandled();
      // We didn't route request to anything so go to parent
      inner.next();
    }
  }

  @Override
  public void setHandled(boolean handled) {
    inner.setHandled(handled);
  }

  @Override
  public boolean handled() {
    return inner.handled();
  }

  @Override
  public boolean failed() {
    return inner.failed();
  }

  @Override
  public void unhandled() {
    inner.unhandled();
  }

  @Override
  public Throwable failure() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int statusCode() {
    throw new UnsupportedOperationException();
  }
}
