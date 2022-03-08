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
package io.vertx.ext.web.client;

import io.vertx.codegen.annotations.*;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.client.predicate.ResponsePredicateResult;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.uritemplate.Variables;
import io.vertx.uritemplate.UriTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A client-side HTTP request.
 * <p>
 * Instances are created by an {@link WebClient} instance, via one of the methods corresponding to the specific
 * HTTP methods such as {@link WebClient#get}, etc...
 * <p>
 * The request shall be configured prior sending, the request is immutable and when a mutator method
 * is called, a new request is returned allowing to expose the request in a public API and apply further customization.
 * <p>
 * After the request has been configured, the methods
 * <ul>
 *   <li>{@link #send(Handler)}</li>
 *   <li>{@link #sendStream(ReadStream, Handler)}</li>
 *   <li>{@link #sendJson(Object, Handler)} ()}</li>
 *   <li>{@link #sendForm(MultiMap, Handler)}</li>
 * </ul>
 * can be called.
 * The {@code sendXXX} methods perform the actual request, they can be called multiple times to perform the same HTTP
 * request at different points in time.
 * <p>
 * The handler is called back with
 * <ul>
 *   <li>an {@link HttpResponse} instance when the HTTP response has been received</li>
 *   <li>a failure when the HTTP request failed (like a connection error) or when the HTTP response could
 *   not be obtained (like connection or unmarshalling errors)</li>
 * </ul>
 * <p>
 * Most of the time, this client will buffer the HTTP response fully unless a specific {@link BodyCodec} is used
 * such as {@link BodyCodec#create(Handler)}.
 *
 * @param <T> the type of response body
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface HttpRequest<T> {

  /**
   * Configure the request to use a new method {@code value}.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> method(HttpMethod value);

  /**
   * Configure the request to use a new port {@code value}.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> port(int value);

  /**
   * Configure the request to decode the response with the {@code responseCodec}.
   *
   * @param responseCodec the response codec
   * @return a reference to this, so the API can be used fluently
   */
  <U> HttpRequest<U> as(BodyCodec<U> responseCodec);

  /**
   * Configure the request to use a new host {@code value}.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> host(String value);

  /**
   * Configure the request to use a virtual host {@code value}.
   * <p/>
   * Usually the header <i>host</i> (<i>:authority</i> pseudo header for HTTP/2) is set from the request host value
   * since this host value resolves to the server IP address.
   * <p/>
   * Sometimes you need to set a host header for an address that does not resolve to the server IP address.
   * The virtual host value overrides the value of the actual <i>host</i> header (<i>:authority</i> pseudo header
   * for HTTP/2).
   * <p/>
   * The virtual host is also be used for SNI.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> virtualHost(String value);

  /**
   * Configure the request to use a new request URI {@code value}.
   * <p>
   * When the uri has query parameters, they are set in the {@link #queryParams()} multimap, overwritting
   * any parameters previously set.
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> uri(String value);

  /**
   * Configure the request to add multiple HTTP headers .
   *
   * @param headers The HTTP headers
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> putHeaders(MultiMap headers);

  /**
   * Configure the request to set a new HTTP header.
   *
   * @param name  the header name
   * @param value the header value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> putHeader(String name, String value);

  /**
   * Configure the request to set a new HTTP header with multiple values.
   *
   * @param name  the header name
   * @param value the header value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  HttpRequest<T> putHeader(String name, Iterable<String> value);

  /**
   * @return The HTTP headers
   */
  @CacheReturn
  MultiMap headers();

  /**
   * Configure the request to perform HTTP Authentication.
   * <p>
   * Performs a generic authentication using the credentials provided by the user. For the sake of validation safety
   * it is recommended that {@link Credentials#applyHttpChallenge(String)} is called to ensure that the credentials
   * are applicable to the HTTP Challenged received on a previous request that returned a 401 response code.
   *
   * @param credentials    the credentials to use.
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  HttpRequest<T> authentication(Credentials credentials);

  /**
   * Configure the request to perform basic access authentication.
   * <p>
   * In basic HTTP authentication, a request contains a header field of the form 'Authorization: Basic &#60;credentials&#62;',
   * where credentials is the base64 encoding of id and password joined by a colon.
   * </p>
   * In practical terms the arguments are converted to a {@link UsernamePasswordCredentials} object.
   *
   * @param id       the id
   * @param password the password
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default HttpRequest<T> basicAuthentication(String id, String password) {
    return authentication(new UsernamePasswordCredentials(id, password).applyHttpChallenge(null));
  }

  /**
   * Configure the request to perform basic access authentication.
   * <p>
   * In basic HTTP authentication, a request contains a header field of the form 'Authorization: Basic &#60;credentials&#62;',
   * where credentials is the base64 encoding of id and password joined by a colon.
   * </p>
   * In practical terms the arguments are converted to a {@link UsernamePasswordCredentials} object.
   *
   * @param id       the id
   * @param password the password
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default HttpRequest<T> basicAuthentication(Buffer id, Buffer password) {
    return basicAuthentication(id.toString(), password.toString());
  }

  /**
   * Configure the request to perform bearer token authentication.
   * <p>
   * In OAuth 2.0, a request contains a header field of the form 'Authorization: Bearer &#60;bearerToken&#62;',
   * where bearerToken is the bearer token issued by an authorization server to access protected resources.
   * </p>
   * In practical terms the arguments are converted to a {@link TokenCredentials} object.
   *
   * @param bearerToken the bearer token
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default HttpRequest<T> bearerTokenAuthentication(String bearerToken) {
    return authentication(new TokenCredentials(bearerToken).applyHttpChallenge(null));
  }

  @Fluent
  HttpRequest<T> ssl(Boolean value);

  /**
   * Configures the amount of time in milliseconds after which if the request does not return any data within the timeout
   * period an {@link java.util.concurrent.TimeoutException} fails the request.
   * <p>
   * Setting zero or a negative {@code value} disables the timeout.
   *
   * @param value The quantity of time in milliseconds.
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> timeout(long value);

  /**
   * Add a query parameter to the request.
   *
   * @param paramName  the param name
   * @param paramValue the param value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> addQueryParam(String paramName, String paramValue);

  /**
   * Set a query parameter to the request.
   *
   * @param paramName  the param name
   * @param paramValue the param value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> setQueryParam(String paramName, String paramValue);

  /**
   * Set a request URI template string parameter to the request, expanded when the request URI is a {@link UriTemplate}.
   *
   * @param paramName  the param name
   * @param paramValue the param value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> setTemplateParam(String paramName, String paramValue);

  /**
   * Set a request URI template list parameter to the request, expanded when the request URI is a {@link UriTemplate}.
   *
   * @param paramName  the param name
   * @param paramValue the param value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> setTemplateParam(String paramName, List<String> paramValue);

  /**
   * Set a request URI template map parameter to the request, expanded when the request URI is a {@link UriTemplate}.
   *
   * @param paramName  the param name
   * @param paramValue the param value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> setTemplateParam(String paramName, Map<String, String> paramValue);

  /**
   * Set wether or not to follow the directs for the request.
   *
   * @param value true if redirections should be followed
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> followRedirects(boolean value);


  /**
   * Configure the request to set a proxy for this request.
   *
   * Setting proxy here supersedes the proxy set on the client itself
   *
   * @param proxyOptions The proxy options
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> proxy(ProxyOptions proxyOptions);

  /**
   * Add an expectation that the response is valid according to the provided {@code predicate}.
   * <p>
   * Multiple predicates can be added.
   *
   * @param predicate the predicate
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default HttpRequest<T> expect(Function<HttpResponse<Void>, ResponsePredicateResult> predicate) {
    return expect(predicate::apply);
  }

  /**
   * Add an expectation that the response is valid according to the provided {@code predicate}.
   * <p>
   * Multiple predicates can be added.
   *
   * @param predicate the predicate
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> expect(ResponsePredicate predicate);

  /**
   * Return the current query parameters.
   *
   * @return the current query parameters
   */
  MultiMap queryParams();

  /**
   * Return the current request URI template parameters.
   *
   * @return the current request URI template parameters
   */
  Variables templateParams();

  /**
   * Copy this request
   *
   * @return a copy of this request
   */
  HttpRequest<T> copy();

  /**
   * Allow or disallow multipart mixed encoding when sending {@link MultipartForm} having files sharing the same
   * file name.
   * <br/>
   * The default value is {@code true}.
   * <br/>
   * Set to {@code false} if you want to achieve the behavior for <a href="http://www.w3.org/TR/html5/forms.html#multipart-form-data">HTML5</a>.
   *
   * @param allow {@code true} allows use of multipart mixed encoding
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  HttpRequest<T> multipartMixed(boolean allow);

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} stream.
   *
   * @param body the body
   */
  void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * @param body the body
   * @see HttpRequest#sendStream(ReadStream, Handler)
   */
  default Future<HttpResponse<T>> sendStream(ReadStream<Buffer> body) {
    Promise<HttpResponse<T>> promise = Promise.promise();
    sendStream(body, promise);
    return promise.future();
  }

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} buffer.
   *
   * @param body the body
   */
  void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * @param body the body
   * @see HttpRequest#sendBuffer(Buffer, Handler)
   */
  default Future<HttpResponse<T>> sendBuffer(Buffer body) {
    Promise<HttpResponse<T>> promise = Promise.promise();
    sendBuffer(body, promise);
    return promise.future();
  }

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} object encoded as json and the content type
   * set to {@code application/json}.
   *
   * @param body the body
   */
  void sendJsonObject(JsonObject body, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * @param body the body
   * @see HttpRequest#sendJsonObject(JsonObject, Handler)
   */
  default Future<HttpResponse<T>> sendJsonObject(JsonObject body) {
    Promise<HttpResponse<T>> promise = Promise.promise();
    sendJsonObject(body, promise);
    return promise.future();
  }

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} object encoded as json and the content type
   * set to {@code application/json}.
   *
   * @param body the body
   */
  void sendJson(@Nullable Object body, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * @param body the body
   * @see HttpRequest#sendJson(Object, Handler)
   */
  default Future<HttpResponse<T>> sendJson(@Nullable Object body) {
    Promise<HttpResponse<T>> promise = Promise.promise();
    sendJson(body, promise);
    return promise.future();
  }

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} multimap encoded as form and the content type
   * set to {@code application/x-www-form-urlencoded}.
   * <p>
   * When the content type header is previously set to {@code multipart/form-data} it will be used instead.
   *
   * @param body the body
   */
  void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * @param body the body
   * @see HttpRequest#sendForm(MultiMap, Handler)
   */
  default Future<HttpResponse<T>> sendForm(MultiMap body) {
    Promise<HttpResponse<T>> promise = Promise.promise();
    sendForm(body, promise);
    return promise.future();
  }

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} multimap encoded as form and the content type
   * set to {@code application/x-www-form-urlencoded}.
   * <p>
   * When the content type header is previously set to {@code multipart/form-data} it will be used instead.
   *
   * NOTE: the use of this method is strongly discouraged to use when the form is a {@code application/x-www-form-urlencoded}
   * encoded form since the charset to use must be UTF-8.
   *
   * @param body the body
   */
  void sendForm(MultiMap body, String charset, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * @param body the body
   * @param charset the charset
   * @see HttpRequest#sendForm(MultiMap, String, Handler)
   */
  default Future<HttpResponse<T>> sendForm(MultiMap body, String charset) {
    Promise<HttpResponse<T>> promise = Promise.promise();
    sendForm(body, charset, promise);
    return promise.future();
  }

  /**
   * Like {@link #send(Handler)} but with an HTTP request {@code body} multimap encoded as form and the content type
   * set to {@code multipart/form-data}. You may use this method to send attributes and upload files.
   *
   * @param body the body
   */
  void sendMultipartForm(MultipartForm body, Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * @param body the body
   * @see HttpRequest#sendMultipartForm(MultipartForm, Handler)
   */
  default Future<HttpResponse<T>> sendMultipartForm(MultipartForm body) {
    Promise<HttpResponse<T>> promise = Promise.promise();
    sendMultipartForm(body, promise);
    return promise.future();
  }

  /**
   * Send a request, the {@code handler} will receive the response as an {@link HttpResponse}.
   */
  void send(Handler<AsyncResult<HttpResponse<T>>> handler);

  /**
   * @see HttpRequest#send(Handler)
   */
  default Future<HttpResponse<T>> send() {
    Promise<HttpResponse<T>> promise = Promise.promise();
    send(promise);
    return promise.future();
  }
}
