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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.core.Cookie;
import io.vertx.ext.apex.addons.FileUpload;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RoutingContextWrapper extends RoutingContextImplBase {

  protected final RoutingContext inner;
  private final String mountPoint;

  public RoutingContextWrapper(String mountPoint, HttpServerRequest request, Iterator<RouteImpl> iter,
                               RoutingContext inner) {
    super(mountPoint, request, iter);
    this.inner = inner;
    String parentMountPoint = inner.mountPoint();
    if (mountPoint.charAt(mountPoint.length() - 1) == '/') {
      // Remove the trailing slash or we won't match
      mountPoint = mountPoint.substring(0, mountPoint.length() - 1);
    }
    this.mountPoint = parentMountPoint == null ? mountPoint : parentMountPoint + mountPoint;
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
  public Map<String, Object> contextData() {
    return inner.contextData();
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
    // The router itself counts as handling so we reverse this
    unhandled();
    if (!super.iterateNext()) {
      // We didn't route request to anything so go to parent
      inner.next();
    }
  }

  @Override
  public void setHandled(boolean handled) {
    inner.setHandled(handled);
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

  @Override
  public String mountPoint() {
    return mountPoint;
  }

  @Override
  public String normalisedPath() {
    return inner.normalisedPath();
  }

  @Override
  public Cookie getCookie(String name) {
    return inner.getCookie(name);
  }

  @Override
  public void addCookie(Cookie cookie) {
    inner.addCookie(cookie);
  }

  @Override
  public Cookie removeCookie(String name) {
    return inner.removeCookie(name);
  }

  @Override
  public int cookieCount() {
    return inner.cookieCount();
  }

  @Override
  public Set<Cookie> cookies() {
    return inner.cookies();
  }

  @Override
  public String getBodyAsString() {
    return inner.getBodyAsString();
  }

  @Override
  public String getBodyAsString(String encoding) {
    return inner.getBodyAsString(encoding);
  }

  @Override
  public JsonObject getBodyAsJson() {
    return inner.getBodyAsJson();
  }

  @Override
  public Buffer getBody() {
    return inner.getBody();
  }

  @Override
  public void setBody(Buffer body) {
    inner.setBody(body);
  }

  @Override
  public Set<FileUpload> fileUploads() {
    return inner.fileUploads();
  }
}
