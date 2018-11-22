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

package io.vertx.ext.web.impl;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RoutingContextWrapper extends RoutingContextImplBase {

  protected final RoutingContext inner;
  private final String mountPoint;

  public RoutingContextWrapper(String mountPoint, HttpServerRequest request, Set<RouteImpl> iter,
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
  public RoutingContext put(String key, Object obj) {
    inner.put(key, obj);
    return this;
  }

  @Override
  public <T> T get(String key) {
    return inner.get(key);
  }

  @Override
  public <T> T remove(String key) {
    return inner.remove(key);
  }

  @Override
  public Map<String, Object> data() {
    return inner.data();
  }

  @Override
  public Vertx vertx() {
    return inner.vertx();
  }

  @Override
  public int addHeadersEndHandler(Handler<Void> handler) {
    return inner.addHeadersEndHandler(handler);
  }

  @Override
  public boolean removeHeadersEndHandler(int handlerID) {
    return inner.removeHeadersEndHandler(handlerID);
  }

  @Override
  public int addBodyEndHandler(Handler<Void> handler) {
    return inner.addBodyEndHandler(handler);
  }

  @Override
  public boolean removeBodyEndHandler(int handlerID) {
    return inner.removeBodyEndHandler(handlerID);
  }

  @Override
  public void setSession(Session session) {
   inner.setSession(session);
  }

  @Override
  public Session session() {
    return inner.session();
  }

  @Override
  public void setUser(User user) {
    inner.setUser(user);
  }

  @Override
  public void clearUser() {
    inner.clearUser();
  }

  @Override
  public User user() {
    return inner.user();
  }

  @Override
  public void next() {
    if (!super.iterateNext()) {
      // We didn't route request to anything so go to parent
      inner.next();
    }
  }

  @Override
  public boolean failed() {
    return inner.failed();
  }

  @Override
  public Throwable failure() {
    return inner.failure();
  }

  @Override
  public int statusCode() {
    return inner.statusCode();
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
  public RoutingContext addCookie(Cookie cookie) {
    inner.addCookie(cookie);
    return this;
  }

  @Override
  public Cookie removeCookie(String name, boolean invalidate) {
    return inner.removeCookie(name, invalidate);
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
  public JsonArray getBodyAsJsonArray() {
    return inner.getBodyAsJsonArray();
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

  @Override
  public String getAcceptableContentType() {
    return inner.getAcceptableContentType();
  }

  @Override
  public ParsedHeaderValues parsedHeaders() {
    return inner.parsedHeaders();
  }

  @Override
  public void setAcceptableContentType(String contentType) {
    inner.setAcceptableContentType(contentType);
  }

  @Override
  public void reroute(HttpMethod method, String path) {
    inner.reroute(method, path);
  }

  @Override
  public List<Locale> acceptableLocales() {
    return inner.acceptableLocales();
  }

  @Override
  public Map<String, String> pathParams() {
    return inner.pathParams();
  }

  @Override
  public @Nullable String pathParam(String name) {
    return inner.pathParam(name);
  }

  @Override
  public MultiMap queryParams() { return inner.queryParams(); }

  @Override
  public @Nullable List<String> queryParam(String query) {
    return inner.queryParam(query);
  }

}
