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
package io.vertx.webclient.impl;

import io.vertx.core.VertxException;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.webclient.HttpRequest;
import io.vertx.webclient.WebClient;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WebClientImpl implements WebClient {

  private final HttpClient client;

  public WebClientImpl(HttpClient client) {
    this.client = client;
  }

  @Override
  public HttpRequest get(int port, String host, String requestURI) {
    return request(HttpMethod.GET, port, host, requestURI);
  }

  @Override
  public HttpRequest get(String requestURI) {
    return request(HttpMethod.GET, requestURI);
  }

  @Override
  public HttpRequest get(String host, String requestURI) {
    return request(HttpMethod.GET, host, requestURI);
  }

  @Override
  public HttpRequest getAbs(String absoluteURI) {
    return requestAbs(HttpMethod.GET, absoluteURI);
  }

  @Override
  public HttpRequest post(String requestURI) {
    return request(HttpMethod.POST, requestURI);
  }

  @Override
  public HttpRequest post(String host, String requestURI) {
    return request(HttpMethod.POST, host, requestURI);
  }

  @Override
  public HttpRequest post(int port, String host, String requestURI) {
    return request(HttpMethod.POST, port, host, requestURI);
  }

  @Override
  public HttpRequest put(String requestURI) {
    return request(HttpMethod.PUT, requestURI);
  }

  @Override
  public HttpRequest put(String host, String requestURI) {
    return request(HttpMethod.PUT, host, requestURI);
  }

  @Override
  public HttpRequest put(int port, String host, String requestURI) {
    return request(HttpMethod.PUT, port, host, requestURI);
  }

  @Override
  public HttpRequest delete(String host, String requestURI) {
    return request(HttpMethod.DELETE, host, requestURI);
  }

  @Override
  public HttpRequest delete(String requestURI) {
    return request(HttpMethod.DELETE, requestURI);
  }

  @Override
  public HttpRequest delete(int port, String host, String requestURI) {
    return request(HttpMethod.DELETE, port, host, requestURI);
  }

  @Override
  public HttpRequest patch(String requestURI) {
    return request(HttpMethod.PATCH, requestURI);
  }

  @Override
  public HttpRequest patch(String host, String requestURI) {
    return request(HttpMethod.PATCH, host, requestURI);
  }

  @Override
  public HttpRequest patch(int port, String host, String requestURI) {
    return request(HttpMethod.PATCH, port, host, requestURI);
  }

  @Override
  public HttpRequest head(String requestURI) {
    return request(HttpMethod.HEAD, requestURI);
  }

  @Override
  public HttpRequest head(String host, String requestURI) {
    return request(HttpMethod.HEAD, host, requestURI);
  }

  @Override
  public HttpRequest head(int port, String host, String requestURI) {
    return request(HttpMethod.HEAD, port, host, requestURI);
  }

  @Override
  public HttpRequest postAbs(String absoluteURI) {
    return requestAbs(HttpMethod.POST, absoluteURI);
  }

  @Override
  public HttpRequest putAbs(String absoluteURI) {
    return requestAbs(HttpMethod.PUT, absoluteURI);
  }

  @Override
  public HttpRequest deleteAbs(String absoluteURI) {
    return requestAbs(HttpMethod.DELETE, absoluteURI);
  }

  @Override
  public HttpRequest patchAbs(String absoluteURI) {
    return requestAbs(HttpMethod.PATCH, absoluteURI);
  }

  @Override
  public HttpRequest headAbs(String absoluteURI) {
    return requestAbs(HttpMethod.HEAD, absoluteURI);
  }

  public HttpRequest request(HttpMethod method, String requestURI) {
    HttpRequestImpl request = new HttpRequestImpl(client, method);
    request.uri = requestURI;
    return request;
  }

  public HttpRequest request(HttpMethod method, String host, String requestURI) {
    HttpRequestImpl request = new HttpRequestImpl(client, method);
    request.host = host;
    request.uri = requestURI;
    return request;
  }

  public HttpRequest request(HttpMethod method, int port, String host, String requestURI) {
    HttpRequestImpl request = new HttpRequestImpl(client, method);
    request.port = port;
    request.host = host;
    request.uri = requestURI;
    return request;
  }

  public HttpRequest requestAbs(HttpMethod method, String surl) {
    // Note - parsing a URL this way is slower than specifying host, port and relativeURI
    URL url;
    try {
      url = new URL(surl);
    } catch (MalformedURLException e) {
      throw new VertxException("Invalid url: " + surl);
    }
    HttpRequestImpl request = new HttpRequestImpl(client, method);
    request.port = url.getPort();
    request.host = url.getHost();
    request.uri = url.getFile();
    return request;
  }

  @Override
  public void close() {
    client.close();
  }
}
