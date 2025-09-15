/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.client.impl;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.internal.http.HttpClientInternal;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.Address;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.uritemplate.UriTemplate;
import io.vertx.uritemplate.Variables;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class HttpRequestImpl<T> implements HttpRequest<T> {

  private final WebClientBase client;
  private ProxyOptions proxyOptions;
  private final Address address;
  private MultiMap queryParams;
  private Variables templateParams;
  private HttpMethod method;
  private final UriTemplate absoluteUri;
  private int port;
  private String host;
  private String virtualHost;
  private Object uri;
  private long timeout = -1;
  private long idleTimeout = -1;
  private long connectTimeout = -1;
  private String routingKey;
  private boolean followRedirects;
  private Boolean ssl;
  private boolean multipartMixed = true;
  private String traceOperation;
  private BodyCodec<T> codec;
  MultiMap headers;

  HttpRequestImpl(WebClientBase client,
                  HttpMethod method,
                  Address address,
                  UriTemplate absoluteUri,
                  BodyCodec<T> codec,
                  boolean followRedirects,
                  ProxyOptions proxyOptions,
                  MultiMap headers) {
    Objects.requireNonNull(absoluteUri, "AbsoluteUri cannot be null");
    this.client = client;
    this.absoluteUri = absoluteUri;
    this.address = address;
    this.method = method;
    this.ssl = null;
    this.host = null;
    this.port = -1;
    this.uri = null;
    this.codec = codec;
    this.headers = headers;
    this.followRedirects = followRedirects;
    this.proxyOptions = proxyOptions;
  }

  HttpRequestImpl(WebClientBase client,
                  HttpMethod method,
                  Address address,
                  Boolean ssl,
                  int port,
                  String host,
                  Object uri,
                  BodyCodec<T> codec,
                  boolean followRedirects,
                  ProxyOptions proxyOptions,
                  MultiMap headers) {
    Objects.requireNonNull(host, "Host cannot be null");
    this.client = client;
    this.absoluteUri = null;
    this.address = address;
    this.method = method;
    this.ssl = ssl;
    this.port = port;
    this.host = host;
    this.uri = uri;
    this.codec = codec;
    this.headers = headers;
    this.followRedirects = followRedirects;
    this.proxyOptions = proxyOptions;
  }

  private HttpRequestImpl(HttpRequestImpl<T> other) {
    this.client = other.client;
    this.absoluteUri = other.absoluteUri;
    this.address = other.address;
    this.ssl = other.ssl;
    this.method = other.method;
    this.port = other.port;
    this.host = other.host;
    this.uri = other.uri;
    this.codec = other.codec;
    this.headers = other.headers != null ? HttpHeaders.headers().addAll(other.headers) : HttpHeaders.headers();
    this.followRedirects = other.followRedirects;
    this.proxyOptions = other.proxyOptions != null ? new ProxyOptions(other.proxyOptions) : null;
    this.timeout = other.timeout;
    this.idleTimeout = other.idleTimeout;
    this.connectTimeout = other.connectTimeout;
    this.routingKey = other.routingKey;
    this.queryParams = other.queryParams != null ? MultiMap.caseInsensitiveMultiMap().addAll(other.queryParams) : null;
    this.multipartMixed = other.multipartMixed;
    this.virtualHost = other.virtualHost;
  }

  @Override
  public <U> HttpRequest<U> as(BodyCodec<U> responseCodec) {
    codec = (BodyCodec<T>) responseCodec;
    return (HttpRequest<U>) this;
  }

  @Override
  public BodyCodec<T> bodyCodec() {
    return codec;
  }

  @Override
  public HttpRequest<T> method(HttpMethod value) {
    method = value;
    return this;
  }

  @Override
  public HttpMethod method() {
    return method;
  }

  @Override
  public HttpRequest<T> ssl(Boolean value) {
    ssl = value;
    return this;
  }

  @Override
  public Boolean ssl() {
    return ssl;
  }

  @Override
  public HttpRequest<T> port(int value) {
    port = value;
    return this;
  }

  @Override
  public int port() {
    return port;
  }

  @Override
  public HttpRequest<T> host(String value) {
    Objects.requireNonNull(host, "Host cannot be null");
    host = value;
    return this;
  }

  @Override
  public String host() {
    return host;
  }

  @Override
  public HttpRequest<T> uri(String value) {
    queryParams = null;
    uri = value;
    return this;
  }

  public String uri() {
    return uri.toString();
  }

  @Override
  public HttpRequest<T> virtualHost(String value) {
    virtualHost = value;
    return this;
  }

  @Override
  public String virtualHost() {
    return virtualHost;
  }

  @Override
  public HttpRequest<T> putHeaders(MultiMap headers) {
    headers().addAll(headers);
    return this;
  }

  @Override
  public HttpRequest<T> putHeader(String name, String value) {
    headers().set(name, value);
    return this;
  }

  @Override
  public HttpRequest<T> putHeader(CharSequence name, CharSequence value) {
    headers().set(name, value);
    return this;
  }

  @Override
  public HttpRequest<T> putHeader(String name, Iterable<String> value) {
    headers().set(name, value);
    return this;
  }

  @Override
  public HttpRequest<T> putHeader(CharSequence name, Iterable<CharSequence> value) {
    headers().set(name, value);
    return this;
  }

  @Override
  public MultiMap headers() {
    if (headers == null) {
      headers = HttpHeaders.headers();
    }
    return headers;
  }

  @Override
  public HttpRequest<T> authentication(Credentials credentials) {
    putHeader(
      HttpHeaders.AUTHORIZATION.toString(),
      credentials.toHttpAuthorization());

    return this;
  }

  @Override
  public HttpRequest<T> timeout(long value) {
    timeout = value;
    return this;
  }

  @Override
  public long timeout() {
    return timeout;
  }

  @Override
  public HttpRequest<T> idleTimeout(long timeout) {
    idleTimeout = timeout;
    return this;
  }

  @Override
  public long idleTimeout() {
    return idleTimeout;
  }

  @Override
  public HttpRequest<T> connectTimeout(long timeout) {
    connectTimeout = timeout;
    return this;
  }

  @Override
  public long connectTimeout() {
    return connectTimeout;
  }

  @Override
  public HttpRequest<T> addQueryParam(String paramName, String paramValue) {
    queryParams().add(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<T> setQueryParam(String paramName, String paramValue) {
    queryParams().set(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<T> setTemplateParam(String paramName, String paramValue) {
    templateParams().set(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<T> setTemplateParam(String paramName, List<String> paramValue) {
    templateParams().set(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<T> setTemplateParam(String paramName, Map<String, String> paramValue) {
    templateParams().set(paramName, paramValue);
    return this;
  }

  @Override
  public HttpRequest<T> followRedirects(boolean value) {
    followRedirects = value;
    return this;
  }

  @Override
  public boolean followRedirects() {
    return followRedirects;
  }

  @Override
  public HttpRequest<T> routingKey(String key) {
    routingKey = key;
    return this;
  }

  @Override
  public String routingKey() {
    return routingKey;
  }

  @Override
  public HttpRequest<T> proxy(ProxyOptions proxyOptions) {
    this.proxyOptions = proxyOptions;
    return this;
  }

  @Override
  public ProxyOptions proxy() {
    return proxyOptions;
  }

  @Override
  public MultiMap queryParams() {
    if (queryParams == null) {
      queryParams = MultiMap.caseInsensitiveMultiMap();
      if (uri instanceof String) {
        int idx = ((String)uri).indexOf('?');
        if (idx >= 0) {
          QueryStringDecoder dec = new QueryStringDecoder((String)uri);
          dec.parameters().forEach((name, value) -> queryParams.add(name, value));
          uri = ((String)uri).substring(0, idx);
        }
      }
    }
    return queryParams;
  }

  @Override
  public Variables templateParams() {
    if (!(uri instanceof UriTemplate) && !(absoluteUri instanceof UriTemplate)) {
      throw new IllegalStateException();
    }
    if (templateParams == null) {
      templateParams = Variables.variables();
    }
    return templateParams;
  }

  @Override
  public HttpRequest<T> copy() {
    return new HttpRequestImpl<>(this);
  }

  @Override
  public HttpRequest<T> multipartMixed(boolean allow) {
    multipartMixed = allow;
    return this;
  }

  @Override
  public HttpRequest<T> traceOperation(String traceOperation) {
    this.traceOperation = traceOperation;
    return this;
  }

  @Override
  public String traceOperation() {
    return traceOperation;
  }

  @Override
  public boolean multipartMixed() {
    return multipartMixed;
  }

  @Override
  public Future<HttpResponse<T>> sendStream(ReadStream<Buffer> body) {
    return send(null, body.pipe());
  }

  @Override
  public Future<HttpResponse<T>> send() {
    return send(null, null);
  }

  @Override
  public Future<HttpResponse<T>> sendBuffer(Buffer body) {
    return send(null, body);
  }

  @Override
  public Future<HttpResponse<T>> sendJsonObject(JsonObject body) {
    return send("application/json", body);
  }

  @Override
  public Future<HttpResponse<T>> sendJson(Object body) {
    return send("application/json", body);
  }

  @Override
  public Future<HttpResponse<T>> sendForm(MultiMap body) {
    return sendForm(body, "UTF-8");
  }

  @Override
  public Future<HttpResponse<T>> sendForm(MultiMap body, String charset) {
    MultipartForm parts = MultipartForm.create();
    for (Map.Entry<String, String> attribute : body) {
      parts.attribute(attribute.getKey(), attribute.getValue());
    }
    parts.setCharset(charset);
    return send("application/x-www-form-urlencoded", parts);
  }

  @Override
  public Future<HttpResponse<T>> sendMultipartForm(MultipartForm body) {
    return send("multipart/form-data", body);
  }

  RequestOptions buildRequestOptions() throws URISyntaxException, MalformedURLException {

    String protocol = null;
    Boolean ssl = null;
    int port = -1;
    String host = null;
    String uri = null;
    if (absoluteUri != null) {
      uri = absoluteUri.expandToString(templateParams(), client.options.getTemplateExpandOptions());
      ClientUri curi = ClientUri.parse(uri);
      uri = curi.uri;
      host = curi.host;
      port = curi.port;
      protocol = curi.protocol;
      ssl = curi.ssl;
    }
    if (this.ssl != null) {
      ssl = this.ssl;
    }
    if (this.port >= 0) {
      port = this.port;
    }
    if (this.host != null) {
      host = this.host;
    }
    if (this.uri != null) {
      if (this.uri instanceof String) {
        uri = (String) this.uri;
      } else {
        uri = ((UriTemplate) this.uri).expandToString(templateParams(), client.options.getTemplateExpandOptions());
      }
    }
    if (queryParams != null) {
      uri = buildUri(uri, queryParams);
    }

    RequestOptions requestOptions = new RequestOptions();
    if (protocol != null && !protocol.equals("http") && !protocol.equals("https")) {
      // we have to create an abs url again to parse it in HttpClient
      URI tmp = new URI(protocol, null, host, port, uri, null, null);
      requestOptions.setServer(this.address)
        .setMethod(this.method)
        .setAbsoluteURI(tmp.toString());
    } else {
      requestOptions.setServer(this.address)
        .setMethod(this.method)
        .setHost(host)
        .setPort(port)
        .setURI(uri);
      // if the user specified SSL we always enforce it
      // even if the client has a default, because the default
      // may have been used previously to compute the request options
      if (ssl != null) {
        requestOptions
          .setSsl(ssl);
      }
    }
    if (this.virtualHost != null) {
      if (requestOptions.getServer() == null) {
        requestOptions.setServer(SocketAddress.inetSocketAddress(requestOptions.getPort(), requestOptions.getHost()));
      }
      requestOptions.setHost(this.virtualHost);
    }
    this.mergeHeaders(requestOptions);
    if (timeout >= 0) {
      requestOptions.setTimeout(timeout);
    }
    if (idleTimeout >= 0) {
      requestOptions.setIdleTimeout(idleTimeout);
    }
    if (connectTimeout >= 0) {
      requestOptions.setConnectTimeout(connectTimeout);
    }
    requestOptions.setRoutingKey(this.routingKey);
    requestOptions.setProxyOptions(this.proxyOptions);
    requestOptions.setTraceOperation(this.traceOperation);
    return requestOptions;
  }

  Future<HttpResponse<T>> send(String contentType, Object body) {
    ContextInternal context = ((HttpClientInternal) client.client).vertx().getOrCreateContext();
    HttpContext<T> ctx = client.createContext(context);
    ctx.prepareRequest(this, contentType, body);
    return ctx.future();
  }

  void mergeHeaders(RequestOptions options) {
    if (headers != null) {
      MultiMap tmp = options.getHeaders();
      if (tmp == null) {
        tmp = MultiMap.caseInsensitiveMultiMap();
        options.setHeaders(tmp);
      }
      tmp.addAll(headers);
    }
  }

  private static String buildUri(String uri, MultiMap queryParams) {
    QueryStringDecoder decoder = new QueryStringDecoder(uri);
    QueryStringEncoder encoder = new QueryStringEncoder(decoder.rawPath());
    decoder.parameters().forEach((name, values) -> {
      for (String value : values) {
        encoder.addParam(name, value);
      }
    });
    queryParams.forEach(param -> {
      encoder.addParam(param.getKey(), param.getValue());
    });
    uri = encoder.toString();
    return uri;
  }

}
