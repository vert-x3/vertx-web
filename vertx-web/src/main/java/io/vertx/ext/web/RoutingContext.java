/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.web;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the context for the handling of a request in Vert.x-Web.
 * <p>
 * A new instance is created for each HTTP request that is received in the
 * {@link Router#handle(HttpServerRequest)} of the router.
 * <p>
 * The same instance is passed to any matching request or failure handlers during the routing of the request or
 * failure.
 * <p>
 * The context provides access to the {@link HttpServerRequest} and {@link HttpServerResponse}
 * and allows you to maintain arbitrary data that lives for the lifetime of the context. Contexts are discarded once they
 * have been routed to the handler for the request.
 * <p>
 * The context also provides access to the {@link Session}, cookies and body for the request, given the correct handlers
 * in the application.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface RoutingContext {

  /**
   * @return the HTTP request object
   */
  @CacheReturn
  HttpServerRequest request();

  /**
   * @return the HTTP response object
   */
  @CacheReturn
  HttpServerResponse response();

  /**
   * Tell the router to route this context to the next matching route (if any).
   * This method, if called, does not need to be called during the execution of the handler, it can be called
   * some arbitrary time later, if required.
   * <p>
   * If next is not called for a handler then the handler should make sure it ends the response or no response
   * will be sent.
   */
  void next();

  /**
   * Fail the context with the specified status code.
   * <p>
   * This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
   * match a default failure response will be sent.
   *
   * @param statusCode  the HTTP status code
   */
  void fail(int statusCode);

  /**
   * Fail the context with the specified throwable.
   * <p>
   * This will cause the router to route the context to any matching failure handlers for the request. If no failure handlers
   * match a default failure response with status code 500 will be sent.
   *
   * @param throwable  a throwable representing the failure
   */
  void fail(Throwable throwable);

  /**
   * Put some arbitrary data in the context. This will be available in any handlers that receive the context.
   *
   * @param key  the key for the data
   * @param obj  the data
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  RoutingContext put(String key, Object obj);

  /**
   * Get some data from the context. The data is available in any handlers that receive the context.
   *
   * @param key  the key for the data
   * @param <T>  the type of the data
   * @return  the data
   * @throws ClassCastException if the data is not of the expected type
   */
  <T> T get(String key);

  /**
   * Remove some data from the context. The data is available in any handlers that receive the context.
   *
   * @param key  the key for the data
   * @param <T>  the type of the data
   * @return  the previous data associated with the key
   * @throws ClassCastException if the data is not of the expected type
   */
  <T> T remove(String key);

  /**
   * @return all the context data as a map
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Map<String, Object> data();

  /**
   * @return the Vert.x instance associated to the initiating {@link Router} for this context
   */
  @CacheReturn
  Vertx vertx();

  /**
   * @return the mount point for this router. It will be null for a top level router. For a sub-router it will be the path
   * at which the subrouter was mounted.
   */
  @Nullable String mountPoint();

  /**
   * @return the current route this context is being routed through.
   */
  Route currentRoute();

  /**
   * Return the normalised path for the request.
   * <p>
   * The normalised path is where the URI path has been decoded, i.e. any unicode or other illegal URL characters that
   * were encoded in the original URL with `%` will be returned to their original form. E.g. `%20` will revert to a space.
   * Also `+` reverts to a space in a query.
   * <p>
   * The normalised path will also not contain any `..` character sequences to prevent resources being accessed outside
   * of the permitted area.
   * <p>
   * It's recommended to always use the normalised path as opposed to {@link HttpServerRequest#path()}
   * if accessing server resources requested by a client.
   *
   * @return the normalised path
   */
  String normalisedPath();

  /**
   * Get the cookie with the specified name. The context must have first been routed to a {@link io.vertx.ext.web.handler.CookieHandler}
   * for this to work.
   *
   * @param name  the cookie name
   * @return the cookie
   */
  @Nullable Cookie getCookie(String name);

  /**
   * Add a cookie. This will be sent back to the client in the response. The context must have first been routed
   * to a {@link io.vertx.ext.web.handler.CookieHandler} for this to work.
   *
   * @param cookie  the cookie
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  RoutingContext addCookie(Cookie cookie);

  /**
   * Expire a cookie, notifying a User Agent to remove it from its cookie jar. The context must have first been routed
   * to a {@link io.vertx.ext.web.handler.CookieHandler} for this to work.
   *
   * @param name  the name of the cookie
   * @return the cookie, if it existed, or null
   */
  default @Nullable Cookie removeCookie(String name) {
    return removeCookie(name, true);
  }

  /**
   * Remove a cookie from the cookie set. If invalidate is true then it will expire a cookie, notifying a User Agent to
   * remove it from its cookie jar. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.CookieHandler} for this to work.
   *
   * @param name  the name of the cookie
   * @return the cookie, if it existed, or null
   */
  @Nullable Cookie removeCookie(String name, boolean invalidate);

  /**
   * @return the number of cookies. The context must have first been routed to a {@link io.vertx.ext.web.handler.CookieHandler}
   * for this to work.
   */
  int cookieCount();

  /**
   * @return a set of all the cookies. The context must have first been routed to a {@link io.vertx.ext.web.handler.CookieHandler}
   * for this to be populated.
   */
  Set<Cookie> cookies();

  /**
   * @return  the entire HTTP request body as a string, assuming UTF-8 encoding. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   */
  @Nullable String getBodyAsString();

  /**
   * Get the entire HTTP request body as a string, assuming the specified encoding. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   *
   * @param encoding  the encoding, e.g. "UTF-16"
   * @return the body
   */
  @Nullable String getBodyAsString(String encoding);

  /**
   * @return Get the entire HTTP request body as a {@link JsonObject}. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   */
  @Nullable JsonObject getBodyAsJson();

  /**
   * @return Get the entire HTTP request body as a {@link JsonArray}. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   */
  @Nullable JsonArray getBodyAsJsonArray();

  /**
   * @return Get the entire HTTP request body as a {@link Buffer}. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to be populated.
   */
  @Nullable Buffer getBody();

  /**
   * @return a set of fileuploads (if any) for the request. The context must have first been routed to a
   * {@link io.vertx.ext.web.handler.BodyHandler} for this to work.
   */
  Set<FileUpload> fileUploads();

  /**
   * Get the session. The context must have first been routed to a {@link io.vertx.ext.web.handler.SessionHandler}
   * for this to be populated.
   * Sessions live for a browser session, and are maintained by session cookies.
   * @return  the session.
   */
  @Nullable Session session();

  /**
   * Get the authenticated user (if any). This will usually be injected by an auth handler if authentication if successful.
   * @return  the user, or null if the current user is not authenticated.
   */
  @Nullable User user();

  /**
   * If the context is being routed to failure handlers after a failure has been triggered by calling
   * {@link #fail(Throwable)} then this will return that throwable. It can be used by failure handlers to render a response,
   * e.g. create a failure response page.
   *
   * @return  the throwable used when signalling failure
   */
  @CacheReturn
  @Nullable
  Throwable failure();

  /**
   * If the context is being routed to failure handlers after a failure has been triggered by calling
   * {@link #fail(int)}  then this will return that status code.  It can be used by failure handlers to render a response,
   * e.g. create a failure response page.
   *
   * When the status code has not been set yet (it is undefined) its value will be -1.
   *
   * @return  the status code used when signalling failure
   */
  @CacheReturn
  int statusCode();

  /**
   * If the route specifies produces matches, e.g. produces `text/html` and `text/plain`, and the `accept` header
   * matches one or more of these then this returns the most acceptable match.
   *
   * @return  the most acceptable content type.
   */
  @Nullable String getAcceptableContentType();

  /**
   * The headers:
   * <ol>
   * <li>Accept</li>
   * <li>Accept-Charset</li>
   * <li>Accept-Encoding</li>
   * <li>Accept-Language</li>
   * <li>Content-Type</li>
   * </ol>
   * Parsed into {@link ParsedHeaderValue}
   * @return A container with the parsed headers.
   */
  @CacheReturn
  ParsedHeaderValues parsedHeaders();

  /**
   * Add a handler that will be called just before headers are written to the response. This gives you a hook where
   * you can write any extra headers before the response has been written when it will be too late.
   *
   * @param handler  the handler
   * @return  the id of the handler. This can be used if you later want to remove the handler.
   */
  int addHeadersEndHandler(Handler<Void> handler);

  /**
   * Remove a headers end handler
   *
   * @param handlerID  the id as returned from {@link io.vertx.ext.web.RoutingContext#addHeadersEndHandler(Handler)}.
   * @return true if the handler existed and was removed, false otherwise
   */
  boolean removeHeadersEndHandler(int handlerID);

  /**
   * Provides a handler that will be called after the last part of the body is written to the wire.
   * The handler is called asynchronously of when the response has been received by the client.
   * This provides a hook allowing you to do more operations once the request has been sent over the wire.
   * Do not use this for resource cleanup as this handler might never get called (e.g. if the connection is reset).
   *
   * @param handler  the handler
   * @return  the id of the handler. This can be used if you later want to remove the handler.
   */
  int addBodyEndHandler(Handler<Void> handler);

  /**
   * Remove a body end handler
   *
   * @param handlerID  the id as returned from {@link io.vertx.ext.web.RoutingContext#addBodyEndHandler(Handler)}.
   * @return true if the handler existed and was removed, false otherwise
   */
  boolean removeBodyEndHandler(int handlerID);

  /**
   * @return true if the context is being routed to failure handlers.
   */
  boolean failed();

  /**
   * Set the body. Used by the {@link io.vertx.ext.web.handler.BodyHandler}. You will not normally call this method.
   *
   * @param body  the body
   */
  void setBody(Buffer body);

  /**
   * Set the session. Used by the {@link io.vertx.ext.web.handler.SessionHandler}. You will not normally call this method.
   *
   * @param session  the session
   */
  void setSession(Session session);

  /**
   * Set the user. Usually used by auth handlers to inject a User. You will not normally call this method.
   *
   * @param user  the user
   */
  void setUser(User user);

  /**
   * Clear the current user object in the context. This usually is used for implementing a log out feature, since the
   * current user is unbounded from the routing context.
   */
  void clearUser();

  /**
   * Set the acceptable content type. Used by
   * @param contentType  the content type
   */
  void setAcceptableContentType(@Nullable String contentType);

  /**
   * Restarts the current router with a new path and reusing the original method. All path parameters are then parsed
   * and available on the params list.
   *
   * @param path the new http path.
   */
  default void reroute(String path) {
    reroute(request().method(), path);
  }

  /**
   * Restarts the current router with a new method and path. All path parameters are then parsed and available on the
   * params list.
   *
   * @param method the new http request
   * @param path the new http path.
   */
  void reroute(HttpMethod method, String path);

  /**
   * Returns the locales for the current request. The locales are determined from the `accept-languages` header and
   * sorted on quality.
   *
   * When 2 or more entries have the same quality then the order used to return the best match is based on the lowest
   * index on the original list. For example if a user has en-US and en-GB with same quality and this order the best
   * match will be en-US because it was declared as first entry by the client.
   *
   * @deprecated Use {@link #acceptableLanguages()} or {@link #parsedHeaders()}.{@link ParsedHeaderValues#acceptLanguage()}
   * @return the best matched locale for the request
   */
  @Deprecated
  @CacheReturn
  List<Locale> acceptableLocales();

  /**
   * Returns the languages for the current request. The languages are determined from the <code>Accept-Language</code>
   * header and sorted on quality.
   *
   * When 2 or more entries have the same quality then the order used to return the best match is based on the lowest
   * index on the original list. For example if a user has en-US and en-GB with same quality and this order the best
   * match will be en-US because it was declared as first entry by the client.
   *
   * @return The best matched language for the request
   */
  @CacheReturn
  default List<LanguageHeader> acceptableLanguages(){
    return parsedHeaders().acceptLanguage();
  }

  /**
   * Helper to return the user preferred locale. It is the same action as returning the first element of the acceptable
   * locales.
   *
   * @deprecated Use {@link #preferredLanguage()} instead
   * @return the users preferred locale.
   */
  @CacheReturn
  @Deprecated
  default Locale preferredLocale() {
    return preferredLanguage();
  }

  /**
   * Helper to return the user preferred language.
   * It is the same action as returning the first element of the acceptable languages.
   *
   * @return the users preferred locale.
   */
  @CacheReturn
  default LanguageHeader preferredLanguage() {
    List<? extends LanguageHeader> acceptableLanguages = acceptableLanguages();
    return acceptableLanguages.size() > 0 ? acceptableLanguages.get(0) : null;
  }

  /**
   * Returns a map of named parameters as defined in path declaration with their actual values
   *
   * @return the map of named parameters
   */
  Map<String, String> pathParams();

  /**
   * Gets the value of a single path parameter
   *
   * @param name the name of parameter as defined in path declaration
   * @return the actual value of the parameter or null if it doesn't exist
   */
  @Nullable
  String pathParam(String name);

  /**
   * Returns a map of all query parameters inside the <a href="https://en.wikipedia.org/wiki/Query_string">query string</a>
   *
   * @return the multimap of query parameters
   */
  MultiMap queryParams();

  /**
   * Gets the value of a single query parameter
   *
   * @param query The name of query parameter
   * @return The list of all elements inside query parameter
   */
  @Nullable
  List<String> queryParam(String query);
}
