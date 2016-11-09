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

import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.*;
import io.vertx.ext.web.Locale;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class RoutingContextImpl extends RoutingContextImplBase {

  private final RouterImpl router;
  private Map<String, Object> data;
  private Map<String, String> pathParams;
  private AtomicInteger handlerSeq = new AtomicInteger();
  private Map<Integer, Handler<Void>> headersEndHandlers;
  private Map<Integer, Handler<Void>> bodyEndHandlers;
  private Throwable failure;
  private int statusCode = -1;
  private String normalisedPath;
  private String acceptableContentType;
  private ParsableHeaderValuesContainer parsedHeaders;

  // We use Cookie as the key too so we can return keySet in cookies() without copying
  private Map<String, Cookie> cookies;
  private Buffer body;
  private Set<FileUpload> fileUploads;
  private Session session;
  private User user;

  public RoutingContextImpl(String mountPoint, RouterImpl router, HttpServerRequest request, Set<RouteImpl> routes) {
    super(mountPoint, request, routes);
    this.router = router;
    try{
      fillParsedHeaders(request);
      if (request.path().charAt(0) != '/') {
        fail(404);
      }
    } catch (HeaderTooLongException e){
      fail(e);
    }
  }

  private String ensureNotNull(String string){
    return string == null ? "" : string;
  }

  private static void assertHeaderSmallEnough(String name, String content){
    if(content != null && content.length() > HeaderParser.MAX_HEADER_SIZE){
      throw new HeaderTooLongException("Header '" + name + "' too long");
    }
  }
  
  private void fillParsedHeaders(HttpServerRequest request) {
    String accept = request.getHeader("Accept");
    String acceptCharset = request.getHeader ("Accept-Charset");
    String acceptEncoding = request.getHeader("Accept-Encoding");
    String acceptLanguage = request.getHeader("Accept-Language");
    String contentType = ensureNotNull(request.getHeader("Content-Type"));

    assertHeaderSmallEnough("Accept", accept);
    assertHeaderSmallEnough("Accept-Charset",  acceptCharset);
    assertHeaderSmallEnough("Accept-Encoding", acceptEncoding);
    assertHeaderSmallEnough("Accept-Language", acceptLanguage);
    assertHeaderSmallEnough("Content-Type", contentType);

    parsedHeaders = new ParsableHeaderValuesContainer(
        HeaderParser.sort(HeaderParser.convertToParsedHeaderValues(accept, ParsableMIMEValue::new)),
        HeaderParser.sort(HeaderParser.convertToParsedHeaderValues(acceptCharset, ParsableHeaderValue::new)),
        HeaderParser.sort(HeaderParser.convertToParsedHeaderValues(acceptEncoding, ParsableHeaderValue::new)),
        HeaderParser.sort(HeaderParser.convertToParsedHeaderValues(acceptLanguage, ParsableLanguageValue::new)),
        new ParsableMIMEValue(contentType)
    );
    
  }

  @Override
  public HttpServerRequest request() {
    return request;
  }

  @Override
  public HttpServerResponse response() {
    return request.response();
  }

  @Override
  public Throwable failure() {
    return failure;
  }

  @Override
  public int statusCode() {
    return statusCode;
  }

  @Override
  public boolean failed() {
    return failure != null || statusCode != -1;
  }

  @Override
  public void next() {
    if (!iterateNext()) {
      checkHandleNoMatch();
    }
  }

  private void checkHandleNoMatch() {
    // Next called but no more matching routes
    if (failed()) {
      // Send back FAILURE
      unhandledFailure(statusCode, failure, router);
    } else {
      // Send back default 404
      response().setStatusCode(404);
      if (request().method() == HttpMethod.HEAD) {
        // HEAD responses don't have a body
        response().end();
      } else {
        response()
                .putHeader(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
                .end(DEFAULT_404);
      }
    }
  }

  @Override
  public void fail(int statusCode) {
    this.statusCode = statusCode;
    doFail();
  }

  @Override
  public void fail(Throwable t) {
    this.failure = t == null ? new NullPointerException() : t;
    doFail();
  }

  @Override
  public RoutingContext put(String key, Object obj) {
    getData().put(key, obj);
    return this;
  }

  @Override
  public Vertx vertx() {
    return router.vertx();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    Object obj = getData().get(key);
    return (T)obj;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T remove(String key) {
    Object obj = getData().remove(key);
    return (T)obj;
  }

  @Override
  public Map<String, Object> data() {
    return getData();
  }

  @Override
  public String normalisedPath() {
    if (normalisedPath == null) {
      normalisedPath = Utils.normalisePath(request.path());
    }
    return normalisedPath;
  }

  @Override
  public Cookie getCookie(String name) {
    return cookiesMap().get(name);
  }

  @Override
  public RoutingContext addCookie(Cookie cookie) {
    cookiesMap().put(cookie.getName(), cookie);
    return this;
  }

  @Override
  public Cookie removeCookie(String name) {
    return cookiesMap().remove(name);
  }

  @Override
  public int cookieCount() {
    return cookiesMap().size();
  }

  @Override
  public Set<Cookie> cookies() {
    return new HashSet<>(cookiesMap().values());
  }

  @Override
  public String getBodyAsString() {
    return body != null ? body.toString() : null;
  }

  @Override
  public String getBodyAsString(String encoding) {
    return body != null ? body.toString(encoding) : null;
  }

  @Override
  public JsonObject getBodyAsJson() {
    return body != null ? new JsonObject(body.toString()) : null;
  }

  @Override
  public JsonArray getBodyAsJsonArray() {
    return body != null ? new JsonArray(body.toString()) : null;
  }

  @Override
  public Buffer getBody() {
    return body;
  }

  @Override
  public void setBody(Buffer body) {
    this.body = body;
  }

  @Override
  public Set<FileUpload> fileUploads() {
    return getFileUploads();
  }

  @Override
  public void setSession(Session session) {
    this.session = session;
  }

  @Override
  public Session session() {
    return session;
  }

  @Override
  public User user() {
    return user;
  }

  @Override
  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public void clearUser() {
    this.user = null;
  }

  @Override
  public String getAcceptableContentType() {
    return acceptableContentType;
  }

  @Override
  public void setAcceptableContentType(String contentType) {
    this.acceptableContentType = contentType;
  }
  
  @Override
  public ParsableHeaderValuesContainer parsedHeaders() {
    return parsedHeaders;
  }

  @Override
  public int addHeadersEndHandler(Handler<Void> handler) {
    int seq = nextHandlerSeq();
    getHeadersEndHandlers().put(seq, handler);
    return seq;
  }

  @Override
  public boolean removeHeadersEndHandler(int handlerID) {
    return getHeadersEndHandlers().remove(handlerID) != null;
  }

  @Override
  public int addBodyEndHandler(Handler<Void> handler) {
    int seq = nextHandlerSeq();
    getBodyEndHandlers().put(seq, handler);
    return seq;
  }

  @Override
  public boolean removeBodyEndHandler(int handlerID) {
    return getBodyEndHandlers().remove(handlerID) != null;
  }

  @Override
  public void reroute(HttpMethod method, String path) {
    ((HttpServerRequestWrapper) request).setMethod(method);
    ((HttpServerRequestWrapper) request).setPath(path);
    request.params().clear();
    // we need to reset the normalized path
    normalisedPath = null;
    // we also need to reset any previous status
    statusCode = -1;
    // we need to reset any response headers
    response().headers().clear();
    // special header case cookies are parsed and cached
    if (cookies != null) {
      cookies.clear();
    }
    // reset the end handlers
    if (headersEndHandlers != null) {
      headersEndHandlers.clear();
    }
    if (bodyEndHandlers != null) {
      bodyEndHandlers.clear();
    }

    failure = null;
    restart();
  }

  /**
   * <h5>Notes about the dangerous cast and suppression:</h5><br>
   * I know for sure that <code>List&lt;Locale></code> will contain only <code>List&lt;LanguageHeader></code>.<br>
   * Currently, LanguageHeader is the only one that extends Locale.<br>
   * Locale does not extend LanguageHeader because I want full backwards compatibility to the previous vertx version<br>
   * Also, Locale is being deprecated and the type of objects that extend it inside vertx should not change.
   */
  @SuppressWarnings({"rawtypes", "unchecked" })
  @Override
  public List<Locale> acceptableLocales() {
    return (List)parsedHeaders.acceptLanguage();
  }

  @Override
  public Map<String, String> pathParams() {
    return getPathParams();
  }

  @Override
  public @Nullable String pathParam(String name) {
    return getPathParams().get(name);
  }

  private Map<String, String> getPathParams() {
    if (pathParams == null) {
      pathParams = new HashMap<>();
    }
    return pathParams;
  }

  private Map<Integer, Handler<Void>> getHeadersEndHandlers() {
    if (headersEndHandlers == null) {
      // order is important we we should traverse backwards
      headersEndHandlers = new TreeMap<>(Collections.reverseOrder());
      response().headersEndHandler(v -> headersEndHandlers.values().forEach(handler -> handler.handle(null)));
    }
    return headersEndHandlers;
  }

  private Map<Integer, Handler<Void>> getBodyEndHandlers() {
    if (bodyEndHandlers == null) {
      // order is important we we should traverse backwards
      bodyEndHandlers = new TreeMap<>(Collections.reverseOrder());
      response().bodyEndHandler(v -> bodyEndHandlers.values().forEach(handler -> handler.handle(null)));
    }
    return bodyEndHandlers;
  }

  private Map<String, Cookie> cookiesMap() {
    if (cookies == null) {
      cookies = new HashMap<>();
    }
    return cookies;
  }

  private Set<FileUpload> getFileUploads() {
    if (fileUploads == null) {
      fileUploads = new HashSet<>();
    }
    return fileUploads;
  }

  private void doFail() {
    this.iter = router.iterator();
    next();
  }

  private Map<String, Object> getData() {
    if (data == null) {
      data = new HashMap<>();
    }
    return data;
  }

  private int nextHandlerSeq() {
    int seq = handlerSeq.incrementAndGet();
    if (seq == Integer.MAX_VALUE) {
      throw new IllegalStateException("Too many header/body end handlers!");
    }
    return seq;
  }

  private static final String DEFAULT_404 =
    "<html><body><h1>Resource not found</h1></body></html>";

}
