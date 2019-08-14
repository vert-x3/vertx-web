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
import io.vertx.core.Promise;
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
  private final Promise<Boolean> promise;

  public BridgeEventImpl(BridgeEventType type, JsonObject rawMessage, SockJSSocket socket) {
    this.type = type;
    this.rawMessage = rawMessage;
    this.socket = socket;
    this.promise = Promise.promise();
  }

  @Override
  public Future<Boolean> future() {
    return promise.future();
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
    promise.handle(asyncResult);
  }

  @Override
  public SockJSSocket socket() {
    return socket;
  }

  @Override
  public void complete(Boolean result) {
    promise.complete(result);
  }

  @Override
  public void complete() {
    promise.complete();
  }

  @Override
  public void fail(Throwable throwable) {
    promise.fail(throwable);
  }

  @Override
  public void fail(String failureMessage) {
    promise.fail(failureMessage);
  }

  @Override
  public boolean tryComplete(Boolean result) {
    return promise.tryComplete(result);
  }

  @Override
  public boolean tryComplete() {
    return promise.tryComplete();
  }

  @Override
  public boolean tryFail(Throwable cause) {
    return promise.tryFail(cause);
  }

  @Override
  public boolean tryFail(String failureMessage) {
    return promise.tryFail(failureMessage);
  }

}
