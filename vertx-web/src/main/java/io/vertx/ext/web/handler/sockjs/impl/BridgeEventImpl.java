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

package io.vertx.ext.web.handler.sockjs.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
class BridgeEventImpl implements BridgeEvent {

  private final BridgeEventType type;
  private final JsonObject rawMessage;
  private final SockJSSocket socket;
  private Future<Boolean> future;

  public BridgeEventImpl(BridgeEventType type, JsonObject rawMessage, SockJSSocket socket) {
    this.type = type;
    this.rawMessage = rawMessage;
    this.socket = socket;
  }

  @Override
  public BridgeEventType type() {
    return type;
  }

  @Override
  public JsonObject getRawMessage() {
    return rawMessage;
  }

  @Override
  public BridgeEvent setRawMessage(JsonObject message) {
    if (message != rawMessage) {
      rawMessage.clear().mergeIn(message);
    }
    return this;
  }

  @Override
  public void handle(AsyncResult<Boolean> asyncResult) {
    future.handle(asyncResult);
  }

  @Override
  public SockJSSocket socket() {
    return socket;
  }

  public void setFuture(Future<Boolean> future) {
    this.future = future;
  }

  @Override
  public boolean isComplete() {
    return future.isComplete();
  }

  @Override
  public Future<Boolean> setHandler(Handler<AsyncResult<Boolean>> handler) {
    future.setHandler(handler);
    return this;
  }

  @Override
  public void complete(Boolean result) {
    future.complete(result);
  }

  @Override
  public void complete() {
    future.complete();
  }

  @Override
  public void fail(Throwable throwable) {
    future.fail(throwable);
  }

  @Override
  public void fail(String failureMessage) {
    future.fail(failureMessage);
  }

  @Override
  public boolean tryComplete(Boolean result) {
    return future.tryComplete(result);
  }

  @Override
  public boolean tryComplete() {
    return future.tryComplete();
  }

  @Override
  public boolean tryFail(Throwable cause) {
    return future.tryFail(cause);
  }

  @Override
  public boolean tryFail(String failureMessage) {
    return future.tryFail(failureMessage);
  }

  @Override
  public Boolean result() {
    return future.result();
  }

  @Override
  public Throwable cause() {
    return future.cause();
  }

  @Override
  public boolean succeeded() {
    return future.succeeded();
  }

  @Override
  public boolean failed() {
    return future.failed();
  }
}
